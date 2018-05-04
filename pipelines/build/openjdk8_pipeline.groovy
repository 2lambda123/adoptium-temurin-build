def buildConfigurations = [
        mac    : [os: 'mac', arch: 'x64', aditionalNodeLabels: 'build'],
        linux  : [os: 'centos6', arch: 'x64', aditionalNodeLabels: 'build'],

        // Currently we have to be quite specific about which windows to use as not all of them have freetype installed
        windows: [os: 'windows', arch: 'x64', aditionalNodeLabels: 'build&&win2008']
]

if (osTarget != "all") {
    buildConfigurations = buildConfigurations
            .findAll { it.key == osTarget }
}

def buildJobs = []
def jobs = [:]

buildConfigurations.each { buildConfiguration ->
    def configuration = buildConfiguration.value

    def buildType = "${configuration.os}-${configuration.arch}"

    jobs[buildType] = {
        stage("build-${buildType}") {
            def buildJob = build job: "openjdk8_build-refactor", parameters: [[$class: 'LabelParameterValue', name: 'NODE_LABEL', label: "${configuration.aditionalNodeLabels}&&${configuration.os}&&${configuration.arch}"]]
            buildJobs.add([
                    job        : buildJob,
                    config     : configuration,
                    targetLabel: buildConfiguration.key
            ]);
        }

    }
}
parallel jobs

node('centos6&&x64&&build') {
    buildJobs.each {
        buildJob ->
            if (buildJob.job.getResult() == 'SUCCESS') {
                copyArtifacts(
                        projectName: 'openjdk8_build-refactor',
                        selector: specific("${buildJob.job.getNumber()}"),
                        filter: 'workspace/target/*',
                        fingerprintArtifacts: true,
                        target: "target/${buildJob.targetLabel}/${buildJob.config.arch}/",
                        flatten: true)
            }
    }

    archiveArtifacts artifacts: 'target/*/*/*'
}
