# Building the Fuzion RFS jenkins slave image

Ensure you have installed the latest version of the Docker and AWS CLI.

## Building the image

The image build is done a single command:
```
docker build -t fuzion-slave .
```
## Tagging the Docker Image

After the build completes, tag your image so you can push the image to this repository:
```
docker tag fuzion-slave 270996056496.dkr.ecr.us-east-1.amazonaws.com/fuzion-slave:{version}
```
The `version` should be unique to prevent that existing version of this image can't retrieved (unless that is what you want).
This (full) tag will be referenced in the Jenkins Cloud definition [see slaves.json configfile](../../../config/slaves.json)

## Pushing Docker image to ECR
Set your AWS credentials. Retrieve the login command to use to authenticate your Docker client to your registry.
Use the AWS CLI:
$(aws ecr get-login --no-include-email --region us-east-1)

Run the following command to push this image to your newly created AWS repository:
```
docker push 270996056496.dkr.ecr.us-east-1.amazonaws.com/fuzion-slave:{version}
```

## Testing the image

You can run a simple test to check if the docker container will connect to the jenkins master.
Bring up Jenkins locally by running `docker-compose up` in the `asimov/jenkins` directory.
Once logged in (admin/admin) check in `http://localhost:8080/configure` if the `fuzion-rfs` cloud exists. Verify that it references the correct docker image.

Make sure the image exists on the local system and is tagged as referenced in the `fuzion-cloud` description.

Create a `New Item`, give it a name and choose the `Pipeline` option. Once created choose it from the Jenkins UI and click on `Configure`. Scroll to the end of the screen check if the `Pipeline script` option is selected and cut and paste the following pipeline definition into the text box.

```
pipeline {
  agent {
    node {
      label 'fuzion-rfs'
    }
  }
  stages {
    stage("test images")
    {
      steps {
        sh 'echo hello fuzion'
      }
    }
  }
}
```

Click safe and run `Build Now`. After a while the job have successfully completed and you can verify in the output if the simple script `echo ehllo fuzion` ran.
