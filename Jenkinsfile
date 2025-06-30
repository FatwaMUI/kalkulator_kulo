pipeline {
    // MENDEFINISIKAN ALAT YANG DIBUTUHKAN DI PALING ATAS
    agent any
    
    tools {
        // Memberitahu Jenkins untuk menggunakan konfigurasi JDK bernama 'JDK17'
        // yang sudah kita atur di Global Tool Configuration.
        jdk 'JDK17' 
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
                echo 'Kode berhasil diambil dari GitHub!'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Menjalankan ./gradlew build...'
                // Sekarang Jenkins akan menjalankan ini menggunakan Java 17
                sh './gradlew build --no-daemon -x test'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Membangun image Docker...'
                // Dockerfile kita sudah benar, jadi ini akan berjalan lancar
                sh 'docker build -t kalkulator-saya:jenkins-build .'
            }
        }
    }
}