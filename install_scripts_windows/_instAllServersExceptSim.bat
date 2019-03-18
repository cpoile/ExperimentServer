time /t
call mvn -pl models install
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call mvn -pl settings install
if not "%ERRORLEVEL%" == "0" exit /b
time /t

time /t
call mvn -pl mba877 package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

call cp mba877/target/mba877.war c:/tomcat7/webapps/

time /t
call mvn -pl mba877Admin package
if not "%ERRORLEVEL%" == "0" exit /b
time /t

call cp mba877Admin/target/mba877Admin.war c:/tomcat7/webapps/


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
call cp admin/target/admin.war c:/tomcat7/webapps/
time /t
call cp survey/target/survey.war c:/tomcat7/webapps/
time /t
call cp signup/target/signup.war c:/tomcat7/webapps/
time /t

