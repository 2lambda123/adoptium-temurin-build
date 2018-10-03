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

def buildConfigurations = [
        x64Mac    : [
                operatingSystem     : 'mac',
                arch                : 'x64',
                bootJDK             : "/Users/jenkins/tools/hudson.model.JDK/JDK9.0.1",
                additionalNodeLabels: 'build-macstadium-macos1010-1',
                test                : ['openjdktest', 'systemtest']
        ],
        x64Linux  : [
                operatingSystem     : 'linux',
                arch                : 'x64',
                additionalNodeLabels: 'centos6',
                test                : ['openjdktest', 'systemtest', 'externaltest']
        ],

        // Currently we have to be quite specific about which windows to use as not all of them have freetype installed
        x64Windows: [
                operatingSystem     : 'windows',
                arch                : 'x64',
                additionalNodeLabels: 'win2012',
                test                : ['openjdktest']
        ],

        ppc64Aix    : [
                operatingSystem     : 'aix',
                arch                : 'ppc64',
                test                : false
        ],

        s390xLinux    : [
                operatingSystem     : 'linux',
                arch                : 's390x',
                additionalNodeLabels: [
                        hotspot: 'rhel7'
                ],
                test                : ['openjdktest', 'systemtest']
        ],

        ppc64leLinux    : [
                operatingSystem     : 'linux',
                arch                : 'ppc64le',
                additionalNodeLabels: [
                        // Pinned as at time of writing build-osuosl-centos74-ppc64le-2 does not have a valid boot jdk
                        hotspot: 'centos7&&build-osuosl-centos74-ppc64le-1',
                        openj9:  'ubuntu'
                ],
                test                : ['openjdktest', 'systemtest']
        ],

        arm32Linux    : [
                operatingSystem     : 'linux',
                arch                : 'arm',
                test                : ['openjdktest']
        ],

        aarch64Linux    : [
                operatingSystem     : 'linux',
                arch                : 'aarch64',
                additionalNodeLabels: 'centos7',
                test                : ['openjdktest']
        ],

        /*
        "x86-32Windows"    : [
                operatingSystem    : 'windows',
                arch               : 'x86-32',
                additionalNodeLabels: 'win2012&&x86-32',
                test                : false
        ],
        */
        "linuxXL"    : [
                operatingSystem      : 'linux',
                additionalNodeLabels : 'centos6',
                arch                 : 'x64',
                test                 : false,
                additionalFileNameTag: "linuxXL",
                configureArgs        : '--with-noncompressedrefs'
        ],
]

def forestName = "jdk10u"

node ("master") {
    def scmVars = checkout scm
    def buildFile = load "${WORKSPACE}/pipelines/build/build_base_file.groovy"
    buildFile.doBuild(forestName, buildConfigurations, targetConfigurations, enableTests, publish, releaseTag, branch, additionalConfigureArgs, scmVars, additionalBuildArgs)
}
