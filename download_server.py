import os
import sys
from http.server import HTTPServer, SimpleHTTPRequestHandler

PORT = 3000
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
APK_PATH = os.path.join(BASE_DIR, "MoppiTV.apk")
if not os.path.exists(APK_PATH):
    APK_PATH = os.path.join(BASE_DIR, "app/build/outputs/apk/debug/app-debug.apk")

HTML_PAGE = """<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Download MoppiTV APK</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Plus Jakarta Sans', -apple-system, sans-serif;
            background: #F8FAFC;
            color: #0F172A;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: 20px;
        }
        .card {
            background: #FFFFFF;
            max-width: 480px;
            width: 100%;
            border-radius: 28px;
            box-shadow: 0 20px 40px -15px rgba(0,0,0,0.07), 0 0 1px 1px rgba(0,0,0,0.04);
            padding: 36px 28px;
            text-align: center;
            border: 1px solid #E2E8F0;
        }
        .app-icon {
            width: 88px;
            height: 88px;
            background: linear-gradient(135deg, #2563EB, #06B6D4);
            border-radius: 22px;
            margin: 0 auto 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 42px;
            box-shadow: 0 12px 24px -6px rgba(37, 99, 235, 0.35);
        }
        h1 {
            font-size: 24px;
            font-weight: 800;
            color: #0F172A;
            margin-bottom: 6px;
        }
        p.subtitle {
            font-size: 14px;
            color: #64748B;
            margin-bottom: 24px;
        }
        .badge {
            display: inline-block;
            background: #EFF6FF;
            color: #2563EB;
            padding: 4px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
            margin-bottom: 24px;
            border: 1px solid #DBEAFE;
        }
        .btn-download {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            background: #2563EB;
            color: #FFFFFF;
            text-decoration: none;
            padding: 16px 24px;
            border-radius: 16px;
            font-size: 16px;
            font-weight: 700;
            box-shadow: 0 10px 20px -5px rgba(37, 99, 235, 0.4);
            transition: all 0.2s ease;
        }
        .btn-download:hover {
            background: #1D4ED8;
            transform: translateY(-2px);
        }
        .file-info {
            font-size: 12px;
            color: #94A3B8;
            margin-top: 10px;
            margin-bottom: 28px;
        }
        .qr-section {
            background: #F8FAFC;
            border-radius: 20px;
            padding: 20px;
            margin-bottom: 24px;
            border: 1px dashed #CBD5E1;
        }
        .qr-section img {
            width: 160px;
            height: 160px;
            border-radius: 12px;
            margin-bottom: 10px;
        }
        .qr-text {
            font-size: 12px;
            color: #64748B;
            font-weight: 600;
        }
        .steps {
            text-align: left;
            background: #F8FAFC;
            border-radius: 18px;
            padding: 18px 20px;
            border: 1px solid #E2E8F0;
        }
        .steps h3 {
            font-size: 13px;
            font-weight: 700;
            color: #334155;
            margin-bottom: 12px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .step-item {
            display: flex;
            gap: 12px;
            font-size: 13px;
            color: #475569;
            margin-bottom: 10px;
            line-height: 1.4;
        }
        .step-num {
            background: #2563EB;
            color: white;
            min-width: 20px;
            height: 20px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            font-weight: 700;
            margin-top: 1px;
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="app-icon">📺</div>
        <h1>MoppiTV</h1>
        <p class="subtitle">Stream Anime OtakuDesu Sub Indo HD & Nobar WatchParty</p>
        <span class="badge">Versi 1.0.0 (Release APK)</span>

        <a href="/MoppiTV.apk" class="btn-download" download="MoppiTV.apk">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            Download APK Sekarang
        </a>
        <p class="file-info">MoppiTV.apk • Ukuran: ~34 MB • Android 8.0+</p>

        <div class="qr-section">
            <img id="qr-img" src="" alt="Scan QR Code">
            <p class="qr-text">Scan QR code ini lewat kamera HP Android Anda untuk download langsung</p>
        </div>

        <div class="steps">
            <h3>Cara Pasang di HP Android:</h3>
            <div class="step-item">
                <div class="step-num">1</div>
                <div>Klik tombol download atau scan QR di atas.</div>
            </div>
            <div class="step-item">
                <div class="step-num">2</div>
                <div>Setelah download selesai, buka file <b>MoppiTV.apk</b> di HP.</div>
            </div>
            <div class="step-item">
                <div class="step-num">3</div>
                <div>Pilih <b>"Instal"</b>. Jika muncul proteksi Google Play Protect, pilih <b>"Tetap Instal (Install anyway)"</b>.</div>
            </div>
        </div>
    </div>

    <script>
        const downloadUrl = window.location.origin + '/MoppiTV.apk';
        document.getElementById('qr-img').src = 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=' + encodeURIComponent(downloadUrl);
    </script>
</body>
</html>
"""

class ApkHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path in ("/", "/index.html"):
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(HTML_PAGE.encode("utf-8"))))
            self.end_headers()
            self.wfile.write(HTML_PAGE.encode("utf-8"))
            return

        if self.path in ("/MoppiTV.apk", "/app-debug.apk", "/download"):
            if not os.path.exists(APK_PATH):
                self.send_error(404, "APK not found")
                return

            file_size = os.path.getsize(APK_PATH)
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Disposition", 'attachment; filename="MoppiTV.apk"')
            self.send_header("Content-Length", str(file_size))
            self.end_headers()

            with open(APK_PATH, "rb") as f:
                while chunk := f.read(64 * 1024):
                    self.wfile.write(chunk)
            return

        return super().do_GET()

if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", PORT), ApkHandler)
    print(f"APK Download Server running on port {PORT}")
    server.serve_forever()
