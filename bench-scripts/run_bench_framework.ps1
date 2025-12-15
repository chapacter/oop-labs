# bench-scripts\run_bench_framework.ps1
param(
  [int]$Iterations = 200
)

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$postman = Join-Path $root "postman"
$collection = Join-Path $postman "collection-lab5.json"
$env = Join-Path $postman "env-framework.postman_env.json"
$reportJson = Join-Path $postman "details_framework.json"
$detailsCsv = Join-Path $postman "details_framework.csv"
$aggCsv = Join-Path $postman "aggregates_framework.csv"

Write-Host "Running newman (iterations=$Iterations)..."
newman run $collection -e $env --iteration-count $Iterations --reporters cli,json --reporter-json-export $reportJson

Write-Host "Converting JSON -> CSV & aggregates..."
node (Join-Path $root "bench-scripts\newman-to-csv.js") --input $reportJson --out-details $detailsCsv --out-agg $aggCsv

Write-Host "Done. Output:"
Get-ChildItem $postman | Select-Object Name,Length
