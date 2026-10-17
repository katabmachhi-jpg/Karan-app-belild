package com.example.builder

object GitHubWorkflowGenerator {

    fun generateWorkflowFile(): String {
        return """
name: Build Android APK (Karan AI App Builder)

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout Repository
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Grant Execute Permission for Gradlew
      run: chmod +x gradlew || true

    - name: Build with Gradle
      run: ./gradlew assembleDebug --stacktrace

    - name: Upload Debug APK Artifact
      uses: actions/upload-artifact@v4
      with:
        name: karan-ai-app-debug
        path: app/build/outputs/apk/debug/*.apk
        retention-days: 14
        """.trimIndent()
    }
}
