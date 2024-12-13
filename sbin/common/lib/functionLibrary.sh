#!/usr/bin/bash
# This script contains a library of useful functions
# Functions list:
# downloadFile - Downloads a file and optionally checks it against a sha.
# info - echoes any string sent to it, but only if it is enabled first.
# checkFileSha - Checks whether a named file matches a supplied sha.
# doesThisURLExist - Checks if a given URL exists.

# This simple function takes a string, and echoes it if info-level logging is enabled.
# Info-level logging can be enabled by simply passing the word "enable" to it as the sole argument.
# This is handy for debug messages, as it can simply not be enabled for quiet runs.
enableInfoLoggingBool=1

function info() {
  [ $# == 0 ] && echo "Warning: The library function \"info\" was called without an argument." && return 1
  [[ $enableInfoLoggingBool == 0 ]] && echo $1 && return 0
  [[ "${1}" == "enable" ]] && enableInfoLoggingBool=0 && return 0
}

# This function takes the supplied sha and compares it against the supplied file.
# Example: checkFileSha 123456 /usr/bin/etc/exampleFile
# Caution: I strongly advise against using relative paths.
function checkFileSha() {
  info "Checking if a file matches the sha256 checksum. Fails if there is no checksum."
  if [ $# != 2 ]; then
    echo "Error: checkFileSha() function was not supplied with exactly 2 arguments."
    exit 1
  fi
  
  if [[ -z $1 ]]; then
    info "No sha256 checksum found."
    info "Check declared failed."
    return 1
  fi
  
  if [[ ! -x $2 ]]; then
    info "The file we're trying to check does not exist: ${2}"
    return 1
  fi

  info "Checking if a file matches the checksum."
  shaReturnCode=1
  if command -v sha256sum &> /dev/null; then
    echo "$1 $2" | sha256sum -c --quiet
    shaReturnCode=$?
  elif command -v shasum &> /dev/null; then
    echo "$1 $2" | shasum -a 256 -c &> /dev/null -
    shaReturnCode=$?
  else
    echo "Error: Neither sha256sum nor shasum is available on this machine."
    exit 1
  fi
  
  if [ $shaReturnCode != 0 ]; then
    echo "Warning: File ${2} does not match the supplied checksum."
    return 1
  else
    info "File matches the checksum."
    return 0
  fi
}

# This function checks if a given URL (string argument) exists.
function doesThisURLExist() {
  info "Checking if a given URL exists."
  if [ $# == 0 ]; then
    echo "Error: doesThisURLExist() function was not supplied with a URL."
    exit 1
  fi
  
  spiderOutput=1
  if command -v wget &> /dev/null; then
    wget --spider -q ${source} 2> /dev/null
    spiderOutput=$?
  elif command -v curl &> /dev/null; then
    curl -I ${source} -s | grep "200 OK" -q
    spiderOutput=$?
  else
    echo "Error: Neither wget nor curl could be found when downloading this file: ${source}"
    exit 1
  fi
  
  return $spiderOutput
}

# This function downloads files
# The accepted arguments are:
# -source (mandatory: a web URL where the file is located)
# -destination (mandatory: an existent folder where the file will be put)
# -filename (optional: the new name of the file post-download)
# -sha (optional: the anticipated sha of the downloaded file)
function downloadFile() {
  source=""
  destination=""
  filename=""
  sha=""

  arrayOfArgs=( "$@" )
  x=0
  while [[ ${#arrayOfArgs[@]} -gt $((x+1)) ]]; do
    arg="${arrayOfArgs[x]}"
    x="$((x+1))"
    value="${arrayOfArgs[$x]}"
    case "$arg" in
      --source | -s )
      source="${value}"
      ;;

      --destination | -d )
      destination="${value}"
      ;;
  
      --filename | -f )
      filename="${value}"
      ;;

      --sha | -sha )
      sha="${value}"
      ;;
  
      *) echo >&2 "Invalid downloadFile argument: ${arg} ${value}"; exit 1;;
    esac
    x="$((x+1))"
  done

  info "File download requested."
  
  if [[ "${source}" == "" || "${destination}" == "" ]]; then
    echo "Error: function downloadFile requires both a source and a destination."
    echo "Source detected: ${source}"
    echo "Destination detected: ${destination}"
    exit 1
  fi

  info "File details: "
  info "- source: ${source}"
  info "- destination: ${destination}"
  info "- file name: ${filename}"
  info "- sha256 checksum: ${sha}"
  
  if [ -z ${filename} ]; then
    filename="${source##*/}"
  fi

  info "Checking if source exists."
  doesThisURLExist "${source}"
  [[ $? != 0 ]] && echo "Error: File could not be found at source." && exit 1
  info "Source exists."

  info "Checking if destination folder exists."
  [ ! -x ${destination} ] && echo "Error: Destination folder could not be found." && exit 1

  info "Destination folder exists. Checking if file is already present."
  if [ -x "${destination}/${filename}" ]; then
    info "Warning: File already exists."
    checkFileSha "${sha}" "${destination}/${filename}"
    if [[ $? != 0 ]]; then
      info "Warning: A file was found with the same name, and it does not match the supplied checksum. Removing file."
      rm "${destination}/${filename}"
      if [ $? != 0 ]; then
        echo "Error: Could not remove file."
        exit 1
      fi
    else
      info "A file was found with the same name, and it matches the supplied checksum. Skipping download."
      exit 0
    fi
  fi
  if [ -x "${destination}/${source##*/}" ]; then
    info "Warning: File already exists with the default file name: ${source##*/}"
    checkFileSha "${sha}" "${destination}/${source##*/}"
    if [[ $? != 0 ]]; then
      info "Warning: A file was found with the same name, and it does not match the supplied checksum. Removing file."
      rm "${destination}/${source##*/}"
      if [ $? != 0 ]; then
        echo "Error: Could not remove file."
        exit 1
      fi
    else
      info "A file was found with the same name, and it matches the supplied checksum. Skipping download."
      mv "${destination}/${source##*/}" "${destination}/${filename}"
      exit 0
	fi
  fi

  info "File not already downloaded. Attempting file download."
  if command -v wget &> /dev/null; then
    info "Found wget. Using wget to download the file."
    wget -q -P "${destination}" "${source}"
  elif command -v curl &> /dev/null; then
    info "Found curl. Using curl to download the file."
    curl -s "${source}" -o "${destination}"
  fi
  if [ ! -x "${destination}/${filename}" ]; then
    mv "${destination}/${source##*/}" "${destination}/${filename}"
  fi
  
  info "File download is complete."
  
  if [[ -n $sha ]]; then
    checkFileSha "${destination}/${filename}"
    if [[ $? != 0 ]]; then
      echo "Error: Checksum does not match the downloaded file. Removing file."
      rm "${destination}/${filename}"
      exit 1
    fi
  fi
  
  info "File has been downloaded successfully."
  
  info "Setting file permissions to 770."
  chmod 770 "${destination}/${filename}"
  [ $? != 0 ] && echo "Error: Checksum does not match the downloaded file. Removing file." && rm "${destination}/${filename}" && exit 1
  
  info "File permissions set successfully."
  info "File download script complete"
  
  return 0
}
