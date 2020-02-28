if ( System.getenv('DOCKER_COMPOSE') ) {
  pipelineJob('dev_shared_pipeline') {

    logRotator {
      numToKeep(10)
    }

    definition {
      cps {
        script('''
@Library('shared-library')_
sharedPipeline()
        ''')
        sandbox(false)
      }
    }
  }

}
