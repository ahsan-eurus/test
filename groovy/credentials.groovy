import hudson.model.*
import java.io.File;
import jenkins.model.Jenkins;

def loader = new GroovyClassLoader(getClass().getClassLoader())
def Credentials = loader.parseClass(new File(System.getenv("JENKINS_HOME") + "/JenkinsCredentials.groovy")).newInstance()

def terraformWorkspace  = System.getenv("TERRAFORM_WORKSPACE")
def github_token = "/SIQ/" + terraformWorkspace + "/jenkinsGitHubToken"
def slack_token = "/SIQ/" + terraformWorkspace + "/slackToken"
def fuzion_pem = "/SIQ/" + terraformWorkspace + "/fuzion/PrivateSSHKey"

if ( System.getenv('DOCKER_COMPOSE') ) {
    Credentials.SetSSHCredentials("id_rsa","git")
}
else {
    // add github token as a user password due an issue in Jenkins
    Credentials.SetUsernamePasswordCredentials(github_token, "github-token", "github API token", "Jenkins")
    // add github token as a secret text for github-plugin configuration. This secret is used in the github-global-configuration.groovy file
    Credentials.SetStringCredentials(github_token, "github-token-secret", "token for managing webhooks triggers")

    // add token for Slack notifications
    Credentials.SetStringCredentials(slack_token, "slack-token", "token for slack notifications")

    // add ssh key for fuzion project
    Credentials.SetSSHCredentials(fuzion_pem, "fuzion_key","fuzion", "private key for fuzion project")

}
