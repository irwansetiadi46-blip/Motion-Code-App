# Dual Output Mode: Videos (MP4) & Images (JPG / PNG with Transparency)

Integrasi mode ganda **Videos** dan **Images** pada War Motion. Pengguna dapat merender animasi canvas berbasis kode menjadi video MP4 loop atau mengekspor frame gambar beresolusi tinggi (JPG atau PNG transparan) hingga 4K, dengan dropdown mode terpadu di tab Studio dan tab Export serta persistensi format file.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> Keputusan dan preferensi yang telah dikonfirmasi pengguna:
> - **Sinkronisasi Antar Tab**: Mode aktif (**Videos** / **Images**) disinkronkan di seluruh aplikasi (tab Studio dan tab Export).
> - **Latar Belakang JPG**: Format JPG (yang tidak mendukung kanal alpha) akan diisi dengan latar belakang hitam pekat (`#000000`) jika kode canvas memiliki area transparan.
> - **Dukungan Transparansi PNG**: Format PNG mempertahankan transparansi asli 100% jika kode canvas tidak mengisi background (transparan).
> - **Resolusi Gambar**: Menggunakan selektor resolusi yang sudah ada (1:1, 16:9, 9:16, hingga resolusi 4K Ultra HD).
> - **Format Persisten**: Pilihan format file (JPG / PNG) disimpan di memori dan tidak di-reset ke nilai default saat berpindah layar.

---

### 1. Overview & Core Concept

- **Mode Selector Container**:
  - Ditempatkan tepat di atas Viewport pada tab **Studio** (dan disinkronkan ke tab Export).
  - Menggunakan Card bernuansa dark glassmorphism dengan teks status mode aktif ("Videos" / "Images") dan ikon panah ke bawah.
  - Menampilkan menu dropdown interaktif dengan pilihan **Videos** dan **Images**.
- **Mode Videos (Default)**:
  - Berjalan seperti sedia kala: tombol **"Render Mp4"**, progress render frame-by-frame, preview pemutar video MP4, dan tombol **"Download MP4"** dengan auto-rename metadata.
- **Mode Images**:
  - Fungsi render dialihkan untuk menangkap frame canvas beresolusi tinggi langsung dari WebGL/Canvas 2D engine.
  - Label tombol render berubah menjadi **"Render Images"**.
  - Tombol download berubah menjadi **"Export"**.
  - Di samping tombol "Export", terdapat tombol dropdown pilihan format: **JPG** dan **PNG**.
  - Format PNG mempertahankan transparansi penuh jika kode canvas bersifat transparan.
  - Format JPG menerapkan background hitam pekat secara mulus sebelum encoding.
  - File hasil export disimpan ke folder `Download/Code Motion Video` dengan nama sesuai textarea metadata (atau fallback `Img-WM.jpg` / `Img-WM.png`).

---

### 2. User Experience & Visual Design

#### Key User Flows:
1. **Beralih ke Mode Images**:
   - Di tab Studio, pengguna melihat container Card mode di atas viewport dengan teks **"Videos"** dan ikon panah.
   - Mengetuk card memunculkan dropdown menu: pengguna memilih **"Images"**.
   - UI bereaksi instan: tombol render bertransformasi menjadi **"Render Images"**.
2. **Merender Gambar**:
   - Pengguna menekan tombol **"Render Images"**.
   - Engine mengeksekusi canvas pada resolusi target (misal 3840×2160 atau 1080×1080) dan menangkap data grafis.
   - Selesai dalam hitungan milidetik; tampilan langsung menampilkan preview gambar yang baru saja dirender dengan badge dimensi dan format.
3. **Mengekspor & Memilih Format**:
   - Pengguna melihat tombol utama **"Export"** dan tombol dropdown format di sebelahnya bertuliskan **"PNG"** atau **"JPG"**.
   - Mengetuk tombol dropdown membuka opsi **JPG** dan **PNG**. Format yang dipilih langsung tersimpan.
   - Mengetuk tombol **"Export"** menyimpan tepat 1 file gambar ke direktori `Download/Code Motion Video` dengan nama otomatis dari textarea Metadata.

#### Visual Styling:
- **Card Mode Selector**:
  - Background surface gelap `DarkSurface` (`#0F172A`) dengan border tipis `ElectricCyan` / `SkyGlow`.
  - Chip indikator status (ikon Film untuk Videos, ikon Image/Palette untuk Images).
- **Split Export Button**:
  - Tombol aksi utama **"Export"** dengan Electric Blue (`#0284C7`).
  - Tombol dropdown format file kompak berdampingan dengan border vertikal pemisah, teks bold format (PNG / JPG), dan chevron down.

---

### 3. Key Product Decisions & Trade-Offs

- **Penangkapan Gambar via JavaScript Engine Bridge**:
  - *Pendekatan*: Menambahkan fungsi `window.renderStillImage(format, bgFill)` di `EngineHtmlBuilder.kt` yang mengeksekusi canvas di iframe stage scaler, menggambar ke canvas resolusi target, dan mengirimkan base64 data URL ke `WebCodecsBridge`.
  - *Alasan*: Menjamin gambar yang dihasilkan memiliki ketajaman pixel-perfect sesuai resolusi terpilih (hingga 4K) dan mempertahankan transparansi canvas WebGL/2D tanpa bergantung pada tangkapan layar (screenshot) layar HP yang terbatas ukuran fisiknya.
- **Dukungan Transparansi Otomatis**:
  - *Pendekatan*: Jika format PNG dipilih, canvas diekspor langsung dengan kanal RGBA 32-bit. Jika JPG dipilih, canvas sementara dialokasikan dengan `fillStyle = '#000000'` lalu digambar canvas utama di atasnya sebelum kompresi JPEG kualitas 95%.
  - *Alasan*: Memenuhi aturan format microstock dan kebutuhan desainer grafis.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       STUDIO SCREEN                         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Mode Selector Card: [ 🎬 Videos / 🖼️ Images  ▼ ]      │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Live Viewport Stage (HTML5 / WebGL Canvas)            │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────┬────────────────────┐  │
│  │ [ Render Mp4 / Render Images ]   │                    │  │
│  └──────────────────────────────────┴────────────────────┘  │
│  ┌──────────────────────────────────┬────────────────────┐  │
│  │ [ Download Mp4 / Export ]        │ [ Format: PNG ▼ ]  │  │
│  └──────────────────────────────────┴────────────────────┘  │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow (activeOutputMode, selectedImageFormat)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   CodeMotionViewModel                       │
│  - outputMode: OutputMode (VIDEOS, IMAGES)                  │
│  - imageFormat: ImageFormat (PNG, JPG)                      │
│  - triggerRender() -> dispatches video/image render         │
│  - downloadMedia() -> saves MP4 or PNG/JPG                  │
└──────────────────────────────┬──────────────────────────────┘
                               │ JavaScript Interface
                               ▼
┌─────────────────────────────────────────────────────────────┐
│              EngineHtmlBuilder & WebCodecsBridge            │
│  - window.startVideoRender() (MP4 H.264 WebCodecs)          │
│  - window.renderStillImage(format, bgFill) (Canvas Capture) │
│  - BridgeEvent.ImageSuccess(imageFile, width, height)       │
└─────────────────────────────────────────────────────────────┘
```

#### Komponen yang Akan Dimodifikasi/Dibuat:
1. `com.example.model.OutputMode`: Enum `VIDEOS` dan `IMAGES`.
2. `com.example.model.ImageFormat`: Enum `JPG` dan `PNG`.
3. `com.example.model.SavedMedia`: Model data terpadu untuk media yang dirender (video atau image).
4. `com.example.engine.WebCodecsBridge`: Menambahkan event `ImageSuccess` dan method JavascriptInterface untuk menerima data gambar.
5. `com.example.engine.EngineHtmlBuilder`: Menambahkan `window.renderStillImage(format, bgFill)` dengan dukungan background hitam untuk JPG dan transparansi untuk PNG.
6. `com.example.data.VideoStorageManager`: Menambahkan fungsi simpan dan rename image file (`.jpg` / `.png`) ke folder Download tanpa duplikasi.
7. `com.example.viewmodel.CodeMotionViewModel`: State `outputMode`, `selectedImageFormat`, `lastRenderedImage`, serta fungsi ekspor gambar.
8. `com.example.ui.screens.StudioScreen`: Menambahkan Card Mode Selector di atas Viewport, tombol render dinamis, tombol Export split dengan dropdown format.
9. `com.example.ui.screens.ExportScreen`: Menampilkan preview gambar yang dirender dan kontrol export format saat dalam mode Images.

---

### Verification Plan

#### Automated Tests
- Menjalankan `gradle :app:testDebugUnitTest` untuk memverifikasi seluruh unit test yang ada tetap lulus.
- Menambahkan unit test baru untuk verifikasi nama file export gambar (`.jpg` / `.png`) dan sanitasi nama dari metadata.

#### Manual Verification
1. Beralih mode dari "Videos" ke "Images" di tab Studio via Card Mode Selector: periksa perubahan tombol menjadi "Render Images" dan "Export".
2. Pilih format PNG: klik "Render Images", lalu klik "Export". Periksa bahwa file berekstensi `.png` tersimpan di Download dengan transparansi jika kode transparan.
3. Pilih format JPG: klik "Render Images", lalu klik "Export". Periksa bahwa file berekstensi `.jpg` tersimpan dengan background hitam tanpa transparansi pecah.
4. Periksa bahwa pilihan format (PNG/JPG) tidak kembali ke default saat berpindah tab ke Export atau Metadata.
