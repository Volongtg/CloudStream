@echo off
setlocal
set "GRADLE_VERSION=7.4.2"
if "%GRADLE_USER_HOME%"=="" set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "DIST_BASE=%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin"
set "INSTALL_DIR=%DIST_BASE%\gradle-%GRADLE_VERSION%"
set "ZIP_PATH=%DIST_BASE%\gradle-%GRADLE_VERSION%-bin.zip"
set "DIST_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%INSTALL_DIR%\bin\gradle.bat" (
  if not exist "%DIST_BASE%" mkdir "%DIST_BASE%"
  if not exist "%ZIP_PATH%" (
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing '%DIST_URL%' -OutFile '%ZIP_PATH%'"
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP_PATH%' '%DIST_BASE%'"
)

call "%INSTALL_DIR%\bin\gradle.bat" %*
