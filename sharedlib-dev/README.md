# Using the git local shared library git server



### Shared Library setup

If you need to setup the shared library for jenkins on the local github server, then follow the following steps:



Change directory to the directory where jenkins configuration is defined:

```
$ cd asimov/jenkins/
```


Apply command to build the image with the new repository information:

```
$ docker-compose build --no-cache
```



Now deploy the containers with the command:

```
$ docker-compose up
```



### Already setup Shared Library

If the shared library repository doesnot need to be changed, then just apply the following commands:

```
$ cd asimov/jenkins

$ docker-compose build --no-cache

$ docker-compose up 
```



### Removing the local git hub server Shared Library setup

```
$ docker-compose down
$ docker volume rm jenkins_git-repo
```

