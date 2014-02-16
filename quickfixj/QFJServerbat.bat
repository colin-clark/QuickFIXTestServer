@echo off

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set QFJ_BIN=%~dp0
set DEFAULT_QFJ_HOME=%QFJ_BIN%..\\..\\..

if "%QFJ_HOME%"=="" set QFJ_HOME=%DEFAULT_QFJ_HOME%
set DEFAULT_QFJ_HOME=

if not "%QFJ_HOME%"=="" goto qfjHomeOk
echo QFJ_HOME must set manually for older versions of Windows. Please set QFJ_HOME.
goto end
:qfjHomeOk

set CP="%QFJ_HOME%/testserver.jar;%QFJ_HOME%/core/src/main/lib/mina-core-1.1.7.jar;%QFJ_HOME%/core/src/main/lib/slf4j-api-1.5.3.jar;%QFJ_HOME%/core/src/main/lib/slf4j-jdk14-1.5.3.jar;%QFJ_HOME%/core/target/quickfixj-all-4.2.jar;%QFJ_HOME%/core/target/quickfixj-core-4.2.jar;%QFJ_HOME%/core/target/quickfixj-msg-fix40-4.2.jar;%QFJ_HOME%/core/target/quickfixj-msg-fix41-4.2.jar;%QFJ_HOME%/core/target/quickfixj-msg-fix42-4.2.jar;%QFJ_HOME%/core/target/quickfixj-msg-fix43-4.2.jar;%QFJ_HOME%/core/target/quickfixj-msg-fix44-4.2.jar;%QFJ_HOME%/core/target/quickfixj-msg-fix50-4.2.jar;%QFJ_HOME%/core/target/quickfixj-msg-fixt11-4.2.jar;%QFJ_HOME%/examples/target/quickfixj-examples-4.2.jar"

java -cp %CP% com.cep.quickfix.server.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
