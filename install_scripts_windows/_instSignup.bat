time /t
call mvn -pl signup package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

cp signup/target/signup.war c:/tomcat7/webapps/