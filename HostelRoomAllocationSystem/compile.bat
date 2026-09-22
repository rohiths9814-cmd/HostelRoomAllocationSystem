@echo off
REM Compiles every .java file in src into the bin folder.
echo Compiling...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin -cp "lib\*" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo COMPILATION FAILED - read the errors above.
    del sources.txt
    pause
    exit /b 1
)
del sources.txt
echo Compiled successfully into the bin folder.
pause
