$ErrorActionPreference = 'Stop'

$moduleRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourceFile = Join-Path $moduleRoot 'src\main\java\com\ticketing\demo\TicketPlatformDemo.java'
$classesDir = Join-Path $moduleRoot 'target\classes'
$jarFile = Join-Path $moduleRoot 'target\ticket-mvp-demo.jar'

New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
javac --release 17 -encoding UTF-8 -d $classesDir $sourceFile
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

jar --create --file $jarFile --main-class com.ticketing.demo.TicketPlatformDemo -C $classesDir .
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

java -jar $jarFile
