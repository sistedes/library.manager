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
REM As a consequence, we use this script as a simple but fair enough solution to
REM run all the required commands in an automated and easy way.
REM

setlocal

CALL execute.env.bat

CALL mvn package

IF "%1"=="init" GOTO INIT
IF "%1"=="sync-authors" GOTO SYNC_AUTHORS
IF "%1"=="publish" GOTO PUBLISH
IF "%1"=="register-handles" GOTO REGISTER_HANDLES
IF "%1"=="dump-handles-batch" GOTO DUMP_HANDLES_BATCH
IF "%1"=="validate" GOTO VALIDATE

ECHO ERROR: Must provide exactly one of the following subcommands:
ECHO init, sync-authors, publish, validate

GOTO END


:INIT
SET COMMON_OPTS=init -y %YEAR% -P %HANDLE_PREFIX%
CALL java -jar target/%JAR% %COMMON_OPTS% -a %JISBD% -i "%INPUT_DIR%/%JISBD%" -o "%OUTPUT_DIR%/%JISBD%" -p %JISBD_PDF_PATTERN% -x %JISBD_XLSX%
CALL java -jar target/%JAR% %COMMON_OPTS% -a %JCIS%  -i "%INPUT_DIR%/%JCIS%"  -o "%OUTPUT_DIR%/%JCIS%"  -p  %JCIS_PDF_PATTERN% -x  %JCIS_XLSX%
CALL java -jar target/%JAR% %COMMON_OPTS% -a %PROLE% -i "%INPUT_DIR%/%PROLE%" -o "%OUTPUT_DIR%/%PROLE%" -p %PROLE_PDF_PATTERN% -x %PROLE_XLSX%
GOTO END

:SYNC_AUTHORS
SET COMMON_OPTS=sync-authors -i -u %DS_URI% -e %DS_EMAIL% -p %DS_PASSWORD%
CALL java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE%
CALL java -jar target/%JAR% %COMMON_OPTS% -f  %JCIS_EDITION_FILE%
CALL java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE%
GOTO END

:PUBLISH
SET COMMON_OPTS=publish -u %DS_URI% -e %DS_EMAIL% -p %DS_PASSWORD%
CALL java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE%
CALL java -jar target/%JAR% %COMMON_OPTS% -f  %JCIS_EDITION_FILE%
CALL java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE%
GOTO END

:VALIDATE
SET COMMON_OPTS=validate
CALL java -jar target/%JAR% %COMMON_OPTS% -f %JISBD_EDITION_FILE%
CALL java -jar target/%JAR% %COMMON_OPTS% -f %JCIS_EDITION_FILE%
CALL java -jar target/%JAR% %COMMON_OPTS% -f %PROLE_EDITION_FILE%
GOTO END

:END