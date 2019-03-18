time /t
call mvn -pl simulation package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

call cp simulation/target/simulation.war c:/Tomcat7/webapps/
time /t