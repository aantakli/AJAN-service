#!/bin/bash

sleep 60

curl --location --request PUT ${repoURL}node_definitions --header 'Content-Type: application/trig' --data-binary "@/app/create.node"

curl --location --request PUT ${repoURL}editor_data --header 'Content-Type: application/trig' --data-binary "@/app/create.editor"

curl --location --request PUT ${repoURL}node_definitions/statements --header 'Content-Type: application/trig' --data-binary "@/app/node_definitions.ttl"

curl --location --request PUT ${repoURL}editor_data/statements --header 'Content-Type: application/trig' --data-binary "@/app/editor_data.trig"

