// Jenkinsfile - Versi Final (Nama Barumu + Mesin yang Benar)

pipeline {
    agent any

    // SOLUSI MASALAH #1: Memanggil credential dengan ID yang benar.
    environment {
        // Ganti 'mysql-root-password-id' dengan ID credential-mu yang sebenarnya.
        DB_PASSWORD = credentials('mysql-root-password-id') 
    }

    stages {
        stage('Tahap 1: Checkout Kode dari GitHub') {
            steps {
                echo "Mengambil kode terbaru dari https://github.com/FatwaMUI/kalkulator_kulo.git"
                git url: 'https://github.com/FatwaMUI/kalkulator_kulo.git', branch: 'main'
            }
        }

        stage('Tahap 2: Persiapan Lingkungan') {
            steps {
                echo "Memberikan Izin Eksekusi pada gradlew..."
                sh 'chmod +x gradlew'
                
                echo "Membersihkan container lama..."
                // Menggunakan nama container barumu
                sh 'docker stop app-kalkulator mysql-db || true'
                sh 'docker rm -f app-kalkulator mysql-db || true'
            }
        }
        
        // SOLUSI MASALAH #2: Mengganti 'agent' dengan perintah 'docker run' yang lebih handal.
        stage('Tahap 3: Build Aplikasi (JAR)') {
            steps {
                echo "Memberikan Izin Eksekusi lagi (untuk jaga-jaga)..."
                echo "Membangun aplikasi menggunakan bungkus Docker Java 17..."
                // Perintah 'brutal force' yang terbukti jalan di lingkunganmu.
                sh 'docker run --rm -u"$(id -u)":"$(id -g)" -v "${WORKSPACE}":/app -w /app gradle:8.5.0-jdk17 ./gradlew clean build -x test'
            }
        }
        
        stage('Tahap 4: Build Image Docker') {
            steps {
                echo "Membangun image Docker..."
                // Menggunakan nama image barumu
                sh 'docker build -t kalkulator-kula:v1 .'
            }
        }
        
        stage('Tahap 5: Jalankan dan Verifikasi') {
            steps {
                script {
                    try {
                        echo "Menjalankan container database..."
                        // Menggunakan nama container barumu
                        sh 'docker run -d --name mysql-db -p 3307:3306 -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} -e MYSQL_DATABASE=calculator_db mysql:8.0'
                        
                        echo "Menunggu database siap... (20 detik)"
                        // Health check untuk nama container barumu
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'until docker exec mysql-db mysqladmin ping -h"127.0.0.1" --silent; do sleep 5; done'
                        }
                        
                        echo "Menjalankan aplikasi dari image..."
                        // Menggunakan nama container & image barumu
                        sh 'docker run -d --name app-kalkulator -p 8080:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" -e "SPRING_DATASOURCE_USERNAME=root" -e "SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}" kalkulator-kula:v1'
                        
                        echo "Menunggu aplikasi siap... (15 detik)"
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'until curl -s -f http://localhost:8080/actuator/health > /dev/null; do sleep 5; done'
                        }
                        
                        echo "Verifikasi final!"
                        // Log untuk nama container barumu
                        sh 'docker logs app-kalkulator'
                        
                    } finally {
                        echo "Pembersihan akhir..."
                        // Cleanup untuk nama container barumu
                        sh 'docker stop app-kalkulator mysql-db || true'
                        sh 'docker rm -f app-kalkulator mysql-db || true'
                    }
                }
            }
        }
    }
}