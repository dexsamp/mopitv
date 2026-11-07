package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.NobarChatMessage
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Real-time WatchParty Firestore Repository.
 * Powers synchronized room chat messaging overlays via Firestore real-time listeners.
 */
class WatchPartyFirestoreRepository(
    private val firestore: FirebaseFirestore?
) {
    companion object {
        private const val TAG = "WatchPartyFirestore"

        fun create(context: Context): WatchPartyFirestoreRepository {
            return try {
                val dbId = context.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                WatchPartyFirestoreRepository(db)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize Firestore with firestore_database_id: ${e.message}")
                WatchPartyFirestoreRepository(null)
            }
        }
    }

    // In-memory local message cache per room for instant optimistic UI & offline fallback
    private val localRoomMessages = MutableStateFlow<Map<String, List<NobarChatMessage>>>(emptyMap())

    /**
     * Observes real-time messages for a specific WatchParty room.
     * Uses Firestore snapshot flow when connected, merged with local optimistic messages.
     */
    fun observeRoomMessages(roomId: String): Flow<List<NobarChatMessage>> {
        val db = firestore
        val currentUserId = if (db != null) {
            try { Firebase.auth.currentUser?.uid } catch (e: Exception) { null }
        } else null

        if (db == null || currentUserId == null) {
            // Unauthenticated or offline: observe local room messages
            return localRoomMessages.map { map ->
                map[roomId] ?: getDefaultMessagesForRoom(roomId)
            }
        }

        return flow {
            val query = db.collection("watchparties")
                .document(roomId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(100)

            emitAll(
                query.snapshots().map { snapshot ->
                    val remoteMessages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            val roleStr = data["senderRole"] as? String ?: "WARGA"
                            val role = try {
                                UserRole.valueOf(roleStr)
                            } catch (e: Exception) {
                                UserRole.WARGA
                            }

                            val timestamp = doc.getTimestamp("createdAt")
                            val timeStr = if (timestamp != null) {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
                            } else {
                                "Baru saja"
                            }

                            NobarChatMessage(
                                id = doc.id,
                                senderName = data["senderName"] as? String ?: "Warga",
                                senderRole = role,
                                isSenderPremium = data["isSenderPremium"] as? Boolean ?: false,
                                senderUid = data["senderId"] as? String ?: "",
                                senderAvatarEmoji = when (role) {
                                    UserRole.DEVELOPER -> "👑"
                                    UserRole.ADMIN -> "🛡️"
                                    UserRole.SEPUH -> "⚡"
                                    UserRole.WARGA -> "🍿"
                                },
                                message = data["message"] as? String ?: "",
                                timestamp = timeStr
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping message doc", e)
                            null
                        }
                    }

                    val localMsgs = localRoomMessages.value[roomId] ?: emptyList()
                    val combined = (remoteMessages + localMsgs).distinctBy { it.id }
                    if (combined.isEmpty()) getDefaultMessagesForRoom(roomId) else combined
                }.catch { error ->
                    Log.w(TAG, "Firestore message stream error: ${error.message}")
                    val fallback = localRoomMessages.value[roomId] ?: getDefaultMessagesForRoom(roomId)
                    emit(fallback)
                }
            )
        }
    }

    /**
     * Sends a real-time message to the WatchParty chat overlay.
     */
    suspend fun sendMessage(roomId: String, messageText: String, user: UserProfile): Result<Unit> {
        val cleanText = messageText.trim()
        if (cleanText.isEmpty()) return Result.success(Unit)

        val localId = "msg_local_${System.currentTimeMillis()}"
        val optimisticMsg = NobarChatMessage(
            id = localId,
            senderName = user.username,
            senderRole = user.role,
            isSenderPremium = user.isPremium,
            senderUid = user.id,
            senderAvatarEmoji = when (user.role) {
                UserRole.DEVELOPER -> "👑"
                UserRole.ADMIN -> "🛡️"
                UserRole.SEPUH -> "⚡"
                UserRole.WARGA -> "🍿"
            },
            message = cleanText,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )

        // Optimistically record locally
        localRoomMessages.update { map ->
            val currentList = map[roomId] ?: emptyList()
            map + (roomId to (currentList + optimisticMsg))
        }

        val db = firestore
        val currentUserId = if (db != null) {
            try { Firebase.auth.currentUser?.uid } catch (e: Exception) { null }
        } else null

        if (db != null && currentUserId != null) {
            try {
                val payload = hashMapOf<String, Any>(
                    "roomId" to roomId,
                    "senderId" to currentUserId,
                    "senderName" to user.username,
                    "senderRole" to user.role.name,
                    "isSenderPremium" to user.isPremium,
                    "message" to cleanText,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("watchparties")
                    .document(roomId)
                    .collection("messages")
                    .document()
                    .set(payload)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist chat message to Firestore, kept local: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    private fun getDefaultMessagesForRoom(roomId: String): List<NobarChatMessage> {
        return listOf(
            NobarChatMessage(
                id = "demo_1_$roomId",
                senderName = "MoppiBot",
                senderRole = UserRole.DEVELOPER,
                isSenderPremium = true,
                senderUid = "system_dev",
                senderAvatarEmoji = "👑",
                message = "Selamat datang di Live Sync WatchParty Room! Nikmati audio & frame sinkron tanpa lag 🔥",
                timestamp = "12:00"
            ),
            NobarChatMessage(
                id = "demo_2_$roomId",
                senderName = "AnimeSepuh",
                senderRole = UserRole.SEPUH,
                isSenderPremium = true,
                senderUid = "sepuh_01",
                senderAvatarEmoji = "⚡",
                message = "Visual animasi studio di episode ini gokil parah, 60 FPS halus banget! 🍿",
                timestamp = "12:02"
            ),
            NobarChatMessage(
                id = "demo_3_$roomId",
                senderName = "SakuraFan",
                senderRole = UserRole.WARGA,
                isSenderPremium = false,
                senderUid = "warga_02",
                senderAvatarEmoji = "🌸",
                message = "Suara mic jernih banget, nobar sambil ngobrol seru bgt min!",
                timestamp = "12:03"
            )
        )
    }
}
