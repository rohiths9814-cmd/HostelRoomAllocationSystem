@echo off
REM Starts the React development server.
REM run-api.bat must already be running in another window.
cd /d "%~dp0hostel-frontend"

if not exist node_modules (
    echo ============================================================
    echo    First run - installing the React dependencies
    echo    This needs an internet connection and takes a few minutes.
    echo ============================================================
    echo.
    call npm install
    if errorlevel 1 (
        echo.
        echo npm install FAILED. Is Node.js installed and are you online?
        pause
        exit /b 1
    )
    echo.
)

echo ============================================================
echo    Hostel Management - React frontend
echo ============================================================
echo.
echo Make sure run-api.bat is ALSO running in another window,
echo otherwise every page will show a connection error.
echo.
echo Keep this window open. Press Ctrl+C to stop.
echo.
call npm run dev
pause
