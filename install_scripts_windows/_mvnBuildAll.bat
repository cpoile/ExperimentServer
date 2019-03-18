time /t
call mvn install
if not "%ERRORLEVEL%" == "0" exit /b
time /t

cp admin/target/admin.war c:/tomcat7/webapps/
cp survey/target/survey.war c:/tomcat7/webapps/
cp signup/target/signup.war c:/tomcat7/webapps/