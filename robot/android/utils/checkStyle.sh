#!/bin/bash

java -jar google-java-format-1.7-all-deps.jar -n --set-exit-if-changed `find ../app/src -type f -name *java`

if [ $? -eq 0 ]
then
    echo "All java files follow the correct style."
    exit 0
else
    echo "The above java files follow the wrong style. Please use ./gradlew applyStyle"
    exit 1
fi
