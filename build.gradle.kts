buildscript {
    //apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/dev/remote3.gradle'
    apply(from = "https://raw.githubusercontent.com/hss01248/flipperUtil/dev/remote3.gradle")
    apply(from = "https://raw.githubusercontent.com/hss01248/flipperUtil/master/deps/depsLastestChecker.gradle")
    apply(from = "https://raw.githubusercontent.com/hss01248/flipperUtil/dev/z_config/git_branch_info.gradle")
    apply(from = "https://raw.githubusercontent.com/skyNet2017/AppUpdate/master/uploadToPyger.gradle?a=2")
    //    apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/master/deps/depsLastestChecker.gradle'
    //    apply from: 'https://raw.githubusercontent.com/hss01248/flipperUtil/dev/z_config/git_branch_info.gradle'
    //apply from:'https://raw.githubusercontent.com/skyNet2017/AppUpdate/master/uploadToPyger.gradle?a=1'
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(ClassPath.FIREBASE_CRASH)
        classpath(ClassPath.GOOGLE_SERVICE)
        classpath(ClassPath.GRADLE_KOTLIN)
        classpath(ClassPath.GRADLE_MAVEN)
        classpath(ClassPath.GRADLE)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven { url = uri(Uri.JITPACK_IO) }
        maven { url = uri(Uri.ALIYUN_MAVEN) }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}