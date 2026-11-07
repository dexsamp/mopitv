package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MoppiTab
import com.example.ui.theme.*

@Composable
fun MoppiBottomBar(
    selectedTab: MoppiTab,
    onTabSelected: (MoppiTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("moppi_bottom_bar"),
        color = MoppiWhite,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = MoppiBorder, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoppiTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    val icon: ImageVector = when (tab) {
                        MoppiTab.HOME -> Icons.Filled.Home
                        MoppiTab.NOBAR -> Icons.Filled.Tv
                        MoppiTab.SHORTS -> Icons.Filled.PlayCircleOutline
                        MoppiTab.PETS -> Icons.Filled.AutoAwesome
                        MoppiTab.COMMUNITY -> Icons.Filled.Forum
                        MoppiTab.PROFILE -> Icons.Filled.PersonOutline
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MoppiSurfaceMuted else MoppiWhite
                            )
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 6.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) MoppiSapphire else MoppiTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MoppiSapphire else MoppiTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
