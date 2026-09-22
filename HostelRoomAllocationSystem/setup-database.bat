@echo off
setlocal
echo ============================================================
echo    Creating the hostel_management database
echo ============================================================
echo.
echo WARNING: this DROPS and recreates hostel_management.
echo Anything already stored in that database will be lost.
echo.
set /p CONFIRM="Type YES to continue: "
if /I not "%CONFIRM%"=="YES" goto cancelled

echo.
echo Looking for mysql.exe ...
set "MYSQL="
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-ChildItem 'C:\Program Files\MySQL','C:\Program Files (x86)\MySQL' -Recurse -Filter mysql.exe -ErrorAction SilentlyContinue ^| Select-Object -First 1).FullName" 2^>nul') do set "MYSQL=%%i"

if not defined MYSQL (
    where mysql.exe >nul 2>&1
    if not errorlevel 1 set "MYSQL=mysql.exe"
)

if not defined MYSQL (
    echo.
    echo Could not find mysql.exe on this computer.
    echo.
    echo Either MySQL Server is not installed, or it is in an unusual place.
    echo Load database\schema.sql through MySQL Workbench instead -
    echo see docs\SETUP_GUIDE.md, step 4.
    echo.
    pause
    exit /b 1
)

echo Found: %MYSQL%
echo.
set /p DBUSER="MySQL username [press ENTER for root]: "
if "%DBUSER%"=="" set DBUSER=root

echo.
echo You will now be asked for the MySQL password for '%DBUSER%'.
echo.
"%MYSQL%" -u %DBUSER% -p < database\schema.sql

if errorlevel 1 (
    echo.
    echo FAILED.
    echo  - is the MySQL service running?
    echo  - was the password correct?
    echo See docs\SETUP_GUIDE.md, troubleshooting section.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo    Database created. 8 tables and sample data are ready.
echo ============================================================
echo.
echo NEXT: put your password into
echo   src\com\hostel\util\DBConnection.java
echo then run compile.bat
echo.
pause
exit /b 0

:cancelled
echo.
echo Cancelled. Nothing was changed.
pause
exit /b 0
