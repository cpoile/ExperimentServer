time /t
call mvn -pl admin package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

call cp admin/target/admin.war c:/Tomcat7-Dev/webapps/
time /t
