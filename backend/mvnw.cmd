@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "MVN_CMD=mvn") ELSE (SET "MVN_CMD=%__MVNW_ARG0_NAME__%")
@SET MAVEN_PROJECTBASEDIR=%~dp0

@SET MAVEN_USER_HOME=%USERPROFILE%\.m2
@SET MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
@SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

@REM Read distribution URL
@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
  @IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

@IF EXIST "%MAVEN_WRAPPER_JAR%" GOTO runMavenWithJavaHome

@REM Download maven-wrapper.jar if needed
@SET WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
@echo Downloading Maven Wrapper...
@powershell -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%MAVEN_WRAPPER_JAR%')"

:runMavenWithJavaHome
@IF NOT "%JAVA_HOME%"=="" SET JAVA_CMD=%JAVA_HOME%\bin\java.exe

@IF NOT EXIST "%JAVA_CMD%" SET JAVA_CMD=java

@REM Download Maven distribution if needed
@SET LOCAL_REPO=%MAVEN_USER_HOME%\wrapper\dists
@SET DIST_NAME=apache-maven-3.9.6
@SET DIST_ZIP=%LOCAL_REPO%\%DIST_NAME%\%DIST_NAME%-bin.zip
@SET DIST_DIR=%LOCAL_REPO%\%DIST_NAME%\%DIST_NAME%

@IF EXIST "%DIST_DIR%\bin\mvn.cmd" GOTO runMaven

@IF NOT EXIST "%LOCAL_REPO%\%DIST_NAME%" MD "%LOCAL_REPO%\%DIST_NAME%"
@echo Downloading Apache Maven 3.9.6 (primera vez, puede tardar ~1 minuto)...
@powershell -Command "(New-Object Net.WebClient).DownloadFile('%DISTRIBUTION_URL%', '%DIST_ZIP%')"
@powershell -Command "Expand-Archive -Path '%DIST_ZIP%' -DestinationPath '%LOCAL_REPO%\%DIST_NAME%'"

:runMaven
@"%JAVA_CMD%" -classpath "%MAVEN_WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %DIST_DIR% %*
@IF "%ERRORLEVEL%"=="0" EXIT /B 0

@REM Fallback: use system mvn
@mvn %*
