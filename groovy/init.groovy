#!groovy
import jenkins.*
import jenkins.model.*
import hudson.model.*
import java.io.File
import jenkins.model.Jenkins
import hudson.security.*
import java.util.logging.Logger
import jenkins.security.s2m.*
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.CredentialsScope
import hudson.util.Secret
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl
import com.cloudbees.plugins.credentials.SecretBytes

import hudson.tasks.Maven.MavenInstallation;
import hudson.tools.*;
import hudson.util.DescribableList;

import org.jfrog.*
import org.jfrog.hudson.*
import org.jfrog.hudson.util.Credentials;


def loader = new GroovyClassLoader(getClass().getClassLoader())
def SSMQuery = loader.parseClass(new File(System.getenv("JENKINS_HOME") + "/SSMQuery.groovy")).newInstance()
def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()
def terraformWorkspace  = System.getenv("TERRAFORM_WORKSPACE")
def jenkins_Url = System.getenv("JENKINS_URL")?: 'https://cicd.sleepiqlabs.com'
def adminAddress = System.getenv("ADMIN_ADDRESS")

def artifactoryUser = ""
if( System.getenv('DOCKER_COMPOSE') ){
  artifactoryUser = 'local'
}
artifactoryUser = System.getenv("ARTIFACTORY_USER")? 'jenkins': artifactoryUser
def artifactoryServer = System.getenv("ARTIFACTORY_SERVER_SIQ")?: 'https://repo.sleepiqlabs.com/artifactory'

def ssm_param = "/SIQ/"+terraformWorkspace+"/artifactory/"+artifactoryUser
def credpassword = SSMQuery.getParameterByName(ssm_param+'/password')
def credusername = SSMQuery.getParameterByName(ssm_param+'/user')

if ( adminAddress != null) {
  jenkinsLocationConfiguration.setAdminAddress(adminAddress)
}

jenkinsLocationConfiguration.setUrl(jenkins_Url)
jenkinsLocationConfiguration.save()

Jenkins.instance.injector.getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
Jenkins.instance.save()

// maven congig global tool
def mavenDesc = jenkins.model.Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]

def isp = new InstallSourceProperty()
def autoInstaller = new hudson.tasks.Maven.MavenInstaller("3.3.3")
isp.installers.add(autoInstaller)

def proplist = new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>()
proplist.add(isp)

def installation = new MavenInstallation("maven3", "", proplist)

mavenDesc.setInstallations(installation)
mavenDesc.save()

// artifactor server definition
def inst = Jenkins.getInstance()
def desc = inst.getDescriptor("org.jfrog.hudson.ArtifactoryBuilder")
def deployerCredentials = new CredentialsConfig(credusername, credpassword, "")
def resolverCredentials = new CredentialsConfig("", "", "")

def sinst = [new ArtifactoryServer(
  "ARTIFACTORY_SERVER_SIQ",
  artifactoryServer,
  deployerCredentials,
  resolverCredentials,
  300,
  false,
  3,1 )
]

desc.setArtifactoryServers(sinst)

desc.save()
