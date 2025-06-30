# --- TAHAP 1: BUILD APLIKASI ---
# Kita menggunakan image resmi Gradle dengan JDK 17 untuk membangun proyek.
# Kita beri nama tahap ini "builder".
FROM gradle:7.6.4-jdk17 AS builder

# Membuat direktori kerja di dalam container
WORKDIR /app

# Menyalin seluruh isi proyek kita ke dalam container
COPY . .

# Menjalankan perintah Gradle untuk membangun file .jar yang bisa dieksekusi.
# -x test berarti kita lewati testing untuk mempercepat proses build.
RUN ./gradlew build -x test

# --- TAHAP 2: MENJALANKAN APLIKASI ---
# Kita menggunakan image Java yang sangat kecil dan ringan (hanya untuk menjalankan).
FROM eclipse-temurin:17-jre-jammy

# Membuat direktori kerja
WORKDIR /app

# Menyalin HANYA file .jar yang sudah jadi dari tahap "builder" ke sini.
# Ini membuat image akhir kita sangat kecil dan efisien.
COPY --from=builder /app/build/libs/*.jar app.jar

# Memberi tahu Docker bahwa aplikasi kita akan menggunakan port 8080
EXPOSE 8080

# Perintah yang akan dijalankan saat container dinyalakan
ENTRYPOINT ["java", "-jar", "app.jar"]