#!/bin/bash
#Parameters
GIT_CREDENTIALS=$1
ENV=$2
HOSTS=$3
SERVICES=$4
BUILD_ID=$5
BUILD_HOSTS=${BUILD_ID}_hosts

#Exit on error
set -e
if [ -d "gitrepos" ]; then
  #If gitrepos exists, update the repositories
  echo "Updating local Git repositories"
  cd gitrepos/c-deploy
  git pull --all
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
  git clone https://${GIT_CREDENTIALS}@github.com/gbif/c-deploy
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

#Executes the ansible playbook
echo "Executing ansible playbook"
ansible-playbook -vvv -i $BUILD_HOSTS services.yml --su --su-user=root --private-key=~/.ssh/id_rsa --skip-tags "containers" --extra-vars "git_credentials=${GIT_CREDENTIALS}"

#remove temporary files
rm -f group_vars/$BUILD_ID $BUILD_HOSTS
