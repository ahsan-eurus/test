pipelineJob('Backend_docker_base') {

  logRotator {
      numToKeep(100)
  }
  parameters {
      stringParam('IMAGE_TAG', 'base', '')
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
                url('https://github.com/bam-labs/BAM_Services.git')
                credentials('github-token')
            }
            branches('ASIMOV-15')
        }
      }
      scriptPath('siq-docker-base/Jenkinsfile')
      lightweight(false)
    }
  }
}
