/*
   Copyright 2015-2019 Sam Gleske - https://github.com/samrocketman/jenkins-bootstrap-jervis
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   */
/*
   Configure GitHub plugin for GitHub servers and other global Jenkins
   configuration settings.
   github 1.29.5
 */
import net.sf.json.JSONObject
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.GitHubServerConfig
import org.jenkinsci.plugins.github.config.HookSecretConfig
import jenkins.model.Jenkins;

github_plugin = [
    servers: [
        'Public GitHub.com': [
            apiUrl: 'https://api.github.com',
            manageHooks: true,
            credentialsId: 'github-token-secret',
        ]
    ]
]
github_plugin = github_plugin as JSONObject
List configs = []

github_plugin.optJSONObject('servers').each { name, config ->
    if(name && config && config in Map) {
        def server = new GitHubServerConfig(config.optString('credentialsId'))
        server.name = name
        server.apiUrl = config.optString('apiUrl', 'https://api.github.com')
        server.manageHooks = config.optBoolean('manageHooks', false)
        server.clientCacheSize = 20
        configs << server
    }
}

def global_settings = Jenkins.instance.getExtensionList(GitHubPluginConfig.class)[0]
global_settings.overrideHookUrl = false
global_settings.hookUrl = null
global_settings.configs = configs
global_settings.save()
println 'Configured GitHub plugin.'
