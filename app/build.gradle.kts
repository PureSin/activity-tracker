plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
}

android {
    namespace = "com.example.kelvinma.activitytracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kelvinma.activitytracker"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    lint {
        abortOnError = false
        ignoreWarnings = false
        warningsAsErrors = false
        checkReleaseBuilds = true
        checkDependencies = true
        
        htmlReport = true
        xmlReport = true
        sarifReport = true
        
        htmlOutput = file("build/reports/lint-results-debug.html")
        xmlOutput = file("build/reports/lint-results-debug.xml")
        sarifOutput = file("build/reports/lint-results-debug.sarif")
        
        disable += setOf(
            "ObsoleteLintCustomCheck",
            "LintError"
        )
        
        enable += setOf(
            "UnusedResources",
            "UnusedIds",
            "IconMissingDensityFolder",
            "IconDuplicates"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kaml)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-inline:4.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.robolectric:robolectric:4.12.1")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.8")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.mockito:mockito-android:4.8.0")
    
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Custom task to build, install, and launch the app
tasks.register("deployDebug") {
    group = "deployment"
    description = "Builds, installs, and launches the debug APK"
    
    dependsOn("assembleDebug", "installDebug")
    
    doLast {
        val packageName = android.defaultConfig.applicationId
        val activityName = "$packageName.MainActivity"
        
        exec {
            commandLine("adb", "shell", "am", "start", "-n", "$packageName/$activityName")
        }
    }
}

// Custom task for release version
tasks.register("deployRelease") {
    group = "deployment"
    description = "Builds, installs, and launches the release APK"
    
    dependsOn("assembleRelease", "installRelease")
    
    doLast {
        val packageName = android.defaultConfig.applicationId
        val activityName = "$packageName.MainActivity"
        
        exec {
            commandLine("adb", "shell", "am", "start", "-n", "$packageName/$activityName")
        }
    }
}