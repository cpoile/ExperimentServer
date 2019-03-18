time /t
call mvn -pl settings install -pl models install
if not "%ERRORLEVEL%" == "0" exit /b
time /t