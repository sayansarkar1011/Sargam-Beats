// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {

    alias(libs.plugins.android.application) apply false

    id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false

}