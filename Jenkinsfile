// Jenkinsfile - Versi Profesional untuk Docker-in-Docker

pipeline {
    // Jalankan di agent manapun yang terhubung
    agent any

    // Definisikan variabel di satu tempat
    environment {
        DOCKER_IMAGE_NAME = "kalkulator-kula"
        DOCKER_IMAGE_TAG  = "${env.BUILD_NUMBER}"
        // Nama network untuk isolasi build ini
        DOCKER_NETWORK    = "kalkulator-net"
    }

    stages {
        stage('Tahap 1: Checkout Kode') {
            steps {
                echo "Mengambil kode dari GitHub..."
                git url: 'https://github.com/FatwaMUI/kalkulator_kulo.git', branch: 'main'
            }
        }

        stage('Tahap 2: Build Aplikasi') {
            steps {
                echo "Memberikan Izin Eksekusi pada gradlew (aman untuk Linux container)..."
                // Perintah ini aman karena container Jenkins kita berbasis Linux
                sh 'chmod +x gradlew'

                echo "Menjalankan gradle build..."
                // Build aplikasi, skip test untuk percepat CI
                sh './gradlew build -x test'
            }
        }
        
        stage('Tahap 3: Build Docker Image') {
            steps {
                echo "Membangun image Docker: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
            }
        }
        
        stage('Tahap 4: Setup & Verifikasi di Isolated Network') {
            steps {
                script {
                    // Gunakan blok try-finally untuk memastikan pembersihan selalu berjalan
                    try {
                        echo "Membersihkan environment lama dan membuat network baru..."
                        // || true -> jika gagal (karena tidak ada), jangan hentikan pipeline
                        sh '''
                            docker stop app-kalkulator mysql-db || true
                            docker rm app-kalkulator mysql-db || true
                            docker network rm ${DOCKER_NETWORK} || true
                            docker network create ${DOCKER_NETWORK}
                        '''

                        echo "Menjalankan container database di network: ${DOCKER_NETWORK}..."
                        // Gunakan --network dan port internal (3306) sudah cukup
                        sh 'docker run -d --name mysql-db --network ${DOCKER_NETWORK} -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=calculator_db mysql:8.0'
                        
                        echo "Menjalankan aplikasi di network: ${DOCKER_NETWORK}..."
                        // PERUBAHAN PENTING:
                        // 1. Gunakan --network
                        // 2. Hubungkan ke 'mysql-db:3306'. Tidak perlu 'host.docker.internal' dan mapping port 3307 lagi.
                        sh 'docker run -d --name app-kalkulator --network ${DOCKER_NETWORK} -p 8080:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}'
                        
                        echo "Menunggu aplikasi siap (health check)..."
                        // PERUBAHAN PENTING:
                        // Gunakan fitur 'retry' dari Jenkins, lebih bersih daripada script 'timeout'.
                        // Cek ke http://localhost:8080 karena port 8080 di-mapping ke host Jenkins.
                        retry(count: 15) { // Coba 15 kali
                            sh 'sleep 4' // Jeda 4 detik antar percobaan
                            echo "Mencoba cek health aplikasi..."
                            // Curl dari dalam Jenkins Container ke port yang dipublish di host.
                            sh 'curl --fail --silent http://localhost:8080/actuator/health'
                        }
                        
                        echo "Verifikasi final! Aplikasi berjalan dengan baik."
                        sh 'docker logs app-kalkulator'
                        
                    } finally {
                        // Tahap pembersihan ini SANGAT PENTING
                        echo "Pembersihan akhir environment..."
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