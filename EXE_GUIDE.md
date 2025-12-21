# Panduan Membuat Game Menjadi .EXE (Executable)

Karena Anda membutuhkan file `.exe` agar lebih mudah dijalankan oleh client, kita akan menggunakan tool bawaan Java bernama `jpackage`.

## Prasyarat
Pastikan Java Development Kit (JDK) yang Anda gunakan minimal versi 14 (Project ini menggunakan Java 17, jadi sudah aman).

## Langkah-langkah

### 1. Build Project menjadi JAR
Buka terminal (CMD/PowerShell) di folder `Frontend`, lalu jalankan:

```bash
./gradlew lwjgl3:dist
```
*(Tunggu sampai muncul "BUILD SUCCESSFUL")*

### 2. Generate .EXE menggunakan jpackage
Masih di terminal folder `Frontend`, jalankan perintah berikut (bisa dicopy-paste satu baris penuh):

```bash
jpackage --name LabuBoom --input lwjgl3/build/libs --main-jar lwjgl3-1.0.jar --main-class com.labubushooter.frontend.lwjgl3.Lwjgl3Launcher --type app-image --dest release --win-console
```

**Penjelasan Perintah:**
- `--name LabuBoom`: Nama file exe nanti (`LabuBoom.exe`).
- `--input ...`: Folder tempat file JAR hasil build berada.
- `--main-jar ...`: Nama file JAR utamanya.
- `--type app-image`: Membuat folder portable yang berisi `.exe` dan runtime Java (jadi user tidak perlu install Java lagi).
- `--dest release`: Hasilnya akan disimpan di folder `release`.
- `--win-console`: (Opsional) Menampilkan window hitam console saat game jalan. Berguna untuk melihat log koneksi ke backend. Hapus opsi ini jika ingin game berjalan tanpa console.

### 3. Hasil Akhir
1. Buka folder `Frontend/release/LabuBoom`.
2. Anda akan menemukan file `LabuBoom.exe`.
3. Folder ini bersifat **Portable**. Artinya, Anda harus meng-copy **satu folder penuh** `LabuBoom` tersebut jika ingin membagikannya ke teman. Jangan hanya file `.exe`-nya saja.

### 4. Konfigurasi Cloud
Agar `.exe` ini terhubung ke backend Render (Cloud), lakukan hal berikut:

1. Masuk ke dalam folder `Frontend/release/LabuBoom`.
2. Buat file baru bernama `config.properties`.
3. Isi dengan:
   ```properties
   api.url=https://labuboom-backend.onrender.com/api/players
   ```
   *(Ganti URL dengan URL backend Render Anda)*.

Sekarang, Anda tinggal zip folder `LabuBoom` tersebut dan kirim ke teman. Mereka tinggal klik `LabuBoom.exe` untuk main.
