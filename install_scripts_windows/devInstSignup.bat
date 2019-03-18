time /t
call mvn -pl signup package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

call cp signup/target/signup.war c:/Tomcat7-Dev/webapps/
time /t