pipelineJob('ICC_master') {

  logRotator {
      numToKeep(100)
  }
  triggers {
      gitHubPushTrigger()
  }

  parameters {
      stringParam('EMAIL_RECIPIENTS', '', 'THIS IS USED FOR FINDBUGS REPORT SUBMISSION')
      stringParam('RELEASE_NUMBER', 'icc-master', '')
      stringParam('GIT_BRANCH_NAME', 'master', 'ENTER GITHUB BRANCH NAME')
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
                url('https://github.com/bam-labs/ICC.git')
                credentials('github-token')
            }
            branches('*/${GIT_BRANCH_NAME}')
        }
      }
      scriptPath('Jenkinsfile')
      lightweight(false)
    }
  }
}
