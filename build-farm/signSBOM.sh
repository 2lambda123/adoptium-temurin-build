#!/bin/sh

# Check if JDK-17 is installed and set as the default JDK
if ! java -version 2>&1 | grep -q '"17.'; then
  echo "Error: JDK-17 is not installed or not set as the default JDK. Please install JDK-17 and set it as the default JDK before running this script."
  exit 1
fi

# Set the SCRIPT_DIR variable
SCRIPT_DIR=$(cd "$(dirname "$0")"; pwd)

# Run the ant build command to build the org.webpki.json openkeystore code
ant -buildfile ${SCRIPT_DIR}/../cyclonedx-lib/build.xml buildSignSBOM

# Check the exit status of the ant build command
if [ $? -ne 0 ]; then
  echo "Error: The ant build command to build the org.webpki.json openkeystore code failed."
  exit 1
fi

echo "Success: The org.webpki.json openkeystore code was built successfully using JDK-17."
