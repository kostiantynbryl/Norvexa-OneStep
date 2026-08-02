@echo off
setlocal
set "APP_HOME=%~dp0"
set "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "WRAPPER_URL=https://services.gradle.org/distributions/gradle-9.5.0-wrapper.jar"
set "WRAPPER_SHA256=497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7"

if not exist "%WRAPPER_JAR%" goto downloadWrapper
for /f "usebackq tokens=*" %%H in (`powershell -NoProfile -Command "(Get-FileHash -LiteralPath '%WRAPPER_JAR%' -Algorithm SHA256).Hash.ToLower()"`) do set "ACTUAL_SHA256=%%H"
if /I "%ACTUAL_SHA256%"=="%WRAPPER_SHA256%" goto wrapperReady

del /q "%WRAPPER_JAR%" >nul 2>&1

:downloadWrapper
if not exist "%APP_HOME%gradle\wrapper" mkdir "%APP_HOME%gradle\wrapper"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%.tmp'; $hash=(Get-FileHash -LiteralPath '%WRAPPER_JAR%.tmp' -Algorithm SHA256).Hash.ToLower(); if ($hash -ne '%WRAPPER_SHA256%') { Remove-Item '%WRAPPER_JAR%.tmp' -Force; throw 'Gradle Wrapper checksum verification failed.' }; Move-Item '%WRAPPER_JAR%.tmp' '%WRAPPER_JAR%' -Force"
if errorlevel 1 exit /b 1

:wrapperReady
if defined JAVA_HOME goto findJavaFromJavaHome
set "JAVA_EXE=java.exe"
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute
echo ERROR: JAVA_HOME is not set and no java command was found.
exit /b 1

:findJavaFromJavaHome
set "JAVA_HOME=%JAVA_HOME:"=%"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if exist "%JAVA_EXE%" goto execute
echo ERROR: JAVA_HOME points to an invalid directory: %JAVA_HOME%
exit /b 1

:execute
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=gradlew" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
