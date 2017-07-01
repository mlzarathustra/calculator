@echo off

if ""=="%1" goto noArg

zip %1 *.java eval\action\*.java eval\*.java

md a:\eval
md ..\_backups\eval

copy %1.zip a:\eval
move %1.zip ..\_backups\eval
dir /od ..\_backups\eval

goto out


:noArg

echo Please enter an argument (eg. backup 29dec97)
echo.

:out

