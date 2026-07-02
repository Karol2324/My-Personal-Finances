plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mypersonalfinances"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.mypersonalfinances"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // Proyecto 100% Java: define la versión del bytecode que generará el compilador
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ViewBinding genera clases de enlace (ej. ActivityMainBinding) a partir de los XML
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // UI clásica con AppCompat y componentes Material (FAB, TextInputLayout, etc.)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // RecyclerView para el listado de transacciones con Adapter personalizado
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}