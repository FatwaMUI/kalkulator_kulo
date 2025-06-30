// Jenkinsfile - Versi Final dengan Perbaikan Total

pipeline {
    // SOLUSI UTAMA: Menjalankan pipeline di dalam container Docker yang sudah punya JDK 17 dan Gradle.
    // Ini menyelesaikan masalah "invalid source release: 17" secara tuntas.
    agent {
        docker {
            image 'gradle:8.5.0-jdk17' // Image resmi dari Gradle dengan JDK 17 di dalamnya.
            args '-v /var/run/docker.sock:/var/run/docker.sock -v "$HOME/.m2":/root/.m2' // Agar bisa menjalankan Docker & caching
        }
    }

    // Menggunakan Jenkins Credentials untuk menyimpan password dengan aman.
    environment {
        DB_PASSWORD = credentials('mysql-root-password-id')
        // Nama image dan container agar mudah diubah di satu tempat
        DOCKER_IMAGE_NAME = 'kalkulator-ci:final'
        APP_CONTAINER_NAME = 'app-ci'
        DB_CONTAINER_NAME = 'mysql-ci'
    }

    stages {
        stage('Tahap 1: Persiapan dan Pembersihan') {
            steps {
                echo "Membersihkan container dari build sebelumnya..."
                // Menggunakan variabel environment
                sh "docker stop ${APP_CONTAINER_NAME} ${DB_CONTAINER_NAME} || true"
                sh "docker rm -f ${APP_CONTAINER_NAME} ${DB_CONTAINER_NAME} || true"
            }
        }

        stage('Tahap 2: Checkout Kode dari GitHub') {
            steps {
                echo "Mengambil kode terbaru dari https://github.com/FatwaMUI/kalkulator_kulo.git"
                // 'checkout scm' adalah cara standar untuk mengambil kode yang dikonfigurasi di job Jenkins
                checkout scm
            }
        }
        
        stage('Tahap 3: Build Aplikasi (JAR)') {
            steps {
                echo "Membangun aplikasi dengan Gradle..."
                // Tidak perlu './gradlew' atau 'chmod', karena 'gradle' sudah tersedia di dalam image Docker agent.
                // 'clean' dan 'build' digabung dalam satu perintah.
                sh 'gradle clean build -x test'
            }
        }
        
        stage('Tahap 4: Build Image Docker') {
            steps {
                echo "Membangun image Docker: ${DOCKER_IMAGE_NAME}"
                sh "docker build -t ${DOCKER_IMAGE_NAME} ."
            }
        }
        
        stage('Tahap 5: Jalankan dan Verifikasi (Tanpa `sleep`)') {
            steps {
                script {
                    try {
                        echo "Menjalankan container database: ${DB_CONTAINER_NAME}"
                        sh """
                            docker run -d --name ${DB_CONTAINER_NAME} -p 3307:3306 \
                            -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} \
                            -e MYSQL_DATABASE=calculator_db mysql:8.0
                        """
                        
                        // HEALTH CHECK DATABASE: Menunggu sampai DB benar-benar siap.
                        echo "Menunggu database siap..."
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'until docker exec ${DB_CONTAINER_NAME} mysqladmin ping -h"127.0.0.1" -p"3306" --silent; do echo "Menunggu mysql..."; sleep 5; done'
                        }
                        echo "Database siap!"
                        
                        echo "Menjalankan container aplikasi: ${APP_CONTAINER_NAME}"
                        sh """
                            docker run -d --name ${APP_CONTAINER_NAME} -p 8080:8080 \
                            -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" \
                            -e "SPRING_DATASOURCE_USERNAME=root" \
                            -e "SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}" \
                            ${DOCKER_IMAGE_NAME}
                        """
                        
                        // HEALTH CHECK APLIKASI: Menunggu sampai aplikasi merespons.
                        echo "Menunggu aplikasi siap..."
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'until curl -s -f http://localhost:8080/actuator/health > /dev/null; do echo "Menunggu aplikasi..."; sleep 5; done'
                        }
                        echo "Aplikasi siap dan berjalan!"

                        echo "Verifikasi final dengan tes koneksi dan log..."
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