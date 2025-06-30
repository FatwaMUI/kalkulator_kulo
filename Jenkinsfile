// Jenkinsfile - Versi Final Profesional dengan Agen Docker yang Benar

pipeline {
    // Kita definisikan agen secara global, tapi ini adalah agen DOCKER.
    // Semua 'steps' akan berjalan di dalam container ini secara default.
    agent {
        docker {
            image 'gradle:7.6.4-jdk17'
            // Kita berikan remote control Docker dan jalankan sebagai root
            args '-v /var/run/docker.sock:/var/run/docker.sock -u root' 
        }
    }

    stages {
        
        stage('Tahap 1: Build & Buat Docker Image') {
            steps {
                echo 'Berjalan di dalam container Gradle JDK 17...'
                
                echo 'Langkah 1.1: Membersihkan dan Membangun Aplikasi...'
                // Perintah ini sekarang dijalankan oleh Java 17, jadi PASTI BISA.
                sh './gradlew clean build -x test' 
                
                echo 'Langkah 1.2: Membangun Image Docker...'
                // Perintah ini juga dijalankan dari dalam container Gradle.
                sh 'docker build -t kalkulator-kula:final .'
            }
        }
        
        stage('Tahap 2: Jalankan & Verifikasi') {
            // Kita bisa menjalankan sisa pipeline di sini
            steps {
                script {
                    try {
                        echo "Langkah 2.1: Menyiapkan Lingkungan Uji Coba..."
                        sh 'docker run -d --name mysql-ci -p 3307:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=calculator_db mysql:8.0'
                        
                        echo "Menunggu database siap... (20 detik)"
                        sleep 20
                        
                        echo "Langkah 2.2: Menjalankan Aplikasi dari Image yang Baru Dibuat..."
                        // Kita pakai host.docker.internal karena Jenkins sekarang berkomunikasi
                        // dari dalam sebuah container.
                        sh 'docker run -d --name app-ci -p 8080:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/calculator_db?allowPublicKeyRetrieval=true&useSSL=false" kalkulator-kula:final'
                        
                        echo "Menunggu aplikasi siap... (15 detik)"
                        sleep 15
                        
                        echo "Langkah 2.3: Verifikasi Akhir!"
                        // Kita bisa tes langsung karena Jenkins punya akses ke docker.sock
                        sh "docker exec app-ci curl -s --fail http://localhost:8080/add?a=1&b=1"
                        
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