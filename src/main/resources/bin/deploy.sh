#!/bin/bash -eu

#Parameters
GIT_CREDENTIALS=$1
ENV=$2
HOSTS=$3
SERVICES=$4
PLAYBOOK=$5
BUILD_ID=$6
CDEPLOY_BRANCH=$7
CONFIGURATION_BRANCH=$7
BUILD_HOSTS=${BUILD_ID}_hosts

if [[ -d "gitrepos" ]]; then
  # If gitrepos exists, update the repositories
  echo "Updating local Git repositories"
  cd gitrepos/c-deploy
  echo -n "Current branch: "
  echo $(git rev-parse --abbrev-ref HEAD)
  git reset --hard HEAD
  git clean -fdx
  git fetch --all --tags
  git checkout $CDEPLOY_BRANCH
  # Update the branch, if it's a branch.
  if git show-ref --verify --quiet refs/remotes/origin/$CDEPLOY_BRANCH; then git merge refs/remotes/origin/$CDEPLOY_BRANCH; fi
  # (With a newer Git, we could just do 'git pull $CDEPLOY_BRANCH')
  cd ../..
  cd gitrepos/gbif-configuration
  git checkout $CONFIGURATION_BRANCH
  git pull --all
  cd ..
else
  # Create the directory gitrepos if it doesn't exist
  mkdir gitrepos
  cd gitrepos
  echo "Cloning Git repositories: c-deploy and gbif-configuration"
  # Clone repos
  git clone -b $CDEPLOY_BRANCH https://${GIT_CREDENTIALS}@github.com/gbif/c-deploy
  git clone -b $CONFIGURATION_BRANCH https://${GIT_CREDENTIALS}@github.com/gbif/gbif-configuration
fi

# Create group_vars
# (It can't exist, as we did a "git clean" above.)
cd c-deploy/services
echo "Creating group_vars directory"
mkdir group_vars

# Configuration and services files are concatenated into a single file, that will contain the Ansible variables
cat ../../gbif-configuration/environments/$ENV/configuration.yml \
    ../../gbif-configuration/environments/$ENV/monitoring.yml \
    $SERVICES >> group_vars/$BUILD_ID

# The default Ansible inventory file 'hosts' is concatenated with the input HOSTS file
cat ../../gbif-configuration/environments/$ENV/hosts $HOSTS >> $BUILD_HOSTS

# Executes the Ansible playbook
echo "Executing Ansible playbook"

ansible-playbook -vvv -i $BUILD_HOSTS $PLAYBOOK.yml --private-key=~/.ssh/id_rsa --extra-vars "git_credentials=${GIT_CREDENTIALS}"

# Remove temporary files
rm -f group_vars/$BUILD_ID $BUILD_HOSTS
