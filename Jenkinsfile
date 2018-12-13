pipeline {
    agent any

    tools {
        maven 'M3.3.9'
    }

    triggers {
        pollSCM('*/2 * * * *')
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

    post {
        success {
            sh 'curl --data "build=true" -X POST https://registry.hub.docker.com/u/dmadk/ais-store-cli/trigger/a995ad1e-4a4c-11e4-a6f5-dafbef59e66b/'
            sh 'curl --data "build=true" -X POST https://registry.hub.docker.com/u/dmadk/ais-filedump/trigger/f0eb0638-6378-11e4-aea0-d22c3a4d29af/'
        }
    }
}
