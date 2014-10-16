#!/bin/bash
rm -rf tempbuild
mkdir tempbuild
cd tempbuild
git clone https://github.com/gbif/c-deploy
cd c-deploy
mkdir group_vars
cp -f $2 group_vars/all
ansible-playbook -vvv -i $1 control-host.yml --private-key=~/.vagrant.d/insecure_private_key
