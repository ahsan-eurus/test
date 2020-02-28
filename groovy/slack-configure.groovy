import jenkins.model.Jenkins
import jenkins.plugins.slack.SlackNotifier.*

// def slack = Jenkins.instance.getDescriptorByType(jenkins.plugins.slack.SlackNotifier.DescriptorImpl)
def slack = Jenkins.instance.getExtensionList('jenkins.plugins.slack.SlackNotifier$DescriptorImpl')[0]


def tokenId = 'slack-token'
def teamDomain = System.getenv("SLACK_TEAM_DOMAIN")
def room = System.getenv("SLACK_ROOM")

if  (room == null) {
  room = 'siq_internal'
}

if  (teamDomain == null) {
  teamDomain = 'nlcouds'
}
slack.tokenCredentialId = tokenId
slack.teamDomain = "nclouds"
slack.room =  "siq_internal"

slack.save()
println 'Slack global settings configured.'
