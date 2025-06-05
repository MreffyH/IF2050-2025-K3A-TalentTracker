#!/bin/sh

# JavaFX SDK path
JAVAFX_PATH=~/javafx-sdk/javafx-sdk-22.0.2/lib

# Clean output directory
rm -rf out
mkdir -p out

echo "Copying resources..."
# Copy all resources maintaining directory structure
cp -r src/main/resources/* out/

echo "Compiling..."
# Compile
javac --module-path $JAVAFX_PATH \
      --add-modules javafx.controls,javafx.fxml \
      -d out \
      $(find src/main/java -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation successful! Running application..."
    # Run
    java --module-path $JAVAFX_PATH \
         --add-modules javafx.controls,javafx.fxml \
         -cp out \
         com.talenttracker.Main
else
    echo "Compilation failed!"
fi 