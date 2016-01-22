#!/bin/bash
#Parameters
GIT_CREDENTIALS=$1
ENV=$2
HOSTS=$3
SERVICES=$4
PLAYBOOK=$5
BUILD_ID=$6
CDEPLOY_BRANCH=$7
BUILD_HOSTS=${BUILD_ID}_hosts
INSTALL_WEBSERVER="installWebserver=False"

#Exit on error
set -e
if [ -d "gitrepos" ]; then
  #If gitrepos exists, update the repositories
  echo "Updating local Git repositories"
  cd gitrepos/c-deploy
  echo "Current branch "
  echo $(git rev-parse --abbrev-ref HEAD)
  git reset --hard HEAD
  git clean -fdx
  git fetch --tags
  if [ $(git rev-parse --abbrev-ref HEAD) !=  $CDEPLOY_BRANCH ]; then
    git checkout $CDEPLOY_BRANCH
  fi
  git pull origin $CDEPLOY_BRANCH
  cd ../..
  cd gitrepos/gbif-configuration
  git pull --all
  cd ..
else
  #Create the directory gitrepos if it doesn't exist
  mkdir gitrepos
  cd gitrepos
  echo "Cloning Git repositories: c-deploy and gbif-configuration"
  #Clone repos
  git clone -b $CDEPLOY_BRANCH https://${GIT_CREDENTIALS}@github.com/gbif/c-deploy
  git clone https://${GIT_CREDENTIALS}@github.com/gbif/gbif-configuration
fi

#Create group_vars if doesn't exist
cd c-deploy/services
if [ ! -d "group_vars" ]; then
  echo "group_vars directory didn't exist, creating it"
  mkdir group_vars
fi

#Configuration an services files are concatenated into a single file, that will contain the ansible variables
cat ../../gbif-configuration/environments/$ENV/configuration.yml $SERVICES >> group_vars/$BUILD_ID

#The default anisble inventory file 'hosts' is concatenated with the input HOSTS file
cat ../../gbif-configuration/environments/$ENV/hosts $HOSTS >> $BUILD_HOSTS

if [ $PLAYBOOK="webserver" ]; then
  INSTALL_WEBSERVER="installWebserver=True"
fi

#Executes the ansible playbook
echo "Executing ansible playbook"

ansible-playbook -vvv -i $BUILD_HOSTS $PLAYBOOK.yml --private-key=~/.ssh/id_rsa --extra-vars "git_credentials=${GIT_CREDENTIALS} ${INSTALL_WEBSERVER}"

#remove temporary files
rm -f group_vars/$BUILD_ID $BUILD_HOSTS
