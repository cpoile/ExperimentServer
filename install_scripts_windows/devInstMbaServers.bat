time /t
call mvn -pl settings install -pl models install
if not "%ERRORLEVEL%" == "0" exit /b
time /t
call mvn -pl mba877Admin package
if not "%ERRORLEVEL%" == "0" exit /b
time /t
call mvn -pl mba877 package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call cp mba877Admin/target/mba877Admin.war c:/Tomcat7-Dev/webapps/
time /t
call cp mba877/target/mba877.war c:/Tomcat7-Dev/webapps/
time /t

