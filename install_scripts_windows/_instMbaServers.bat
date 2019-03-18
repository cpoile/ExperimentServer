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

time /t
call mvn -pl mba877Admin package
if not "%ERRORLEVEL%" == "0" exit /b
time /t


time /t
call cp mba877/target/mba877.war c:/tomcat7/webapps/
time /t
call cp mba877Admin/target/mba877Admin.war c:/tomcat7/webapps/
time /t

