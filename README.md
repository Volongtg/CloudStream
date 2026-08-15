HHKungfu CloudStream build fixes

This revision aligns the project with the current CloudStream Gradle plugin.
- Android Gradle Plugin: 9.1.1
- Gradle: 9.3.1
- Kotlin: 2.4.0
- CloudStream Gradle plugin: recloudstream/gradle commit 32895ae
- CloudStream API dependency uses the plugin's `cloudstream` configuration instead of the retired `apk` configuration.
