@echo off
setlocal

set "APP_HOME=%~dp0"
set "GRADLE_VERSION=9.5.0"
set "DIST_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
set "DIST_SHA256=553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746"
if defined GRADLE_USER_HOME (
    set "CACHE_ROOT=%GRADLE_USER_HOME%\wrapper\dists\onestep-gradle-%GRADLE_VERSION%"
) else (
    set "CACHE_ROOT=%USERPROFILE%\.gradle\wrapper\dists\onestep-gradle-%GRADLE_VERSION%"
)
set "DIST_ZIP=%CACHE_ROOT%\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_BIN=%CACHE_ROOT%\gradle-%GRADLE_VERSION%\bin\gradle.bat"

if exist "%GRADLE_BIN%" goto execute

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$root='%CACHE_ROOT%'; $zip='%DIST_ZIP%'; $url='%DIST_URL%'; $expected='%DIST_SHA256%';" ^
  "New-Item -ItemType Directory -Path $root -Force | Out-Null;" ^
  "$valid=(Test-Path -LiteralPath $zip) -and ((Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLower() -eq $expected);" ^
  "if (-not $valid) { $tmp=$zip+'.tmp'; Remove-Item $tmp,$zip -Force -ErrorAction SilentlyContinue; Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $tmp; if ((Get-FileHash -LiteralPath $tmp -Algorithm SHA256).Hash.ToLower() -ne $expected) { Remove-Item $tmp -Force; throw 'Gradle distribution checksum verification failed.' }; Move-Item $tmp $zip -Force };" ^
  "$destination=Join-Path $root 'gradle-%GRADLE_VERSION%'; Remove-Item $destination -Recurse -Force -ErrorAction SilentlyContinue; Expand-Archive -LiteralPath $zip -DestinationPath $root -Force"
if errorlevel 1 exit /b 1

:execute
call "%GRADLE_BIN%" -p "%APP_HOME%" %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
