time /t
call mvn -pl admin package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call mvn -pl survey package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call mvn -pl signup package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call mvn -pl simulation package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call cp admin/target/admin.war c:/tomcat7/webapps/
time /t
call cp survey/target/survey.war c:/tomcat7/webapps/
time /t
call cp signup/target/signup.war c:/tomcat7/webapps/
time /t
call cp simulation/target/simulation.war c:/tomcat7/webapps/
time /t
