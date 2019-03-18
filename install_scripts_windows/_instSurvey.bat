time /t
call mvn -pl survey package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

call cp survey/target/survey.war c:/Tomcat7/webapps/
time /t
