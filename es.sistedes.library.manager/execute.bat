@ECHO OFF

REM 
REM Copyright (c) 2023 Sistedes
REM All rights reserved. This program and the accompanying materials
REM are made available under the terms of the Eclipse Public License v2.0
REM which accompanies this distribution, and is available at
REM http://www.eclipse.org/legal/epl-v20.html
REM
REM Contributors:
REM Abel Gómez - initial API and implementation
REM

REM 
REM NOTE ABOUT THIS SCRIPT:
REM
REM We use a batch script (a shell script could be used as well) because for 
REM some subcommands it is necessary to read from stdin in interactive mode.
REM Initially, I considered to use Ant. However, Ant and other utilities have 
REM problems when trying to read from stdin (and moreover, there seems to
REM be some issue with the "javax.management.MBeanTrustPermission" permission
REM and Ant which causes exceptions in some cases).
REM
REM As a cons==ence, we use this script as a simple but fair enough solution to
REM run all the r==ired commands in an automated and easy way.
REM

setlocal

CALL execute.env.bat

CALL mvn package

IF "%1"=="init" GOTO INIT
IF "%1"=="list" GOTO LIST
IF "%1"=="sync-authors" GOTO SYNC_AUTHORS
IF "%1"=="publish" GOTO PUBLISH
IF "%1"=="validate" GOTO VALIDATE
IF "%1"=="discard-uuids" GOTO DISCARD

ECHO ERROR: Must provide exactly one of the following subcommands:
ECHO init, list, sync-authors, publish, validate, discard-uuids

GOTO END


:INIT
SET COMMON_OPTS=init -y %YEAR% -P %HANDLE_PREFIX%
ECHO Initializing JISBD
java -jar target/%JAR% %COMMON_OPTS% -a %JISBD% -i "%INPUT_DIR%/%JISBD%" -o "%OUTPUT_DIR%/%JISBD%" -p %JISBD_PDF_PATTERN% -x %JISBD_XLSX% %JISBD_INIT_ARGS% || GOTO FAIL
ECHO Initializing JCIS
java -jar target/%JAR% %COMMON_OPTS% -a %JCIS%  -i "%INPUT_DIR%/%JCIS%"  -o "%OUTPUT_DIR%/%JCIS%"  -p  %JCIS_PDF_PATTERN% -x %JCIS_XLSX%  %JCIS_INIT_ARGS%  || GOTO FAIL
ECHO Initializing PROLE
java -jar target/%JAR% %COMMON_OPTS% -a %PROLE% -i "%INPUT_DIR%/%PROLE%" -o "%OUTPUT_DIR%/%PROLE%" -p %PROLE_PDF_PATTERN% -x %PROLE_XLSX% %PROLE_INIT_ARGS% || GOTO FAIL
GOTO END

:LIST
SET COMMON_OPTS=list -t -n -e -o
ECHO Listing JISBD
java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE% || GOTO FAIL
ECHO Listing JCIS
java -jar target/%JAR% %COMMON_OPTS% -f %JCIS_EDITION_FILE%  || GOTO FAIL
ECHO Listing PROLE
java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE% || GOTO FAIL
GOTO END


:SYNC_AUTHORS
ECHO WARNING! WARNING!
CHOICE /C YN /M "We're going to modify %DS_URI%. Continue?"
IF %ERRORLEVEL% == 2 GOTO ABORT
SET COMMON_OPTS=sync-authors -i -u %DS_URI% -e %DS_EMAIL% -p %DS_PASSWORD% -a
ECHO Synchronizing JISBD authors 
java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE%                      || GOTO FAIL
ECHO Synchronizing JCIS authors 
java -jar target/%JAR% %COMMON_OPTS% -f  %JCIS_EDITION_FILE%                      || GOTO FAIL
ECHO Synchronizing PROLE authors 
java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE%                      || GOTO FAIL
ECHO Curating all authors 
java -jar target/%JAR% curate-authors -u %DS_URI% -e %DS_EMAIL% -p %DS_PASSWORD%  || GOTO FAIL
GOTO END

:PUBLISH
ECHO WARNING! WARNING!
CHOICE /C YN /M "We're going to modify %DS_URI%. Continue?"
IF %ERRORLEVEL% == 2 GOTO ABORT
SET COMMON_OPTS=publish -u %DS_URI% -e %DS_EMAIL% -p %DS_PASSWORD% -a -c
ECHO Publishing JISBD proceedings 
java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE% || GOTO FAIL
ECHO Publishing JCIS proceedings
java -jar target/%JAR% %COMMON_OPTS% -f  %JCIS_EDITION_FILE% || GOTO FAIL
ECHO Publishing PROLE proceedings 
java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE% || GOTO FAIL
GOTO END

:VALIDATE
SET COMMON_OPTS=validate
ECHO Validating JISBD 
java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE% || GOTO FAIL
ECHO Validating JCIS 
java -jar target/%JAR% %COMMON_OPTS% -f %JCIS_EDITION_FILE%  || GOTO FAIL
ECHO Validating PROLE 
java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE% || GOTO FAIL
GOTO END

:DISCARD
SET COMMON_OPTS=discard-uuids -a -e -p -s -t
ECHO Discarding JISBD UUIDs 
java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE% || GOTO FAIL
ECHO Discarding JCIS UUIDs 
java -jar target/%JAR% %COMMON_OPTS% -f %JCIS_EDITION_FILE%  || GOTO FAIL
ECHO Discarding PROLE UUIDs 
java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE% || GOTO FAIL
GOTO END

:FAIL
ECHO An error occurred!
:ABORT
ECHO Aborting!
:END
