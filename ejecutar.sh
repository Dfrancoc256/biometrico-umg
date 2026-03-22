#!/bin/bash
# ============================================================
#  Sistema Biometrico UMG - Sede La Florida, Zona 19
#  Script de ejecucion para Linux / macOS
# ============================================================

echo ""
echo "  Universidad Mariano Galvez - Sistema Biometrico"
echo "  ================================================"
echo ""

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "  ERROR: Java no esta instalado."
    echo "  En Ubuntu/Debian:  sudo apt install openjdk-21-jdk"
    echo "  En macOS:          brew install openjdk@21"
    echo "  Descarga general:  https://adoptium.net/es/"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 19 ] 2>/dev/null; then
    echo "  ADVERTENCIA: Se requiere Java 19 o superior. Version actual: $JAVA_VER"
fi

# Verificar JAR
JAR="registro-biometrico-2.0-2026-all.jar"
if [ ! -f "$JAR" ]; then
    echo "  ERROR: No se encontro $JAR"
    echo "  Ejecute este script desde la carpeta donde esta el JAR."
    exit 1
fi

# Verificar configuracion
if [ ! -f "configuracion.properties" ]; then
    echo "  ADVERTENCIA: No se encontro configuracion.properties"
fi

echo "  Iniciando aplicacion..."
echo ""

java -jar "$JAR"

EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo ""
    echo "  La aplicacion termino con el codigo de error: $EXIT_CODE"
fi
