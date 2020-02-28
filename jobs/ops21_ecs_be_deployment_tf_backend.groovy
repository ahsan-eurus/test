def foldername = 'ops21'
def wildcard = 'OPS21'
def jobname = wildcard+'-ECS-BE-Deployment-TF-Backend'

folder(foldername) {
    description('subfolder the ops21 environment')
}

listView('OPS21') {
  recurse()
  jobs {
      regex('.*.'+wildcard+'.*')
  }
  columns {
    status()
    weather()
    name()
    lastSuccess()
    lastFailure()
    lastDuration()
    buildButton()
  }
}

pipelineJob(foldername+'/'+jobname) {

  logRotator {
      numToKeep(5)
  }
  environmentVariables(AWS_Account: 'siq-dev', CLUSTER: 'ops21', VERSION: 'v2', SCRIPT: 'ECS_BE_Test.sh')
  parameters {
    gitParam('GIT_BRANCH_NAME') {
        description 'The Git branch to checkout'
        type 'BRANCH'
        defaultValue 'origin/master'
    }
    stringParam('BUILD_VERSION', null, 'Build number that available on AWS ECR repository')
    booleanParam('FORCE_REFRESH', false, 'This will taint all the ecs tasks and force a refresh of the instances')
    choiceParam('APP', ['all','siq-db-manager','siq-data-consumers','siq-batch','siq-rollup','siq-service-admin','siq-service-applications','siq-product-ops'], 'AWS ECS service name')
    choiceParam('SIQ_DATA_CONSUMERS', ['0','1','2','3','4','5'], 'Desired Count for siq-data-consumers docker container')
    choiceParam('SIQ_BATCH', ['0','1','2','3','4','5'], 'Desired Count for siq-batch docker container')
    choiceParam('SIQ_ROLLUP', ['0','1','2','3','4','5'], 'Desired Count for rollup docker container')
    choiceParam('SIQ_SERVICE_ADMIN', ['0','1','2','3','4','5'], 'Desired Count for siq-service-admin docker container')
    choiceParam('SIQ_SERVICE_APPLICATIONS', ['0','1','2','3','4','5'], 'Desired Count for service application docker container')
    choiceParam('SIQ_PRODUCTS_OPS', ['0','1','2','3','4','5'], 'Desired Count for siq-products-ops docker container')
    stringParam('EMAIL_RECIPIENTS', null, 'Email notification recipient list')
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
                url('https://github.com/bam-labs/aws_infrastructure.git')
                credentials('github-token')
            }
            branches('${GIT_BRANCH_NAME}')
        }
      }
      scriptPath('Jenkinsfile')
      lightweight(false)
    }
  }
}
