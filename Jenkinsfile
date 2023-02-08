pipeline {
  agent any
  stages {
    stage('run') {
      steps {
        withGradle() {
          sh '''source /etc/profile
gradle bootRun
'''
        }

      }
    }

  }
}