@echo off
REM ============================================================
REM  Sistema Biometrico UMG - Sede La Florida, Zona 19
REM  Script de ejecucion para Windows
REM ============================================================

title Sistema Biometrico UMG

echo.
echo  Universidad Mariano Galvez - Sistema Biometrico
echo  ================================================
echo.

REM Verificar que Java este instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo  ERROR: Java no esta instalado o no esta en el PATH.
    echo  Descargue Java 21 desde: https://adoptium.net/es/
    echo.
    pause
    exit /b 1
)

REM Verificar que el JAR existe
if not exist "registro-biometrico-2.0-2026-all.jar" (
    echo  ERROR: No se encontro el archivo registro-biometrico-2.0-2026-all.jar
    echo  Asegurese de ejecutar este script desde la carpeta correcta.
    echo.
    pause
    exit /b 1
)

REM Verificar configuracion.properties
if not exist "configuracion.properties" (
    echo  ADVERTENCIA: No se encontro configuracion.properties
    echo  Se usara la configuracion predeterminada.
    echo.
)

echo  Iniciando aplicacion...
echo.

java -jar registro-biometrico-2.0-2026-all.jar

if %errorlevel% neq 0 (
    echo.
    echo  La aplicacion termino con un error.
    echo  Revise la consola para mas detalles.
    pause
)
