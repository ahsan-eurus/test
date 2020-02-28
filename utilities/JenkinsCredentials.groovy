import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.impl.*;
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import com.cloudbees.plugins.credentials.domains.*;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.model.*
import hudson.util.Secret
import java.io.File;
import jenkins.model.Jenkins;
import java.util.logging.Logger

class JenkinsCredentials {

  def Logger logger = Logger.getLogger("jenkins.SSMQuery")

  public String SetUsernamePasswordCredentials( ssm_param, credential_name, description, user){

    def loader = new GroovyClassLoader(getClass().getClassLoader())
    def SSMQuery = loader.parseClass(new File(System.getenv("JENKINS_HOME") + "/SSMQuery.groovy")).newInstance()
    def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

    def credential = SSMQuery.getParameterByName(ssm_param)

    def domain = Domain.global()

    if (!credential.equals("")){
      Credentials c = (Credentials) new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        credential_name,
        description,
        user,
        credential
        )
        store.addCredentials(domain, c)
    }
  }

  public String SetStringCredentials( ssm_param, credential_name, description){

    def loader = new GroovyClassLoader(getClass().getClassLoader())
    def SSMQuery = loader.parseClass(new File(System.getenv("JENKINS_HOME") + "/SSMQuery.groovy")).newInstance()
    def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

    def credential = SSMQuery.getParameterByName(ssm_param)

    def domain = Domain.global()

    if (!credential.equals("")){
      Credentials c = (Credentials) new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        credential_name,
        description,
        Secret.fromString(credential)
        )
        store.addCredentials(domain, c)
    }
  }

  public String SetSSHCredentials(ssm_param=null, credential_name, user, description=null){
    def domain = Domain.global()
    def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    if ( ssm_param == null) {
      def keyFileContents = new File("/var/jenkins_home/.ssh/" + credential_name).text
      def privateKey = new BasicSSHUserPrivateKey(
        CredentialsScope.GLOBAL,
        "shared-libraries-deploy-key",
        "git",
        new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(keyFileContents),
        "",
        "SSH key for shared-library"
        )
        store.addCredentials(domain, privateKey)
    }
    else {
      def loader = new GroovyClassLoader(getClass().getClassLoader())
      def SSMQuery = loader.parseClass(new File(System.getenv("JENKINS_HOME") + "/SSMQuery.groovy")).newInstance()
      def credential = SSMQuery.getParameterByName(ssm_param)
      if (!credential.equals("")){
        Credentials c = (Credentials) new BasicSSHUserPrivateKey(
          CredentialsScope.GLOBAL,
          credential_name,
          user,
          new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(credential),
          "",
          description,
          )
          store.addCredentials(domain, c)
      }
    }
  }
}
