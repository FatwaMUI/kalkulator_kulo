// Jenkinsfile - Versi Final yang Disempurnakan

pipeline {
    // Lebih baik menentukan agent dengan label untuk memastikan tools (docker, jdk) tersedia
    // agent { label 'docker-node' } 
    agent any 

    // (BARU) Menyimpan kredensial database dengan aman
    environment {
        DB_PASSWORD = credentials('mysql-root-password-id') // Ganti 'mysql-root-password-id' dengan ID kredensial Anda di Jenkins
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
                echo "Memberikan Izin Eksekusi pada gradlew (cukup sekali)..."
                sh 'chmod +x gradlew'
                
                echo "Membersihkan container lama..."
                sh 'docker stop app-ci mysql-ci || true'
                sh 'docker rm app-ci mysql-ci || true'
                
                echo "Menjalankan gradle clean..."
                sh './gradlew clean'
            }
        }
        
        stage('Tahap 3: Build Aplikasi (JAR)') {
            steps {
                // Perintah chmod yang berulang sudah dihapus
                echo "Membangun aplikasi..."
                sh './gradlew build -x test'
            }
        }
        
        stage('Tahap 4: Build Image Docker') {
            steps {
                echo "Membangun image Docker..."
                sh 'docker build -t kalkulator-ci:final .'
            }
        }
        
        stage('Tahap 5: Jalankan dan Verifikasi (Tanpa `sleep`)') {
            steps {
                script {
                    try {
                        echo "Menjalankan container database..."
                        sh "docker run -d --name mysql-ci -p 3307:3306 -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} -e MYSQL_DATABASE=calculator_db mysql:8.0"
                        
                        // (BARU) Health check untuk database, bukan sleep
                        echo "Menunggu database siap..."
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'until docker exec mysql-ci mysqladmin ping -h"127.0.0.1" --silent; do sleep 5; done'
                        }
                        echo "Database siap!"
                        
                        echo "Menjalankan aplikasi dari image..."
                        sh "docker run -d --name app-ci -p 8080:8080 -e 'SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false' -e 'SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}' kalkulator-ci:final"
                        
                        // (BARU) Health check untuk aplikasi, bukan sleep
                        echo "Menunggu aplikasi siap..."
                        timeout(time: 2, unit: 'MINUTES') {
                            // Asumsi Anda punya actuator/health endpoint. Jika tidak, bisa cek log.
                            sh 'until curl -s -f http://localhost:8080/actuator/health > /dev/null; do sleep 5; done'
                        }
                        echo "Aplikasi siap!"

                        echo "Verifikasi final dengan melihat log..."
                        sh 'docker logs app-ci'
                        
                    } finally {
                        echo "Pembersihan akhir..."
                        sh 'docker stop app-ci mysql-ci || true'
                        sh 'docker rm -f app-ci mysql-ci || true' // -f untuk force remove jika stop gagal
                    }
                }
            }
        }
    }
}