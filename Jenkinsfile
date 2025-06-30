// Jenkinsfile - Versi Rekomendasi dengan Perbaikan & Best Practices

pipeline {
    agent any

    environment {
        // Definisikan nama image dan tag di satu tempat biar gampang diubah
        DOCKER_IMAGE_NAME = "kalkulator-kula"
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Tahap 1: Checkout Kode') {
            steps {
                echo "Mengambil kode dari GitHub..."
                git url: 'https://github.com/FatwaMUI/kalkulator_kulo.git', branch: 'main'
            }
        }

        stage('Tahap 2: Persiapan & Build') {
            steps {
                echo "Memberikan Izin Eksekusi pada gradlew..."
                sh 'chmod +x gradlew'
                
                echo "Membersihkan environment lama..."
                // Menggunakan script block agar bisa digabung dalam satu shell
                sh '''
                    docker stop app-kalkulator mysql-db  true
                    docker rm app-kalkulator mysql-db  true
                '''
                
                echo "Menjalankan gradle clean dan build..."
                sh './gradlew build -x test' // clean dan build bisa digabung
            }
        }
        
        stage('Tahap 3: Build Docker Image') {
            steps {
                echo "Membangun image Docker dengan tag dinamis: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
            }
        }
        
        stage('Tahap 4: Jalankan dan Verifikasi') {
            steps {
                script {
                    try {
                        echo "Menjalankan container database..."
                        sh 'docker run -d --name mysql-db -p 3307:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=calculator_db mysql:8.0'
                        
                        echo "Menunggu database siap (health check)..."
                        // Timeout 60 detik, cek setiap 2 detik
                        sh '''
                          timeout 60s bash -c 'while ! docker exec mysql-db mysqladmin ping -h"127.0.0.1" --silent; do echo "Menunggu MySQL..."; sleep 2; done'
                        '''
                        
                        echo "Menjalankan aplikasi dari image..."
                        sh 'docker run -d --name app-kalkulator -p 8080:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}'
                        
                        echo "Menunggu aplikasi siap (health check)..."
                        // Timeout 60 detik, cek setiap 2 detik (asumsi ada actuator/health)
                        sh '''
                          timeout 60s bash -c 'while ! curl --fail --silent http://localhost:8080/actuator/health > /dev/null; do echo "Menunggu aplikasi Spring..."; sleep 2; done'
                        '''
                        
                        echo "Verifikasi final! Aplikasi berjalan."
                        sh 'docker logs app-kalkulator'
                        
                    } finally {
                        echo "Pembersihan akhir environment..."
                        sh '''
                            docker stop app-kalkulator mysql-db  true
                            docker rm app-kalkulator mysql-db  true
                        '''
                    }
                }
            }
        }
    }
}