// Jenkinsfile - Versi Final dengan Perbaikan Izin di Setiap Tahap

pipeline {
    agent any

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
                sh 'docker stop app-ci mysql-ci || true'
                sh 'docker rm app-ci mysql-ci || true'
                
                echo "Menjalankan gradle clean..."
                sh './gradlew clean'
            }
        }
        
        stage('Tahap 3: Build Aplikasi (JAR)') {
            steps {
                echo "Memberikan Izin Eksekusi lagi (untuk jaga-jaga)..."
                sh 'chmod +x gradlew'

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
        
        // Tahap 5 tidak kita ubah karena tidak memakai gradlew
        stage('Tahap 5: Jalankan dan Verifikasi') {
            steps {
                script {
                    try {
                        echo "Menjalankan container database..."
                        sh 'docker run -d --name mysql-ci -p 3307:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=calculator_db mysql:8.0'
                        
                        echo "Menunggu database siap... (20 detik)"
                        sleep 20
                        
                        echo "Menjalankan aplikasi dari image..."
                        sh 'docker run -d --name app-ci -p 8080:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" kalkulator-ci:final'
                        
                        echo "Menunggu aplikasi siap... (15 detik)"
                        sleep 15
                        
                        echo "Verifikasi final!"
                        sh 'docker logs app-ci'
                        
                    } finally {
                        echo "Pembersihan akhir..."
                        sh 'docker stop app-ci || true'
                        sh 'docker rm app-ci || true'
                        sh 'docker stop mysql-ci || true'
                        sh 'docker rm mysql-ci || true'
                    }
                }
            }
        }
    }
}