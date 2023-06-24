#!/bin/bash
sed "s/tagVersion/$1/g" deployment.yml > validation-service-deployment.yml