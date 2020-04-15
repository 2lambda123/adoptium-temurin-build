class Config8 {
  final Map<String, Map<String, ?>> buildConfigurations = [
        x64Mac        : [
                os                  : 'mac',
                arch                : 'x64',
                additionalNodeLabels: [
                        hotspot : 'build-macstadium-macos1010-1',
                        corretto: 'build-macstadium-macos1010-1',
                        openj9  : 'build-macstadium-macos1010-2'
                ],
                test                : false
        ],

        x64MacXL      : [
                os                   : 'mac',
                arch                 : 'x64',
                additionalNodeLabels : 'build-macstadium-macos1010-2',
                test                 : false,
                additionalFileNameTag: "macosXL",
                configureArgs        : '--with-noncompressedrefs'
        ],

        x64Linux      : [
                os                  : 'linux',
                arch                : 'x64',
                additionalNodeLabels: 'centos6',
                test                : false,
                configureArgs       : [
                        "hotspot-jfr" : '--enable-jfr',
                        "openj9"      : '--enable-jitserver'
                ]
        ],

        // Currently we have to be quite specific about which windows to use as not all of them have freetype installed
        x64Windows    : [
                os                  : 'windows',
                arch                : 'x64',
                additionalNodeLabels: [
                        hotspot : 'win2012',
                        corretto: 'win2012',
                        openj9  : 'win2012&&mingw-cygwin'
                ],
                test                : false
        ],

        x64WindowsXL    : [
                os                   : 'windows',
                arch                 : 'x64',
                additionalNodeLabels : 'win2012&&mingw-cygwin',
                test                 : false,
                additionalFileNameTag: "windowsXL",
                configureArgs        : '--with-noncompressedrefs'
        ],

        x32Windows    : [
                os                  : 'windows',
                arch                : 'x86-32',
                additionalNodeLabels: [
                        hotspot : 'win2012',
                        corretto: 'win2012',
                        openj9  : 'win2012&&mingw-cygwin'
                ],
                buildArgs : [
                        hotspot : '--jvm-variant client,server'
                ],
                test                : false
        ],

        ppc64Aix      : [
                os  : 'aix',
                arch: 'ppc64',
                additionalNodeLabels: 'xlc13',
                test: false
        ],

        s390xLinux    : [
                os  : 'linux',
                arch: 's390x',
                test: false
        ],

        sparcv9Solaris: [
                os  : 'solaris',
                arch: 'sparcv9',
                test: false
        ],

        x64Solaris    : [
                os                  : 'solaris',
                arch                : 'x64',
                test                : false
        ],

        ppc64leLinux  : [
                os  : 'linux',
                arch: 'ppc64le',
                test: false,
                configureArgs       : [
                        "openj9"      : '--enable-jitserver'
                ]
        ],

        arm32Linux    : [
                os  : 'linux',
                arch: 'arm',
                // TODO Temporarily remove the ARM tests because we don't have fast enough hardware
                //test                : ['sanity.openjdk']
                test: false
        ],

        aarch64Linux  : [
                os                  : 'linux',
                arch                : 'aarch64',
                additionalNodeLabels: 'centos7',
                test                : false
        ],

        x64LinuxXL       : [
                os                   : 'linux',
                additionalNodeLabels : 'centos6',
                arch                 : 'x64',
                additionalFileNameTag: "linuxXL",
                configureArgs        : '--with-noncompressedrefs --enable-jitserver',
                test                 : false
        ],
        s390xLinuxXL       : [
                os                   : 'linux',
                arch                 : 's390x',
                additionalFileNameTag: "linuxXL",
                test                 : false,
                configureArgs        : '--with-noncompressedrefs'
        ],
        ppc64leLinuxXL       : [
                os                   : 'linux',
                arch                 : 'ppc64le',
                additionalFileNameTag: "linuxXL",
                test                 : false,
                configureArgs        : '--with-noncompressedrefs --enable-jitserver'
        ],
  ]

}

Config8 config = new Config8()
return config.buildConfigurations