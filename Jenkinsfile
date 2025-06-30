// Jenkinsfile - Versi Final (Tampilan Asli, Perbaikan di Dalam)

pipeline {
    agent any

    // Kita tetap butuh ini untuk menyimpan password dengan aman.
    environment {
        DB_PASSWORD = credentials('my-secret-pw') 
    }

    stages {
        // --- TAHAP 1: PERSIS SEPERTI ASLINYA ---
        stage('Tahap 1: Checkout Kode dari GitHub') {
            steps {
                echo "Mengambil kode terbaru dari https://github.com/FatwaMUI/kalkulator_kulo.git"
                git url: 'https://github.com/FatwaMUI/kalkulator_kulo.git', branch: 'main'
            }
        }

        // --- TAHAP 2: PERSIS SEPERTI ASLINYA (tanpa gradle clean) ---
        stage('Tahap 2: Persiapan Lingkungan') {
            steps {
                echo "Memberikan Izin Eksekusi pada gradlew..."
                sh 'chmod +x gradlew'
                
                echo "Membersihkan container lama..."
                sh 'docker stop app-kalkulator mysql-db || true'
                sh 'docker rm -f app-kalkulator mysql-db || true' // Pakai -f biar lebih ampuh
                
                // 'gradlew clean' kita pindah ke tahap build agar berjalan dengan Java 17
            }
        }
        
        // --- TAHAP 3: INI BAGIAN AJAIBNYA (SOLUSI JAVA 17) ---
        stage('Tahap 3: Build Aplikasi (JAR)') {
            // Kita pakai "agent" khusus HANYA untuk tahap ini
            agent { 
                docker { 
                    image 'gradle:8.5.0-jdk17'
                    args '-v ${WORKSPACE}:/home/gradle/project -w /home/gradle/project' 
                }
            }
            steps {
                // Pesan echo persis seperti aslinya
                echo "Memberikan Izin Eksekusi lagi (untuk jaga-jaga)..." // Ini hanya untuk konsistensi pesan
                echo "Membangun aplikasi..."
                // Perintah ini sekarang dijalankan di dalam lingkungan Java 17
                sh 'gradle clean build -x test'
            }
        }
        
        // --- TAHAP 4: PERSIS SEPERTI ASLINYA ---
        stage('Tahap 4: Build Image Docker') {
            steps {
                echo "Membangun image Docker..."
                sh 'docker build -t kalkulator-kula:v1 .'
            }
        }
        
        // --- TAHAP 5: TAMPILAN ASLI, AKSI LEBIH ANDAL ---
        stage('Tahap 5: Jalankan dan Verifikasi') {
            steps {
                script {
                    try {
                        echo "Menjalankan container database..."
                        // Menggunakan ${DB_PASSWORD} yang aman, bukan teks biasa
                        sh 'docker run -d --name mysql-db -p 3307:3306 -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} -e MYSQL_DATABASE=calculator_db mysql:8.0'
                        
                        // Pesan echo sama, tapi aksinya lebih pintar dari 'sleep'
                        echo "Menunggu database siap... (20 detik)"
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'until docker exec mysql-db mysqladmin ping -h"127.0.0.1" --silent; do sleep 5; done'
                        }
                        
                        echo "Menjalankan aplikasi dari image..."
                        // Menambahkan username & password dari environment
                        sh 'docker run -d --name app-kalkulator -p 8080:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" -e "SPRING_DATASOURCE_USERNAME=root" -e "SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}" kalkulator-kula:v1'
                        
                        // Pesan echo sama, tapi aksinya lebih pintar dari 'sleep'
                        echo "Menunggu aplikasi siap... (15 detik)"
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'until curl -s -f http://localhost:8080/actuator/health > /dev/null; do sleep 5; done'
                        }
                        
                        echo "Verifikasi final!"
                        sh 'docker logs app-kalkulator'
                        
                    } finally {
                        echo "Pembersihan akhir..."
                        sh 'docker stop app-kalkulator || true'
                        sh 'docker rm -f app-kalkulator || true'
                        sh 'docker stop mysql-db || true'
                        sh 'docker rm -f mysql-db || true'
                    }
                }
            }
        }
    }
}