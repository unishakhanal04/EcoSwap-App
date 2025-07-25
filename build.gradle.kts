//import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
//
//// Top-level build file where you can add configuration options common to all sub-projects/modules.
//
//// You can optionally keep these commented-out plugin aliases if you're using `libs.versions.toml`
//// plugins {
////     alias(libs.plugins.android.application) apply false
////     alias(libs.plugins.kotlin.android) apply false
////     alias(libs.plugins.kotlin.compose) apply false
//// }
//
//
//
//plugins {
//    id("com.google.gms.google-services") version "4.4.0" apply false
//}
//classpath("com.google.gms:google-services:4.4.0")

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
}
