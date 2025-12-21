# Panduan Deployment Backend ke Cloud (Render)

Panduan ini akan membantu Anda men-deploy backend Spring Boot ke Render.com dan mengkonfigurasi Frontend agar bisa terhubung ke backend cloud tersebut.

## Prasyarat
1. Akun GitHub (untuk menyimpan kode backend).
2. Akun [Render.com](https://render.com) (bisa login pakai GitHub).
3. Database Neon.tech yang sudah Anda miliki.

## Langkah 1: Push Kode ke GitHub
Pastikan kode backend Anda sudah ada di repository GitHub. Jika belum:
1. Buat repository baru di GitHub.
2. Push folder `Backend` (atau root project) ke repository tersebut.

## Langkah 2: Deploy Backend ke Render
1. Login ke Dashboard Render.
2. Klik tombol **New +** dan pilih **Web Service**.
3. Pilih **Build and deploy from a Git repository**.
4. Hubungkan akun GitHub Anda dan pilih repository project ini.
5. Konfigurasi Web Service:
   - **Name**: `labuboom-backend` (atau nama lain yang Anda suka).
   - **Region**: Pilih yang terdekat (misal: Singapore).
   - **Branch**: `main` (atau branch tempat kode Anda berada).
   - **Root Directory**: `Backend` (PENTING: karena Dockerfile ada di dalam folder Backend).
   - **Runtime**: Pilih **Docker**.
   - **Instance Type**: Free.

6. **Environment Variables**:
   Scroll ke bawah ke bagian "Environment Variables" dan tambahkan variable berikut:
   
   | Key | Value |
   |-----|-------|
   | `SPRING_DATASOURCE_URL` | Masukkan URL koneksi Neon Anda (contoh: `jdbc:postgresql://ep-flat...aws.neon.tech/neondb?user=...&password=...&sslmode=require`) |
   | `PORT` | `8080` |

   *Catatan: URL Neon bisa didapat dari dashboard Neon.tech. Pastikan formatnya `jdbc:postgresql://...`*

7. Klik **Create Web Service**.
8. Tunggu proses build dan deploy selesai. Jika berhasil, Anda akan mendapatkan URL backend, contoh: `https://labuboom-backend.onrender.com`.

## Langkah 3: Konfigurasi Frontend
Agar frontend (game) bisa terhubung ke backend cloud, Anda tidak perlu mengubah kode Java lagi. Cukup gunakan file konfigurasi.

1. Build ulang frontend Anda menjadi file JAR (jika belum):
   ```bash
   cd Frontend
   ./gradlew desktop:dist
   ```
   File JAR akan ada di `Frontend/desktop/build/libs/desktop-1.0.jar`.

2. Buat file bernama `config.properties` di folder yang sama dengan file JAR game (atau di folder root project saat menjalankan dari IDE).

3. Isi file `config.properties` dengan URL backend cloud Anda:
   ```properties
   api.url=https://labuboom-backend.onrender.com/api/players
   ```
   *(Ganti URL di atas dengan URL asli dari Render)*

4. Jalankan game. Game akan otomatis membaca URL dari file tersebut.

## Troubleshooting
- **Backend Error**: Cek tab "Logs" di dashboard Render untuk melihat error log Spring Boot.
- **Koneksi Gagal**: Pastikan URL di `config.properties` benar dan backend di Render sudah status "Live".
- **Database Error**: Pastikan connection string Neon benar dan database masih aktif.

## Catatan Penting
- Render versi Free akan "tidur" (spin down) jika tidak diakses selama 15 menit. Request pertama setelah tidur akan memakan waktu sekitar 50 detik (cold start).
- Frontend sudah dilengkapi timeout 10 detik, jadi mungkin perlu mencoba login 2x jika backend sedang bangun tidur.
