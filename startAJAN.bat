@echo off
setlocal enabledelayedexpansion

set ENV_FILE=.env

if not exist %ENV_FILE% (
    echo Error: .env file not found!
    exit /b 1
)

for /f "usebackq tokens=1,2 delims==" %%a in (%ENV_FILE%) do (
    set "VAR_NAME=%%a"
    set "VAR_VALUE=%%b"
    
    :: Skip lines that are comments or empty
    if not "!VAR_NAME!"=="" (
        if not "!VAR_NAME:~0,1!"=="#" (
            set "!VAR_NAME!=!VAR_VALUE!"
        )
    )
)

java -Dtriplestore.initialData.agentFolderPath=use-case/agents -Dtriplestore.initialData.domainFolderPath=use-case/domains -Dtriplestore.initialData.serviceFolderPath=use-case/services -Dtriplestore.initialData.behaviorsFolderPath=use-case/behaviors -Dtriplestore.initialData.nodeDefinitionsFolderPath=use-case/editor/nodeDefinitions -Dtriplestore.initialData.editorDataFolderPath=use-case/editor/editorData -Dpf4j.mode=development -Dserver.port=8080 -DloadTTLFiles=true -Dpf4j.pluginsDir=plugins -Dtriplestore.url=http://localhost:8090/rdf4j -jar executionservice-0.1.jar
endlocal
pause
