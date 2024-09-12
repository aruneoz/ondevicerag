import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

val geminiKey: String = gradleLocalProperties(rootDir, providers).getProperty("geminiKey")

android {
    namespace = "com.ml.arunneo.docqa"
    compileSdk = 34

    packaging {
        resources {
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/NOTICE.md"
            pickFirsts += "README"
        }

    }

    defaultConfig {
        applicationId = "com.ml.arunneo.docqa"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        // Add the field 'geminiKey' in the build config
        // See https://stackoverflow.com/a/60474096/13546426
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField( "String" , "geminiKey" , geminiKey)
        }
        debug {
            buildConfigField( "String" , "geminiKey" , geminiKey)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3.icons.extended)
    implementation(libs.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Apache POI
    implementation(libs.apache.poi)
    implementation(libs.apache.poi.ooxml)

    // Sentence Embeddings
    // https://github.com/shubham0204/Sentence-Embeddings-Android
    implementation("com.github.shubham0204:Sentence-Embeddings-Android:0.0.3")
    implementation ("com.google.mediapipe:tasks-genai:0.10.14")
    // iTextPDF - for parsing PDFs
    implementation("com.itextpdf:itextpdf:5.5.13.3")

    // ObjectBox - vector database
    debugImplementation("io.objectbox:objectbox-android-objectbrowser:4.0.0")
    releaseImplementation("io.objectbox:objectbox-android:4.0.0")

    // Gemini SDK - LLM
    implementation("com.google.ai.client.generativeai:generativeai:0.6.0")

    // Langchain4j - RAG
    implementation("dev.langchain4j:langchain4j:0.34.0")
    implementation("dev.langchain4j:langchain4j-easy-rag:0.34.0")
    implementation ("dev.langchain4j:langchain4j-hugging-face:0.34.0")
    implementation("ai.djl.huggingface:tokenizers:0.26.0")
    //implementation ("dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:0.34.0")
    implementation ("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation ("dev.langchain4j:langchain4j-document-parser-apache-pdfbox:0.34.0")




    // compose-markdown
    // https://github.com/jeziellago/compose-markdown
    implementation("com.github.jeziellago:compose-markdown:0.5.0")

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}


configurations {
    all {
        exclude(group = "com.microsoft.onnxruntime", module = "onnxruntime")
        exclude(group = "commons-logging" , module ="commons-logging")
        exclude(group = "org.bouncycastle", module="bcprov-jdk15to18")
        exclude(group = "org.bouncycastle", module="bcutil-jdk18on")
        exclude(group = "org.bouncycastle", module="bcpkix-jdk18on")
    }
}



apply(plugin = "io.objectbox")