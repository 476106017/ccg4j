pipeline {
  agent any
  stages {
    stage('run') {
      steps {
        withGradle() {
          sh 'gradle bootRun'
        }

      }
    }

  }
}