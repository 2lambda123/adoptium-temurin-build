import java.nio.file.NoSuchFileException
import java.util.regex.Matcher
/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

String javaVersion = "jdk14"

node ("master") {
  try {
    def scmVars = checkout scm
    load "${WORKSPACE}/pipelines/build/common/import_lib.groovy"

    // Load gitUri and gitBranch. These determine where we will be pulling configs from.
    def repoUri = (params.REPOSITORY_URL) ?: "https://github.com/AdoptOpenJDK/openjdk-build.git"
    def repoBranch = (params.REPOSITORY_BRANCH) ?: "master"

    // Checkout into the branch and url place
    checkout(
      [
        $class: 'GitSCM',
        branches: [[name: repoBranch ]],
        userRemoteConfigs: [[ url: repoUri ]]
      ]
    )

    // Load buildConfigurations from config file. This is what the nightlies & releases use to setup their downstream jobs
    def buildConfigurations = null
    def buildConfigPath = "${WORKSPACE}/pipelines/jobs/configurations/${javaVersion}_pipeline_config.groovy"

    // Use default config path if param is empty
    if (BUILD_CONFIG_PATH == "") {

      try {
        buildConfigurations = load buildConfigPath
      } catch (NoSuchFileException e) {
        javaVersion = javaVersion + "u"
        println "[INFO] ${buildConfigPath} does not exist, chances are we want a ${javaVersion} version.\n[INFO] Trying ${WORKSPACE}/pipelines/jobs/configurations/${javaVersion}_pipeline_config.groovy"

        buildConfigurations = load "${WORKSPACE}/pipelines/jobs/configurations/${javaVersion}_pipeline_config.groovy"
      }

    } else {

      buildConfigPath = "${WORKSPACE}/${BUILD_CONFIG_PATH}"
      buildConfigurations = load buildConfigPath

      // Since we can't check if the file is jdkxxu file or not, some regex is needed here in lieu of the try-catch above
      Matcher matcher = "$buildConfigPath" =~ /.*?(?<version>\d+u).*?/
      if (matcher.matches()) { javaVersion = javaVersion + "u" }

    }

    if (buildConfigurations == null) { throw new Exception("[ERROR] Could not find buildConfigurations for ${javaVersion}") }

    // Load targetConfigurations from config file. This is what is being run in the nightlies
    if (TARGET_CONFIG_PATH != "") {
      load "${WORKSPACE}/${TARGET_CONFIG_PATH}"
    } else {
      load "${WORKSPACE}/pipelines/jobs/configurations/${javaVersion}.groovy"
    }
    
    // Pull in other parametrised values (or use defaults if they're not defined)
    def jobRoot = (params.JOB_ROOT) ?: "build-scripts"
    def jenkinsBuildRoot = (params.JENKINS_BUILD_ROOT) ?: "https://ci.adoptopenjdk.net/job/build-scripts/"
    def jobTemplatePath = (params.JOB_TEMPLATE_PATH) ?: "pipelines/build/common/create_job_from_template.groovy"
    def scriptPath = (params.SCRIPT_PATH) ?: "pipelines/build/common/kick_off_build.groovy"
    def excludes = (params.EXCLUDES_LIST) ?: ""

    println "[INFO] Running regeneration script with the following configuration:"
    println "VERSION: $javaVersion"
    println "REPOSITORY URL: $repoUri"
    println "REPOSITORY BRANCH: $repoBranch"
    println "BUILD CONFIGURATIONS: $buildConfigurations"
    println "JOBS TO GENERATE: $targetConfigurations"
    println "JOB ROOT: $jobRoot"
    println "JENKINS ROOT: $jenkinsBuildRoot"
    println "JOB TEMPLATE PATH: $jobTemplatePath"
    println "SCRIPT PATH: $scriptPath"
    println "EXCLUDES LIST: $excludes"

    Closure regenerationScript = load "${WORKSPACE}/pipelines/build/common/config_regeneration.groovy"

    // Pass in credentials if they exist
    if (JENKINS_AUTH != "") {

      // Single quotes here are not a mistake, jenkins actually checks that it's single quoted and that the id starts/ends with '${}'
      withCredentials([
        usernamePassword(
          credentialsId: '${JENKINS_AUTH}',
          usernameVariable: 'jenkinsUsername',
          passwordVariable: 'jenkinsToken'
        )
      ]) {
        regenerationScript(
          javaVersion,
          buildConfigurations,
          targetConfigurations,
          excludes,
          currentBuild,
          this,
          jobRoot,
          repoUri,
          repoBranch,
          jobTemplatePath,
          scriptPath,
          jenkinsBuildRoot,
          jenkinsUsername,
          jenkinsToken
        ).regenerate()
      }

    } else {

      println "[WARNING] No Jenkins API Credentials have been provided! If your server does not have anonymous read enabled, you may encounter 403 api request error codes."
      regenerationScript(
        javaVersion,
        buildConfigurations,
        targetConfigurations,
        excludes,
        currentBuild,
        this,
        jobRoot,
        repoUri,
        repoBranch,
        jobTemplatePath,
        scriptPath,
        jenkinsBuildRoot,
        null,
        null
      ).regenerate()

    }

    println "[SUCCESS] All done!"

  } finally {
    // Always clean up, even on failure (doesn't delete the dsls)
    println "[INFO] Cleaning up..."
    cleanWs()
  }

}
