time /t
call mvn -pl settings install -pl models install -pl admin install -pl signup install -pl survey install
if not "%ERRORLEVEL%" == "0" exit /b
time /t
