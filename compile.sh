#!/bin/sh

# This script now uses Maven to build and run the project,
# which correctly handles all dependencies from pom.xml.

echo "Building the project with Maven..."
mvn clean install

if [ $? -eq 0 ]; then
    echo "Build successful! Running application..."
    mvn javafx:run
else
    echo "Maven build failed!"
fi 