#!/bin/bash

if [ $# -gt 0 ]; then
    cmd_args=$@
    echo "Running with command line arguments $cmd_args"
    scala target/scala-2.12/attire-advisor_2.12-0.1.0-SNAPSHOT.jar $cmd_args
fi