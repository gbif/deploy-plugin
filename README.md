
This projects contains the GBIF plugins that automate deployment steps.

DeployBuilder: a post-build step that triggers the ansible scripts located in https://github.com/gbif/c-deploy to 
install an artifact in environment.
The environments are configured globally using the DeployDescriptor class.
