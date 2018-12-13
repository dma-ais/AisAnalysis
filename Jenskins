pipeline {
    agent any

    tools {
        maven 'M3.3.9'
    }

    triggers {
        pollSCM('* * * *')
    }

    stages {
        stage('checkout') {
            steps {
                checkout scm
            }
        }

        stage('build') {
            steps {
                withMaven() {
                    sh 'mvn -e -DskipTests -DincludeSrcJavadocs clean source:jar install'
                }
            }
        }
    }

//    post {
//        failure {
//            // notify users when the Pipeline fails
//            mail to: 'obo@dma.dk',
//                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
//                    body: "Something is wrong with ${env.BUILD_URL}"
//        }
//    }
}
