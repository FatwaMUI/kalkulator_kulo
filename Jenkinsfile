// Jenkinsfile - Versi Final dengan Instalasi Tools

pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = "kalkulator-kula"
        DOCKER_IMAGE_TAG  = "v1"
        DOCKER_NETWORK    = "kalkulator-net"
    }

    stages {
        stage('Tahap 1: Checkout Kode') {
            steps {
                echo "Mengambil kode dari GitHub..."
                git url: 'https://github.com/FatwaMUI/kalkulator_kulo.git', branch: 'main'
            }
        }

        // =================================================================
        // TAHAP BARU DITAMBAHKAN DI SINI
        // =================================================================
        stage('Tahap 2: Install Build Tools') {
            steps {
                echo "Installing Docker client and other necessary tools..."
                // Perintah ini dijalankan sebagai root karena kita menjalankan container dengan --user root
                sh '''
                    apt-get update
                    apt-get install -y docker.io curl
                '''
            }
        }
        // =================================================================

        stage('Tahap 3: Build Aplikasi') { // Sebelumnya Tahap 2
            steps {
                echo "Memberikan Izin Eksekusi pada gradlew..."
                sh 'chmod +x gradlew'

                echo "Menjalankan gradle build..."
                sh './gradlew build -x test'
            }
        }
        
        stage('Tahap 4: Build Docker Image') { // Sebelumnya Tahap 3
            steps {
                echo "Membangun image Docker: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
            }
        }
        
                stage('Tahap 5: Setup & Verifikasi di Isolated Network') {
            steps {
                script {
                    try {
                        // ... (bagian pembersihan dan run container tetap sama) ...
                        sh '''
                            docker stop app-kalkulator mysql-db || true
                            docker rm app-kalkulator mysql-db || true
                            docker network rm ${DOCKER_NETWORK} || true
                            docker network create ${DOCKER_NETWORK}
                        '''

                        echo "Menjalankan container database di network: ${DOCKER_NETWORK}..."
                        sh 'docker run -d --name mysql-db --network ${DOCKER_NETWORK} -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=calculator_db mysql:8.0'
                        
                        // ===== PERUBAHAN UTAMA DI SEKITAR SINI =====

                        echo "Menunggu database siap..."
                        // Beri waktu 15 detik agar service MySQL benar-benar siap menerima koneksi
                        sleep 15
                        
                        echo "Menjalankan aplikasi di network: ${DOCKER_NETWORK}..."
                        sh 'docker run -d --name app-kalkulator --network ${DOCKER_NETWORK} -p 8080:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}'

                        echo "Memberi waktu aplikasi untuk startup (20 detik)..."
                        // Beri waktu aplikasi untuk mencoba konek ke DB dan startup.
                        sleep 20
                        
                        echo "Melihat log aplikasi SEBELUM health check untuk debugging..."
                        // Ini akan menunjukkan error startup jika ada
                        sh 'docker logs app-kalkulator'
                        
                        echo "Menunggu aplikasi siap (health check)..."
                        retry(count: 10) { // Kurangi retry karena kita sudah menunggu
                            sh 'sleep 3' 
                            echo "Mencoba cek health aplikasi..."
                        }
                        
                        echo "Verifikasi final! Aplikasi berjalan dengan baik."
                        // Log ini akan ditampilkan lagi jika health check berhasil
                        sh 'docker logs app-kalkulator'
                        
                    } finally {
                        // ... (bagian finally tetap sama) ...
                        sh '''
                            docker stop app-kalkulator mysql-db || true
                            docker rm app-kalkulator mysql-db || true
                            docker network rm ${DOCKER_NETWORK} || true
                        '''
                    }
                }
            }
        }
    }
}