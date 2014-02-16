@echo off

REM Scripts for the QFJ binary distribution

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set QFJ_BIN=%~dp0
set DEFAULT_QFJ_HOME=%QFJ_BIN%..

if "%QFJ_HOME%"=="" set QFJ_HOME=%DEFAULT_QFJ_HOME%
set DEFAULT_QFJ_HOME=

if not "%QFJ_HOME%"=="" goto qfjHomeOk
echo QFJ_HOME must set manually for older versions of Windows. Please set QFJ_HOME.
goto end
:qfjHomeOk

set CP=%QFJ_HOME%/lib/mina-core-1.1.0.jar;%QFJ_HOME%/lib/slf4j-api-1.3.0.jar;%QFJ_HOME%/lib/slf4j-jdk14-1.3.0.jar;%QFJ_HOME%/quickfixj-core.jar;%QFJ_HOME%/quickfixj-msg-fix40.jar;%QFJ_HOME%/quickfixj-msg-fix41.jar;%QFJ_HOME%/quickfixj-msg-fix42.jar;%QFJ_HOME%/quickfixj-msg-fix43.jar;%QFJ_HOME%/quickfixj-msg-fix44.jar;%QFJ_HOME%/quickfixj-examples.jar

java -cp %CP% quickfix.examples.banzai.Banzai %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
