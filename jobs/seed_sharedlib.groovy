
pipelineJob('seed_sharedlib') {

    logRotator {
      numToKeep(10)
    }

    definition {
      cps {
        script('''
import jenkins.model.*
import java.util.Arrays
import groovy.json.*
import jenkins.plugins.git.GitSCMSource
import jenkins.plugins.git.traits.BranchDiscoveryTrait
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

node {
  println "Get shared library configuration from file..."
  File f = new File("/usr/share/jenkins/ref/config/sharedlibs-local.json")
  if ( !System.getenv('DOCKER_COMPOSE') ) {
    git branch: 'master', credentialsId: 'github-token', url: 'https://github.com/bam-labs/asimov.git'
    f = new File("$WORKSPACE/jenkins/config/sharedlibs.json")
  }

  def jsonSlurper = new JsonSlurper()
  def jsonText = f.getText()
  println jsonText
  sharedlibs = jsonSlurper.parseText( jsonText )
  List libraries = [] as ArrayList
  sharedlibs.libs.each { lib ->
      def remote = lib.remote
      def credentialsId = lib.credentialsId ?: null
      def version = lib.version
      def scm = new GitSCMSource(remote)
      def retriever = new SCMSourceRetriever(scm)
      scm.traits = [new BranchDiscoveryTrait()]

      name = lib.name
      def library = new LibraryConfiguration(name, retriever)
      defaultVersion = 'master'
      if (remote != null) {   
          if (version == null) {
              version = 'master'
          }
          if (credentialsId != null) {
              scm.credentialsId = credentialsId
          }  
          library.defaultVersion = version
          library.implicit = true
          library.allowVersionOverride = true
          library.includeInChangesets = true
          libraries.add(library)
      }
  }
  def global_settings = Jenkins.instance.getExtensionList(GlobalLibraries.class)[0]
  global_settings.libraries = libraries
  global_settings.save()
  sharedlibs = null
}
        ''')
        sandbox(false)
      }
    }
}
