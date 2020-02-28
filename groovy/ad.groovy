import jenkins.model.Jenkins
import hudson.security.*
import hudson.plugins.active_directory.*

def jenkins = Jenkins.instance

if( System.getenv('SIMPLE_AD_FQDN') ){
  println "Active Directoy Auth"
  String domain = System.getenv('SIMPLE_AD_FQDN') ?: 'null'
  String site = ''
  String server = ''
  String bindName = ''
  String bindPassword = ''

  SecurityRealm ad_realm = new ActiveDirectorySecurityRealm(domain.trim(), site, bindName, bindPassword, server)
  jenkins.instance.setSecurityRealm(ad_realm)
  jenkins.save()
} else {
  println "Jenkins database auth"
  def jenkins_username = "admin"
  def jenkins_password = "admin"
  def hudsonRealm = new HudsonPrivateSecurityRealm(false)
  hudsonRealm.createAccount(jenkins_username,jenkins_password)
  jenkins.instance.setSecurityRealm(hudsonRealm)
  jenkins.save()
}
