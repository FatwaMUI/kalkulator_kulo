pipeline {
    agent any
    
    stages {
        stage('1. Build & Create Docker Image') {
            steps {
                echo 'Membangun aplikasi dan membuat image Docker...'
                sh './gradlew build -x test' // Membuat file JAR
                sh 'docker build -t kalkulator-saya:latest .' // Membuat image dari Dockerfile
            }
        }
        
        stage('2. Run Integration Tests (Simulasi)') {
            steps {
                // Di sini Anda akan menjalankan container aplikasi dan database
                // di dalam jaringan yang sama, lalu menjalankan tes
                // untuk memastikan mereka bisa berkomunikasi.
                // Ini adalah topik yang lebih advance, tapi konsepnya sama.
                echo 'Menjalankan tes integrasi...'
            }
        }

        stage('3. Push to Registry (Simulasi)') {
            steps {
                // Setelah image terbukti bagus, Anda akan mengunggahnya
                // ke sebuah "gudang" image seperti Docker Hub atau ECR.
                echo 'Mendorong image ke registry...'
            }
        }
    }
}