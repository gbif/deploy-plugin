#!/bin/bash
set -e
GIT_CREDENTIALS=$1
ENV=$2
HOSTS=$3
SERVICES=../gbif-configuration/environments/$ENV/services.yml
BUILD_ID=$4
#Directory where the current script is located
SCRIPT_DIR=$(dirname $0)
echo "Deploying environment $ENV"
source $SCRIPT_DIR/deploy.sh $ENV $HOSTS $SERVICES $BUILD_ID
