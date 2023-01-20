@echo off
start cmd.exe /c startTriplestore.bat
SLEEP 5
start cmd.exe /c startAJAN.bat

echo Local IP:
ipconfig | findstr "IPv4"
echo Public IP:
nslookup myip.opendns.com resolver1.opendns.com
pause
