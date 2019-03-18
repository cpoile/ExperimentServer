time /t
call mvn install
if not "%ERRORLEVEL%" == "0" exit /b
time /t

call cp admin/target/admin.war c:/Tomcat7-Dev/webapps/
time /t
call cp survey/target/survey.war c:/Tomcat7-Dev/webapps/
time /t
call cp signup/target/signup.war c:/Tomcat7-Dev/webapps/
time /t
call cp simulation/target/simulation.war c:/Tomcat7-Dev/webapps/
time /t