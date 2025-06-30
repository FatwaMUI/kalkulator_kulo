// Jenkinsfile - Versi Final Hybrid (Struktur Awal + Perbaikan)

pipeline {
    // KEMBALI KE AGENT ANY: Sesuai dengan setup Jenkins-mu yang sudah bisa jalan.
    agent any

    // SOLUSI JDK 17: Meminta Jenkins untuk menyediakan JDK-17 yang sudah dikonfigurasi.
    tools {
        jdk 'JDK-17' // Nama ini HARUS SAMA dengan yang kamu buat di Global Tool Configuration.
    }

    // TETAP PAKAI CREDENTIALS: Ini praktik terbaik yang aman.
    environment {
        DB_PASSWORD = credentials('mysql-root-password-id') // Pastikan ID ini sudah dibuat di Jenkins.
        DOCKER_IMAGE_NAME = 'kalkulator-ci:final'
        APP_CONTAINER_NAME = 'app-ci'
        DB_CONTAINER_NAME = 'mysql-ci'
    }

    stages {
        stage('Tahap 1: Checkout dan Persiapan') {
            steps {
                echo "Mengambil kode terbaru dari GitHub..."
                git url: 'https://github.com/FatwaMUI/kalkulator_kulo.git', branch: 'main'

                echo "Memberikan Izin Eksekusi pada gradlew..."
                sh 'chmod +x gradlew'

                echo "Membersihkan container dari build sebelumnya..."
                sh "docker stop ${APP_CONTAINER_NAME} ${DB_CONTAINER_NAME} || true"
                sh "docker rm -f ${APP_CONTAINER_NAME} ${DB_CONTAINER_NAME} || true"
            }
        }
        
        stage('Tahap 2: Build Aplikasi (JAR)') {
            steps {
                echo "Memverifikasi versi Java yang digunakan oleh pipeline..."
                sh 'java -version' // Ini akan menampilkan output Java 17

                echo "Menjalankan gradle clean dan build..."
                // Menggunakan ./gradlew sesuai struktur awalmu.
                sh './gradlew clean build -x test'
            }
        }
        
        stage('Tahap 3: Build Image Docker') {
            steps {
                echo "Membangun image Docker: ${DOCKER_IMAGE_NAME}"
                sh "docker build -t ${DOCKER_IMAGE_NAME} ."
            }
        }
        
        stage('Tahap 4: Jalankan dan Verifikasi (Tanpa `sleep`)') {
            steps {
                script {
                    try {
                        echo "Menjalankan container database..."
                        sh """
                            docker run -d --name ${DB_CONTAINER_NAME} -p 3307:3306 \
                            -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} \
                            -e MYSQL_DATABASE=calculator_db mysql:8.0
                        """
                        
                        echo "Menunggu database siap..."
                        timeout(time: 2, unit: 'MINUTES') {
                            // Asumsi Jenkins agent punya 'docker' & 'mysql-client' atau kita bisa pakai docker exec
                            sh 'until docker exec ${DB_CONTAINER_NAME} mysqladmin ping -h"127.0.0.1" -p"3306" --silent; do echo "Menunggu mysql..."; sleep 5; done'
                        }
                        echo "Database siap!"
                        
                        echo "Menjalankan container aplikasi..."
                        sh """
                            docker run -d --name ${APP_CONTAINER_NAME} -p 8080:8080 \
                            -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" \
                            -e "SPRING_DATASOURCE_USERNAME=root" \
                            -e "SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}" \
                            ${DOCKER_IMAGE_NAME}
                        """
                        
                        echo "Menunggu aplikasi siap..."
                        timeout(time: 2, unit: 'MINUTES') {
                            // Asumsi Jenkins agent punya 'curl'
                            sh 'until curl -s -f http://localhost:8080/actuator/health > /dev/null; do echo "Menunggu aplikasi..."; sleep 5; done'
                        }
                        echo "Aplikasi siap dan berjalan!"

                        echo "Verifikasi final..."
                        sh 'curl -s http://localhost:8080/actuator/health'
                        sh "docker logs ${APP_CONTAINER_NAME}"
                        
                    } finally {
                        echo "Pembersihan akhir..."
                        sh "docker stop ${APP_CONTAINER_NAME} ${DB_CONTAINER_NAME} || true"
                        sh "docker rm -f ${APP_CONTAINER_NAME} ${DB_CONTAINER_NAME} || true"
                    }
                }
            }
        }
    }
}