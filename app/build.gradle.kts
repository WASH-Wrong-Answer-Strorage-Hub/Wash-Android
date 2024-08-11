import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.wash.washandroid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wash.washandroid"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties 내부에서 kakao login api key값 가져옴
        buildConfigField("String","KAKAO_API_KEY", getApiKey("NATIVE_API_KEY"))

        // local.properties 내부에서 naver login api key값 가져옴
        buildConfigField("String", "NAVER_CLIENT_ID", getApiKey("NAVER_CLIENT_ID"))
        buildConfigField("String", "NAVER_CLIENT_SECRET", getApiKey("NAVER_CLIENT_SECRET"))
        buildConfigField("String", "NAVER_CLIENT_NAME", getApiKey("NAVER_CLIENT_NAME"))
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // lottie 애니메이션
    implementation ("com.airbnb.android:lottie:6.3.0")
    // naver login
    implementation("com.navercorp.nid:oauth-jdk8:5.9.1")
    // kakao login
    implementation("com.kakao.sdk:v2-user:2.10.0")
    // glide 추가
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}

fun getApiKey(propertyKey: String): String {
    val properties = Properties()
    properties.load(rootProject.file("local.properties").inputStream())
    return properties.getProperty(propertyKey)
}