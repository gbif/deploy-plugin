# GBIF Deploy Plugin

Jenkins plugin that automates the deployment of GBIF web services.

For information of how to develop Jenkins plugins, please follow this [link](https://wiki.jenkins-ci.org/display/JENKINS/Hosting+Plugins).

## Requirements

This plugin requires:

- Git must be installed in the Jenkins machine where it runs.
- A Jenkins *system* StandardUsernamePasswordCredentials must be defined which has contain Git credentials to access the Github repos https://github.com/gbif/c-deploy and https://github.com/gbif/gbif-configuration.  The odd behaviour of resetting the credential after a Jenkins restart is necessary here.
- The option "Delete workspace before build" must be selected (otherwise c-deploy isn't updated; TODO)
- Ansible 1.7.1
- Python 2.7.5
- Python libraries: requests, kazoo and pip.
- A key stored in ~/.ssh/id_rsa will be used to connect to the servers contacted by the Ansible playbooks.

## Functionality

This plugin can be used to deploy a single service or an entire environment; to deploy an entire environment the plugin can be added as post-build step without selecting a GBIF service.

## Configuration

All the required configuration is taken from the GBIF Github repos https://github.com/gbif/c-deploy and https://github.com/gbif/gbif-configuration.

## Debugging

```
$ export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
$ mvn hpi:run
```

## Distributing

```
$ mvn package
```

## Releasing

To release this plugin use the Maven command

```
$ mvn release:prepare release:perform
```

For more information see https://wiki.jenkins-ci.org/display/JENKINS/Hosting+Plugins
