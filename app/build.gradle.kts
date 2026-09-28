plugins {
    id("com.android.application")
}

android {
    namespace = "com.oshanrube.gatecontroller"
    // Android 16. Play requires new apps to target it, and androidx.browser 1.9 (from
    // androidbrowserhelper 2.6.2) needs it to compile against; Bubblewrap's template does both.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.oshanrube.gatecontroller"
        minSdk = 26
        targetSdk = 36
        // Play needs a higher versionCode on every upload; CI passes its run number.
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = "1.0.0"
    }

    // The Play upload key, at UPLOAD_KEYSTORE_PATH (CI decodes it there from the
    // UPLOAD_KEYSTORE_BASE64 secret on master and manual runs only). Without it, release builds
    // fall back to the debug key: fine for sideloading, refused by Play.
    val uploadKeystore = System.getenv("UPLOAD_KEYSTORE_PATH")?.let { file(it) }?.takeIf { it.exists() }
    signingConfigs {
        if (uploadKeystore != null) {
            // An unset secret reaches the build as an empty string, not as null.
            fun secret(name: String) = System.getenv(name)?.takeIf { it.isNotBlank() }
            val storePass = secret("UPLOAD_KEYSTORE_PASSWORD")
                ?: throw GradleException("UPLOAD_KEYSTORE_PATH is set but UPLOAD_KEYSTORE_PASSWORD is missing")
            create("upload") {
                storeFile = uploadKeystore
                storePassword = storePass
                keyAlias = secret("UPLOAD_KEY_ALIAS") ?: "upload"
                keyPassword = secret("UPLOAD_KEY_PASSWORD") ?: storePass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Trusted Web Activity: Chrome shows the site full screen, with its login, service worker
    // and push notifications. The version Bubblewrap (Google's own TWA generator) pins.
    implementation("com.google.androidbrowserhelper:androidbrowserhelper:2.6.2")
}
