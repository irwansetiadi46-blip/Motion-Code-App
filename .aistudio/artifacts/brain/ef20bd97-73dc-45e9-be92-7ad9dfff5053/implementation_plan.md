# Rencana Implementasi (Revisi): Standarisasi CSV Shutterstock, Field Category, & Auto Rename MP4

Berdasarkan arahan pengguna, pembuatan CSV Shutterstock akan mengambil kategori langsung dari kolom Category hasil generate AI (tanpa kategori default kaku), dan file video MP4 akan otomatis di-rename menggunakan hasil generate `filename` metadata saat generate selesai, serta saat download file MP4 default filename menggunakan nama tersebut atau fallback `Vid-WM.mp4`.

---

## Proposed Changes

### 1. Data Model & Penyimpanan (`VideoMetadata.kt` & `MetadataRepository.kt`)
- Menambahkan field `categories: String = ""` ke dalam data class `VideoMetadata`.
- Memperbarui serialization/deserialization JSON di `MetadataRepository` agar field kategori tersimpan dan terbaca dengan sempurna.

### 2. Layanan AI Gemini (`GeminiMetadataService.kt`)
- Memperbarui prompt Gemini AI agar mengembalikan objek JSON dengan field `"categories"` yang berisi 1 atau 2 kategori dari daftar resmi video Shutterstock (misalnya: `Technology, Backgrounds/Textures`).
- Memperbarui parser respons JSON untuk mengekstrak `"categories"` secara dinamis.
- Di `generateCodeAwareMetadata`, isi field `categories` sesuai hasil deteksi visual kode canvas tanpa hardcode.

### 3. Generator CSV Shutterstock (`CsvExporter.kt`)
- Menggunakan tepat 4 kolom standar: **`Filename,Description,Keywords,Categories`**.
- Kolom `Categories` mengambil langsung nilai dari `item.categories` yang digenerate AI (tanpa fallback default kaku).
- Kolom `Filename`: jika `item.filename` terisi gunakan `<filename>.mp4`, jika kosong gunakan default `"Vid-WM.mp4"`.
- Kolom `Description`: maksimal 200 karakter.
- Kolom `Keywords`: maksimal 50 kata kunci dipisahkan koma.

### 4. Tampilan Antarmuka Metadata (`StockMetadataCard.kt` & `MetadataScreen.kt`)
- Menambahkan input/tampilan field **Category** di tab **Export** (`StockMetadataCard.kt`) di bawah Description dan di atas Keywords.
- Menambahkan baris **Category** pada accordion detail item di tab **Metadata** (`MetadataScreen.kt`) beserta tombol copy dan edit.

### 5. Auto Rename File MP4 & Download Filename (`CodeMotionViewModel.kt` & `VideoStorageManager.kt`)
- **Auto Rename saat Selesai Generate Metadata**:
  - Saat `generateMetadataFromEditor()` berhasil mendapatkan metadata dengan `filename` baru:
  - Jika terdapat video yang sedang aktif/terpilih (`selectedVideoForPlayback` atau `lastRenderedVideo`), lakukan proses rename fisik file MP4 di disk menjadi `<cleanFilename>.mp4`, dan perbarui record di `VideoStorageManager` / `_savedVideos`.
- **Default Filename saat Download MP4**:
  - Saat `downloadVideo(...)` dipanggil, gunakan nama file dari metadata aktif/video (`<metadataFilename>.mp4`).
  - Jika kolom filename di metadata kosong atau belum di-generate, gunakan default filename **`Vid-WM.mp4`** (misal `Vid-WM_1080p.mp4`).
- **Initial Render Output**:
  - Jika video baru dirender dan belum di-generate metadata, gunakan nama dasar `Vid-WM.mp4`.

---

## Verification Plan

### Automated Tests
1. **Unit Tests**:
   - Menjalankan `gradle :app:testDebugUnitTest`.
   - Menguji ekspor CSV Shutterstock memastikan kolom `Categories` murni mengambil dari `metadata.categories` dan header tepat 4 kolom.
   - Menguji logika penamaan default `"Vid-WM.mp4"` saat metadata filename kosong.

### Manual / Integration Verification
1. Verifikasi ekspor CSV Shutterstock tidak lagi menyertakan kolom yang tidak perlu, dan kategori berasal dari hasil generate AI.
2. Klik tombol **GENERATE**: periksa bahwa file MP4 otomatis ter-rename sesuai filename metadata.
3. Klik tombol **Download MP4**: periksa bahwa nama file yang tersimpan ke folder Download perangkat menggunakan filename metadata tersebut, atau `Vid-WM.mp4` jika belum di-generate.
