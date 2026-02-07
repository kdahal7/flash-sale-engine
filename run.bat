@echo off
echo.
echo ================================
echo   FLASH SALE ENGINE
echo ================================
echo.
echo Starting application...
echo.
echo API: http://localhost:8080
echo Health: http://localhost:8080/actuator/health
echo.

cd /d "%~dp0"
java -jar target\flash-sale-engine-1.0.0.jar --spring.datasource.password=k6226

pause
