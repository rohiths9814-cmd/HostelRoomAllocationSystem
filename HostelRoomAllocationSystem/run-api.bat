@echo off
REM Starts the REST API server that the React frontend talks to.
REM Leave this window OPEN while you use the web interface.
echo ============================================================
echo    Hostel Management - REST API server
echo    http://localhost:8080
echo ============================================================
echo.
echo Keep this window open. Press Ctrl+C to stop the server.
echo.
java -cp "bin;lib\*" com.hostel.server.ApiServer
echo.
echo The server has stopped.
pause
