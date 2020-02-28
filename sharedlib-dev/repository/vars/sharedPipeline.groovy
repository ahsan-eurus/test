def call(){
    pipeline {
        agent any
        options {
            disableConcurrentBuilds()
        }
        stages {
            stage('Checkout') {
                steps {
                    script {
                        echo "Hello World!"
                    }
                }
            }
        }
    }
}
