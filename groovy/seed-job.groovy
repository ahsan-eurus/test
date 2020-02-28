import jenkins.model.*
import org.jenkinsci.plugins.scriptsecurity.scripts.*


def jobName = "seed-job"
def gitRepo = 'https://github.com/bam-labs/asimov.git'
def secretId='github-token'
def branchName='master'

def scm = null
if( System.getenv('DOCKER_COMPOSE') ) {
  scm = ""
} else {
  scm = """<scm class="hudson.plugins.git.GitSCM" plugin="git@3.12.1">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>${gitRepo}</url>
          <credentialsId>${secretId}</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/${branchName}</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>"""
}

def configXml = """\
<?xml version='1.1' encoding='UTF-8'?>
<project>
  <actions/>
  <description>Create Jenkins jobs from DSL groovy files</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <com.sonyericsson.rebuild.RebuildSettings plugin="rebuild@1.31">
      <autoRebuild>false</autoRebuild>
      <rebuildDisabled>false</rebuildDisabled>
    </com.sonyericsson.rebuild.RebuildSettings>
  </properties>
  ${scm}
  <canRoam>true</canRoam>
  <disabled>false</disabled>
  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>

  <triggers>
  <com.cloudbees.jenkins.GitHubPushTrigger plugin="github@1.29.5">
  <spec/>
  </com.cloudbees.jenkins.GitHubPushTrigger>
  </triggers>

  <concurrentBuild>false</concurrentBuild>
  <builders>
    <javaposse.jobdsl.plugin.ExecuteDslScripts plugin="job-dsl@1.76">
      <targets>jenkins/jobs/*.groovy</targets>
      <usingScriptText>false</usingScriptText>
      <sandbox>false</sandbox>
      <ignoreExisting>false</ignoreExisting>
      <ignoreMissingFiles>false</ignoreMissingFiles>
      <failOnMissingPlugin>false</failOnMissingPlugin>
      <failOnSeedCollision>false</failOnSeedCollision>
      <unstableOnDeprecation>false</unstableOnDeprecation>
      <removedJobAction>DELETE</removedJobAction>
      <removedViewAction>DELETE</removedViewAction>
      <removedConfigFilesAction>DELETE</removedConfigFilesAction>
      <lookupStrategy>JENKINS_ROOT</lookupStrategy>
    </javaposse.jobdsl.plugin.ExecuteDslScripts>
  </builders>
  <publishers>
  <hudson.plugins.parameterizedtrigger.BuildTrigger plugin="parameterized-trigger@2.35.2">
  <configs>
  <hudson.plugins.parameterizedtrigger.BuildTriggerConfig>
  <configs class="empty-list"/>
  <projects>seed_cloud</projects>
  <condition>SUCCESS</condition>
  <triggerWithNoParameters>false</triggerWithNoParameters>
  <triggerFromChildProjects>false</triggerFromChildProjects>
  </hudson.plugins.parameterizedtrigger.BuildTriggerConfig>
  <hudson.plugins.parameterizedtrigger.BuildTriggerConfig>
  <configs class="empty-list"/>
  <projects>seed_sharedlib</projects>
  <condition>SUCCESS</condition>
  <triggerWithNoParameters>false</triggerWithNoParameters>
  <triggerFromChildProjects>false</triggerFromChildProjects>
  </hudson.plugins.parameterizedtrigger.BuildTriggerConfig>
  </configs>
  </hudson.plugins.parameterizedtrigger.BuildTrigger>
  </publishers>
  <buildWrappers/>
</project>
""".stripIndent()

def jenkins_home = Jenkins.getInstance().root

if (!Jenkins.instance.getItem(jobName)) {
  def xmlStream = new ByteArrayInputStream( configXml.getBytes() )
  try {
    def seedJob = Jenkins.instance.createProjectFromXML(jobName, xmlStream)
    seedJob.scheduleBuild(0, null)
  } catch (ex) {
    println "ERROR: ${ex}"
    println configXml.stripIndent()
  }
}


/*
  Below script is used for the approval of the seed_cloud job, this is needed because
  the script is not using the groovy sandbox:
    https://jenkins.io/doc/book/managing/script-approval/#groovy-sandbox
  Check the README for more information
*/

int attempts = 50
while(true){
  def item = Jenkins.instance.getItem("seed_cloud") && Jenkins.instance.getItem("seed_sharedlib")
  if (item == false){
    println "Waiting for seed_cloud and seed_sharedlib to be ready"
    sleep(1000)
  }
  else{
    println "Aproving seed_cloud and seed_sharedlib jobs"
    toApprove = ScriptApproval.get().getPendingScripts().collect()
    toApprove.each {pending -> ScriptApproval.get().approveScript(pending.getHash())}
    break
  }
  attempts--
  if(attempts == 0)
    break
}
println "seed-job setup is done"
