#!/bin/bash
sed "s/tagVersion/$1/g" deployment.yaml > app-deployment.yaml