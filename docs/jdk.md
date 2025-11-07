dir "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin\java.exe"


$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"; $env:Path = "$env:JAVA_HOME\bin;$env:Path"; Write-Host "Using Java from: $env:JAVA_HOME"; & "$env:JAVA_HOME\bin\java" -version; .\gradlew.bat --stop; .\gradlew.bat assembleDebug --info