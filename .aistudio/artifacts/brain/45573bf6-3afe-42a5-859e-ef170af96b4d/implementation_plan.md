# WAR MOTION: Engine Tab, Multi-API Key Failover & Render Cancellation

Implementasi pembatalan render interaktif, penyesuaian default durasi 10 detik, standarisasi konsistensi container, rebranding visual "WAR MOTION", serta tab Engine untuk manajemen multi-API key dengan failover otomatis.

### User Review & Critical Decisions

> [!IMPORTANT]
> Keputusan yang telah dikonfirmasi pengguna:
> - **Penyimpanan API Key**: Daftar API key disimpan secara persisten di penyimpanan lokal perangkat (`SharedPreferences`), sehingga tidak hilang saat aplikasi ditutup.
> - **Auto Failover**: Saat terjadi error API (rate limit / quota habis), sistem otomatis beralih ke API key berikutnya secara sirkular disertai notifikasi toast singkat kepada pengguna.
> - **Pembatalan Render**: Menekan tombol Cancel saat render berjalan akan langsung menghentikan proses enkoding seketika dan mereset status tanpa pop-up tambahan.

---

### 1. Overview & Core Concept

- **Rebranding & Header Styling**:
  - Mengubah judul aplikasi di header menjadi **WAR MOTION** dengan tipografi bold: **WAR** (putih) dan **MOTION** (oranye cerah `#FF6D00`).
  - Label di sisi kanan header diubah dari "STUDIO" menjadi **"Engine"** yang interaktif dan dapat diklik untuk membuka panel manajemen Engine & API Key.
  - Memperbarui `res/values/strings.xml` dan `metadata.json` agar konsisten dengan identitas aplikasi.
- **Fungsi Cancel Render & Penyederhanaan Tombol**:
  - Tombol render di layar Export menggunakan teks ringkas: **"Render Mp4"**.
  - Saat proses render berjalan, tombol secara dinamis bertransformasi menjadi **"Cancel"** dengan visual merah rose/crimson. Saat diklik, langsung membatalkan proses render di WebView engine dan mereset state.
- **Standarisasi Durasi & Konsistensi Grid**:
  - Nilai default durasi diubah menjadi **10 detik**.
  - Menyamakan lebar dan margin seluruh kartu kontainer di layar Export dan Studio agar konsisten penuh (`fillMaxWidth` dengan padding 16.dp).
- **Tab Engine & Manajemen Multi-API Key**:
  - Antarmuka input API key dengan `OutlinedTextField` fleksibel yang dapat melebar vertikal hingga 3 baris.
  - Tombol **Add** di sebelah kanan input untuk menambahkan key ke daftar.
  - Daftar API key dilengkapi **Radio Button** untuk memilih active key, tombol hapus (*trash*), dan status masking (`AIza...xxxx`).
  - Integrasi failover sirkular pada `GeminiMetadataService`: jika active key mengalami kendala, sistem otomatis berpindah ke key cadangan berikutnya dan melanjutkan request.

---

### 2. User Experience & Visual Design

#### Key User Flows:
1. **Alur Pembatalan Render**:
   - Pengguna menekan tombol **"Render Mp4"**.
   - Tombol seketika berubah menjadi tombol **"Cancel"** (warna merah/crimson menyala) dengan ikon silang/stop.
   - Jika pengguna menekan **"Cancel"**, perintah penghentian dikirim ke JavaScript engine, progress bar dihentikan, dan tampilan kembali siap untuk render ulang.
2. **Alur Manajemen API Key di Tab Engine**:
   - Pengguna mengetuk chip **"Engine"** pada header kaca di bagian atas layar.
   - Aplikasi beralih ke layar/tab **Engine API Settings**.
   - Pengguna menempelkan API key Gemini di textarea (melebar hingga 3 baris) dan menekan tombol **"Add"**.
   - Key baru muncul di daftar bawah dengan radio button aktif.
   - Pengguna dapat menambahkan beberapa API key cadangan.
   - Saat generate metadata dijalankan di layar Export/Galeri, sistem menggunakan API key aktif. Jika API error (misal error 429), toast muncul: *"API Key #1 error, beralih ke API Key #2"*, radio button berpindah otomatis, dan metadata tetap berhasil dibuat.

#### Visual Identity & Theme:
- **Branding Header**:
  - Teks: `WAR` (Color.White, ExtraBold) + ` ` + `MOTION` (Color(0xFFFF6D00), ExtraBold).
  - Chip Header: Label **"Engine"** dengan efek kaca ungu-oranye bergradasi yang memikat.
- **Tombol Render/Cancel**:
  - State Idle: Electric Blue (`#0284C7`) dengan label **"Render Mp4"**.
  - State Rendering: Crimson Coral (`#E11D48`) dengan label **"Cancel"** dan animasi pulse halus.
- **Komponen Engine Card**:
  - Kartu Glassmorphism semi-transparan yang serasi dengan tema dark navy-purple.
  - Radio button aktif berwarna Sky Glow / Matrix Green.

---

### 3. Key Product Decisions & Trade-Offs

- **Penyimpanan Multi-API Key**:
  - *Pendekatan*: Menggunakan repository lokal berbasis SharedPreferences (`ApiKeyRepository`) yang menyimpan daftar string API key dan indeks key yang sedang aktif.
  - *Alasan*: Ringan, cepat, tidak memerlukan setup database Room yang kompleks untuk daftar string sederhana, dan data langsung siap saat aplikasi boot.
- **Failover Logic (Round-Robin)**:
  - *Pendekatan*: Menjalankan loop percobaan rotasi key hingga maksimal `daftarKey.size` kali. Jika semua key mengalami kegagalan atau daftar kosong, sistem beralih otomatis ke *smart fallback generator*.
  - *Alasan*: Menjamin zero-downtime bagi kreator saat memproduksi video stock secara maraton.
- **Integrasi Navigasi Engine**:
  - *Pendekatan*: Menambahkan `NavigationTab.ENGINE` atau toggle langsung dari chip header Engine, dengan dukungan tombol Back kembali ke Studio/Export.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   TopAppBar Header                     │
│  WAR (White) MOTION (Orange)  ───► [ Chip: Engine ]    │
└───────────────────────────────────────────┬────────────┘
                                            │ Navigates to
                                            ▼
┌────────────────────────────────────────────────────────┐
│                      ENGINE TAB                        │
│  ┌───────────────────────────────────────┬──────────┐  │
│  │ Textarea (expandable up to 3 lines)   │   Add    │  │
│  └───────────────────────────────────────┴──────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ List API Keys with Radio Buttons & Delete Icons  │  │
│  │ (●) AIzaSy...A89f (Active)           [Trash]     │  │
│  │ (○) AIzaSy...K21p (Backup)           [Trash]     │  │
│  └──────────────────────────────────────────────────┘  │
└───────────────────────────┬────────────────────────────┘
                            │ Persisted via SharedPreferences
                            ▼
┌────────────────────────────────────────────────────────┐
│                  ApiKeyRepository                      │
│   - getApiKeys(): List<String>                         │
│   - getActiveKey(): String?                            │
│   - rotateToNextKey(): String?                         │
└───────────────────────────┬────────────────────────────┘
                            │ Injected into
                            ▼
┌────────────────────────────────────────────────────────┐
│              GeminiMetadataService                     │
│   - Call with active API key                           │
│   - On failure: auto-rotate to next key & retry        │
│   - Fallback generator if all exhausted                │
└────────────────────────────────────────────────────────┘
```

#### Komponen yang Akan Dibuat / Diupdate:
1. `com.example.data.ApiKeyRepository`: Mengelola persistensi list API key dan rotasi failover.
2. `com.example.ui.screens.EngineScreen`: Antarmuka input textarea dinamis (hingga 3 baris), tombol Add, dan list radio button API key.
3. `com.example.viewmodel.CodeMotionViewModel`:
   - State `apiKeys`, `activeApiKeyIndex`.
   - Fungsi `addApiKey`, `selectApiKey`, `deleteApiKey`.
   - Update `RenderConfig` dengan default `durationSeconds = 10`.
   - Fungsi `cancelRender()` untuk menghentikan encoder dan me-reset state ke `Idle`.
4. `com.example.engine.WebCodecsBridge` & `EngineHtmlBuilder`: Mendukung penanganan sinyal cancel.
5. `MainActivity.kt`: Rebranding judul menjadi **WAR MOTION**, chip **Engine** yang dapat diklik, dan navigasi tab Engine.
6. `ExportScreen.kt`: Tombol dinamis "Render Mp4" / "Cancel", standarisasi lebar container seragam.
