plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.compose.compiler") version "2.0"
}

android {
    //...
    composeOptions {
        compilerVersion = "2.0"
    }
}