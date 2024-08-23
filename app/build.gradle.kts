import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-android")
    id("androidx.navigation.safeargs")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

val localProperties = Properties()
localProperties.load(project.rootProject.file("local.properties").inputStream())
val openAIAPIKEY = localProperties.getProperty("openAIAPIKEY")?:""

android {
    namespace = "com.wash.washandroid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wash.washandroid"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String","KAKAO_API_KEY", getApiKey("NATIVE_API_KEY"))

        // local.properties 내부에서 naver login api key값 가져옴
        buildConfigField("String", "NAVER_CLIENT_ID", getApiKey("NAVER_CLIENT_ID"))
        buildConfigField("String", "NAVER_CLIENT_SECRET", getApiKey("NAVER_CLIENT_SECRET"))
        buildConfigField("String", "NAVER_CLIENT_NAME", getApiKey("NAVER_CLIENT_NAME"))
        buildConfigField("String", "OPENAI_API_KEY", "\"$openAIAPIKEY\"")

        // manifest에서 kakao key값 선언
        manifestPlaceholders["NATIVE_API_KEY"] = getApiKey("NATIVE_API_KEY")
        manifestPlaceholders["KAKAO_APP_KEY_SCHEME"] = getApiKey("KAKAO_APP_KEY_SCHEME")

        // API 키 값 출력
        println("NATIVE_API_KEY: ${getApiKey("NATIVE_API_KEY")}")
        println("KAKAO_APP_KEY_SCHEME: ${getApiKey("KAKAO_APP_KEY_SCHEME")}")
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
    implementation ("com.kakao.sdk:v2-all:2.20.4") // 전체 모듈 설치
    
    // CameraX core
    implementation("androidx.camera:camera-core:1.1.0-beta01")
    implementation("androidx.camera:camera-camera2:1.1.0-beta01")
    implementation("androidx.camera:camera-lifecycle:1.1.0-beta01")
    implementation("androidx.camera:camera-view:1.0.0-alpha31")
    implementation("androidx.camera:camera-extensions:1.0.0-alpha31")

    // glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.github.yalantis:ucrop:2.2.7")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.activity:activity-ktx:1.9.1")

    implementation("com.tbuonomo:dotsindicator:5.0")
    
    //pie chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // swipecards
    implementation("com.lorentzos.swipecards:library:1.0.9@aar")
}

fun getApiKey(propertyKey: String): String {
    val properties = Properties()
    properties.load(rootProject.file("local.properties").inputStream())
    return properties.getProperty(propertyKey)
}