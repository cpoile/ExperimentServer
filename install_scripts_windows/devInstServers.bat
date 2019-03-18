time /t
call mvn -pl admin package
if not "%ERRORLEVEL%" == "0" exit /b
time /t
call mvn -pl simulation package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call cp admin/target/admin.war c:/Tomcat7-Dev/webapps/
time /t
call cp simulation/target/simulation.war c:/Tomcat7-Dev/webapps/
time /t

