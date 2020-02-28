# Jenkins setup

This guide will give you an introduction of how the jenkins server is setup.

## Prerrequisites

- The following secrets needs to be created in AWS Parameter Store for the Github plugin to work.

    - /SIQ/*${terraform_workspace}*/jenkinsGitHubToken
      - Where *${terraform_workspace}* is the terraform workspace where the jenkins container is going to be launched

## How everything is structured?

### Plugins

Plugins are managed by the plugins.txt file
It has a format like this
```
aws-java-sdk:1.11.594
active-directory:2.16
role-strategy:2.13
```
If you want to add another one, just add a new line, ingress the name of the plugin and the version you want to use.

### Scripts to configure jenkins

If you want to add an script that will add functionality to jenkins, you need to create a groovy file inside the groovy directory and it will automatically get triggered when jenkins get started

### Secrets management

Secrets like github token are pulled from AWS parameter store, and saved on the jenkins credentials.

We have the directory called **Utilities** where we have the class called **SSMQuery.groovy** that is the one in charge on download the secrets.

If you add a script and you want to instanciate it, you need to add this to your script.

```bash
import hudson.model.*
import java.io.File;
import jenkins.model.Jenkins;

def loader = new GroovyClassLoader(getClass().getClassLoader())
def SSMQuery = loader.parseClass(new File(System.getenv("JENKINS_HOME") + "/SSMQuery.groovy")).newInstance()

```

And after that you can access to both methods, read parameter by name or read parameters by Path, for information check the class on the utilities directory.

We have the file called `groovy/credentials.groovy` where we set the common credentials, but if you need to call the class from another file, you can do it with above instructions.


### Jobs

For the jobs we are using jenkins DSL more documentation here
https://jenkinsci.github.io/job-dsl-plugin/

Basically it allow us to configure jobs as code, so everytime jenkins get started we will be able to automatically create the jobs from the files you specify.

So if you want to add a new job follow the next steps.

- Create a job file in DSL format in the jobs directory.
- Build the dockerfile and you are all set


### ECS Clouds
Edit jenkins/config/slaves.json to add or remove slave clouds

* Required values:
- name: the name of the ECS Cloud in Jenkins
- label: the id to use this slave from a Jenkins job

* Optional values:
- image: docker image for the slave, if not specified it will use the value from SLAVE_IMAGE environment variable
- memory: how much memory in mb to assign to the slave container (default is 1500)
- cpu: how many cpu units to assign to the slave container (default is no limit)
- launchType: EC2 or FARGATE (default is EC2)
- networkMode: aws network mode for the slave task (default is default)
- subnets: comma separated list of subnets to deploy the slave in (only required if launchType is FARGATE, default is null)
- securityGroups: comma separated list of security groups (only required if launchType is FARGATE, default is null)
- assignPublicIp: only required with Fargate (default is false)
- privileged: run container as privileged (default is false)
- containerUser: user to run the slave as (default is null)
- taskrole: IAM role for the slave task (default is null)
- executionRole: Task's IAM execution role
- cluster: ECS cluster to run the slave, if not specified the slave will run in the same cluster as the Jenkins master node
- logDriver: the log driver's name as a string
- logDriverOptions: Json array with the following form:
  ```
  [
    {
      "key": "some_key",
      "value": "some_value"
    }
  ]
  ```

### Testing Jenkins locally
You can test some of the code you develop by running the Jenkins master locally by running `docker-compose`. Most of the setting required to run Jenkins in the `siq-dev` environment are hardcoded in the `docker-compose.yaml`. The only environment variable setting that is required is `AWS_PROFILE`. It should point to the appropriate credentials in the ~/.aws/credentials file. Also, a keypair named `id_rsa` (default) is required to be present in the default ssh directory of the local system on which the Jenkins will be run as that key will be used to create credentials that Jenkins would need to access local git server setup. Then all that is needed to run:

```
docker-compose up
```
You can connect to jenkins on `localhost:8080`
Notes:
- The local setup will not use AD.
- It will not be able to bring up a jenkins slave on ECS as the slave won't be able to connect to the master
- This setup makes use of the amazon/amazon-ecs-local-container-endpoints container image. More details [here](https://aws.amazon.com/blogs/compute/a-guide-to-locally-testing-containers-with-amazon-ecs-local-endpoints-and-docker-compose/)


### Github webhooks
We use Github webhooks to trigger jobs when a commit is made to the repository it was configured from.

In order to configure it we need that the token **/SIQ/*${terraform_workspace}*/jenkinsGitHubToken** get the following permissions.

```bash
admin:repo_hook  Full control of repository hooks
 write:repo_hook  Write repository hooks
 read:repo_hook  Read repository hooks

admin:org_hook  Full control of organization hooks
```

And in the DSL configuration, we add this block to make Jenkins add the webhook configuration to Github.

```bash
triggers {
    gitHubPushTrigger()
}
```

### Seed jobs

We have two seed-jobs:

**Seed-job:**
This job is created with XML and can be found here:   **jenkins/groovy/seed-job.groovy**. It needs to be in XML because this job is the one in charge of load all the DSL scripts.

**Seed-cloud:**

This job is a DSL script, and its created by the seed-job.
Because this job needs to update the global configuration settings, it needs to run without the groovy sandbox.

That implies that the script needs to be approved:

- When jenkins first start, the **seed-cloud** is approved in the **jenkins/groovy/seed-job.groovy** script exactly here:
    - Take note we use the `sleep` command to wait for the seed_cloud to gets created by the seed-job, once its created it will approved the script.

```bash
/*
  Below script is used for the approval of the seed_cloud job, this is needed because
  the script is not using the groovy sandbox:
    https://jenkins.io/doc/book/managing/script-approval/#groovy-sandbox
*/

int attempts = 50
while(true){
  def item = Jenkins.instance.getItem("seed_cloud")
  if (item == null){
    println "Waiting to seed_cloud to be ready"
    sleep(1000)
  }
  else{
    println "Aproving seed_cloud job"
    toApprove = ScriptApproval.get().getPendingScripts().collect()
    toApprove.each {pending -> ScriptApproval.get().approveScript(pending.getHash())}
    break
  }
  attempts--
  if(attempts == 0)
    break
}
println "seed-job setup is done"
```

-  **After startup**, if the seed-cloud job is updated an Administrator will must have to approve the script.
