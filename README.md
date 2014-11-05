deploy-plugin
=============
Jenkins plugin that automate the deployment of GBIF web services.

For information of how to develop Jenkins plugin please follow this [link](https://wiki.jenkins-ci.org/display/JENKINS/Hosting+Plugins).


#Requisites
This plugin requires:

- Git must be installed in the Jenkins machine where it runs.
- A Jenkins StandardUsernamePasswordCredentials must be defined which has contain Git credentials to access the Github repos https://github.com/gbif/c-deploy and https://github.com/gbif/gbif-configuration.
- Ansible 1.7.1
- Python 2.7.5
- Python libraries: requests, kazoo and pip.

#Functionality
This plugin can be used to deploy a single service or an entire environment; to deploy an entire environment the plugin can be added as post-build step without selecting an GBIF service.

#Configuration
All the required configuration is taken from the GBIF Github repos https://github.com/gbif/c-deploy and https://github.com/gbif/gbif-configuration.

#Debugging

```
$ export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
$ mvn hpi:run
```

#Distributing
```
$ mvn package
```

#Releasing
See https://wiki.jenkins-ci.org/display/JENKINS/Hosting+Plugins
