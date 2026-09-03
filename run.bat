@echo off
echo Compiling...
javac -cp .;Database\lib\sql-JDBC.jar *.java

echo.
echo Running...
java -cp .;Database\lib\sql-JDBC.jar Main
