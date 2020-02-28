def foldername = 'fuzion-rfs'
def wildcard = 'fuzion-rfs'
def jobname = wildcard

folder(foldername) {
    description('subfolder for the fuzion-rfs')
}

listView('fuzion-rfs') {
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
      numToKeep(10)
  }
  parameters {
    gitParam('GIT_BRANCH_NAME') {
        description 'The Git branch to checkout'
        type 'BRANCH'
        defaultValue 'origin/use_repo_tool'
    }
    stringParam('JIRA', 'FUZN-XXXX', 'Enter JIRA ticket id')
    stringParam('COLOR', 'SD', 'crayon color')
    booleanParam('PUBLISH', false, 'This will publish release artifacts to GitHub and/or Artifactory....')
  }
  definition {
    cpsScm {
      scm {
        git {
          // for now manually add a ssh key called fuzion_key to the credentials store and use it for this checkout and the repo tool
          remote {
                url('https://github.com/bam-labs/fuzion-rfs-jenkinsfile.git')
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
