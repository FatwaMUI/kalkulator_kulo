pipeline {
    // Kita akan menggunakan 'agent dockerfile'. 
    // Ini menyuruh Jenkins untuk menjalankan SEMUA tahap di dalam container
    // yang dibuat dari Dockerfile kita.
    agent {
        dockerfile true
    }

    stages {
        stage('Build and Test inside Docker') {
            steps {
                echo 'Sekarang kita berada di dalam container yang sudah punya Gradle dan Java 17!'
                
                // Karena kita sudah di dalam container yang dibuat dari 'FROM gradle:7.6.4-jdk17',
                // kita bisa langsung menjalankan perintah Gradle.
                // Tidak perlu lagi install JDK di Jenkins.
                sh './gradlew build --no-daemon'
                
                echo 'Build berhasil!'
            }
        }
        
        // Catatan: Setelah ini berhasil, Anda bisa menambahkan stage lain
        // untuk benar-benar membuat image aplikasi final Anda,
        // tapi untuk sekarang, tujuan kita adalah melihat 'Build and Test' ini BERHASIL.
    }
}