
pipelineJob('seed_cloud') {

    logRotator {
      numToKeep(10)
    }

    definition {
      cps {
        script('''
import jenkins.model.*
import java.util.Arrays
import groovy.json.*

import hudson.slaves.JNLPLauncher
import com.cloudbees.jenkins.plugins.amazonecs.ECSCloud
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.MountPointEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.LogDriverOption

import com.nirima.jenkins.plugins.docker.DockerCloud
import com.nirima.jenkins.plugins.docker.DockerTemplate
import com.nirima.jenkins.plugins.docker.DockerTemplateBase
import com.nirima.jenkins.plugins.docker.launcher.AttachedDockerComputerLauncher
import io.jenkins.docker.connector.DockerComputerAttachConnector
import io.jenkins.docker.connector.DockerComputerJNLPConnector

node{
  if ( !System.getenv('DOCKER_COMPOSE') ) {
    git branch: 'master', credentialsId: 'github-token', url: 'https://github.com/bam-labs/asimov.git'
  }

  println "Get slave definitions from file..."
  File f = new File("$WORKSPACE/jenkins/config/slaves.json")
  def jsonSlurper = new JsonSlurper()
  def jsonText = f.getText()
  slaves = jsonSlurper.parseText( jsonText )

  println "Remove current cloud definitions from server..."
  instance = Jenkins.getInstance()
  def ecs_clouds_tmp = instance.clouds
  ecs_clouds_tmp.clear()
  instance.save()
  instance = null

  if ( !System.getenv('DOCKER_COMPOSE') ) {
    slaves.clouds.each { cloud ->
      setupECSCloud(cloud)
    }
  } else {
    slaves.clouds.each { cloud ->
      setupDockerCloud(cloud)
    }
  }

  slaves = null
}

def setupECSCloud(cloud) {
  instance = Jenkins.getInstance()

  def mounts = Arrays.asList(
        new MountPointEntry(
          //name=
          "docker",
          //sourcePath=
          "/var/run/docker.sock",
          //containerPath=
          "/var/run/docker.sock",
          //readOnly=
          false)
        )

  def logDriverOpts = null
  if( cloud.logDriverOptions != null && cloud.logDriver != null ){
    println "setting log driver options..."
    def LogDriverOption[] test = new LogDriverOption[10]
    def i = 0
    cloud.logDriverOptions.each { option ->
      test[i] = new LogDriverOption(option.key,option.value)
      i++
    }
    for (i = 0; i < 10; i++){
      if (test[i] == null){
        test[i] = new LogDriverOption("foo","bar")
      }
    }

    logDriverOpts = Arrays.asList(test)
  }

  def ecsTemplate = new ECSTaskTemplate(
    // templateName
    "jenkins-slave",
    // label =
    cloud.label ?: 'jnlp-slave',
    // taskDefinitionOverride =
    cloud.taskDefinitionOverride ?: null,
    // image =
    cloud.image ?: System.getenv('SLAVE_IMAGE'),
    // repositoryCredentials =
    null,
    // launchType =
    cloud.launchType ?: "EC2",
    // networkMode =
    cloud.networkMode ?: "default",
    // remoteFSRoot =
    "/home/jenkins",
    // memory =
    cloud.memory ?: 1500,
    // memoryReservation =
    0,
    // cpu =
    cloud.cpu ?: 0,
    // subnets =
    cloud.subnets ?: null,
    // securityGroups =
    cloud.securityGroups ?: null,
    // assignPublicIp =
    cloud.assignPublicIp ?: false,
    // privileged =
    cloud.privileged ?: false,
    // containerUser =
    cloud.containerUser ?: null,
    // logDriverOptions =
    logDriverOpts,
    // environments =
    null,
    // extraHosts =
    null,
    // mountPoints =
    (cloud.launchType != "FARGATE") ? mounts : null,
    // portMappings =
    null,
    // executionRole =
    cloud.executionRole ?: null,
    // taskrole =
    cloud.taskrole ?: null,
    // inheritFrom =
    null,
    // sharedMemorySize =
    0
  )

  if( cloud.logDriverOptions != null && cloud.logDriver != null ){
    println "setting log driver..."
    ecsTemplate.setLogDriver(cloud.logDriver)
  }

  ecsCloud = new ECSCloud(
    cloud.name ?: 'ECS_SLAVES',
    "aws-credentials",
    cloud.cluster ?: System.getenv('ECS_CLUSTER_ARN')
  )

  ecsCloud.setTemplates([ecsTemplate])
  ecsCloud.setRegionName(System.getenv('AWS_REGION'))
  ecsCloud.setJenkinsUrl(System.getenv('JENKINS_URL') ?: 'http://'+"curl -s http://169.254.169.254/latest/meta-data/local-ipv4".execute().text+':8080/')

  def ecs_clouds = instance.clouds
  ecs_clouds.add(ecsCloud)
  instance.save()
  println "Saving"
  ecsCloud = null
  instance = null
  logDriverOpts = null
}

def setupDockerCloud(cloud) {

  // https://github.com/jenkinsci/docker-plugin/blob/docker-plugin-1.1.2/src/main/java/com/nirima/jenkins/plugins/docker/DockerTemplateBase.java
  DockerTemplateBase dockerTemplateBase = new DockerTemplateBase(
    // image=
    cloud.image ?: System.getenv('SLAVE_IMAGE'),
    // pullCredentialsId=
    '',
    // dnsString=
    '',
    // network=
    'jenkins_credentials_network',
    // dockerCommand=
    '',
    // volumesString=
    '',
    // volumesFromString=
    '',
    // environmentsString=
    '',
    // hostname=
    '',
    // memoryLimit=
    null,
    // memorySwap=
    null,
    // cpuShares=
    null,
    // sharedMemorySize=
    null,
    // bindPorts=
    '',
    // bindAllPorts=
    false,
    // privileged=
    false,
    // tty=
    true,
    // macAddress=
    '',
    // extraHostsString=
    ''
  )

  // // https://github.com/jenkinsci/docker-plugin/blob/docker-plugin-1.1.2/src/main/java/com/nirima/jenkins/plugins/docker/DockerTemplate.java
  DockerTemplate dockerTemplate = new DockerTemplate(
    dockerTemplateBase,
    new DockerComputerJNLPConnector(new JNLPLauncher()),
    // labelString=
    cloud.label ?: 'jnlp-slave',
    // remoteFs=
    '',
    // instanceCapStr=
    '4'
  )
  //
  // // https://github.com/jenkinsci/docker-plugin/blob/docker-plugin-1.1.2/src/main/java/com/nirima/jenkins/plugins/docker/DockerCloud.java
  DockerCloud dockerCloud = new DockerCloud(
    // name=
    cloud.label ?: 'jnlp-slave',
    // templates=
    [dockerTemplate],
    // serverUrl=
    'unix:///var/run/docker.sock',
    // containerCapStr=
    '4',
    // connectTimeout=
    3,
    // readTimeout=
    60,
    // credentialsId=
    '',
    // version=
    '',
    // dockerHostname=
    ''
  )


  Jenkins jenkins = Jenkins.getInstance()
  jenkins.clouds.add(dockerCloud)
  jenkins.save()
  jenkins = null
  dockerTemplateBase = null
  dockerTemplate = null
  dockerCloud = null
        }
        ''')
        sandbox(false)
      }
    }
}
