$baseUrl    = "http://localhost:8080"
$adminUser  = "seed_user"
$adminPass  = "pass"
$newUser    = "user_no_admin_$([int](Get-Random -Maximum 10000))"
$newPass    = "pass"
$functionsPageSize = 1

Write-Host "Starting authentication/authorization checks..." -ForegroundColor Cyan

function Get-BasicHeader($user, $pass) {
    $b = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$user`:$pass"))
    return @{ Authorization = "Basic $b" }
}

function Get-RespStatus($resp) {
    if ($null -ne $resp) {
        try { return [int]$resp.StatusCode } catch {}
        try { return [int]$resp.StatusCode.Value__ } catch {}
    }
    return 0
}

function Try-Invoke($method, $uri, $body=$null, $headers=$null) {
    try {
        if ($body -ne $null) {
            $resp = Invoke-WebRequest -Uri $uri -Method $method -Headers $headers -Body $body -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
        } else {
            $resp = Invoke-WebRequest -Uri $uri -Method $method -Headers $headers -UseBasicParsing -ErrorAction Stop
        }
        $raw = $resp.Content
        $parsed = $null
        try { $parsed = $raw | ConvertFrom-Json -ErrorAction Stop } catch { $parsed = $null }
        $status = Get-RespStatus $resp
        if ($status -eq 0) {
            $status = 200
        }
        return @{ status = $status; content = $parsed; raw = $raw }
    } catch {
        $status = 0
        $raw = $null
        if ($_.Exception -and $_.Exception.Response) {
            try { $status = $_.Exception.Response.StatusCode.Value__ } catch {}
            try {
                $sr = $_.Exception.Response.GetResponseStream()
                if ($sr) {
                    $srReader = New-Object System.IO.StreamReader($sr)
                    $raw = $srReader.ReadToEnd()
                }
            } catch {}
        } else {
            $raw = $_.ToString()
        }
        if (-not $status) { $status = 0 }
        return @{ status = $status; content = $null; raw = $raw }
    }
}
$uriClear = "$baseUrl/api/admin/clear"
Write-Host "`n[1] POST $uriClear" -ForegroundColor Yellow
$resClear = Try-Invoke -method Post -uri $uriClear
Write-Host ((" => status: {0}" -f $resClear.status)) -ForegroundColor Gray
if ($resClear.status -ne 200) {
    Write-Host "WARNING: admin/clear returned non-200. Check SecurityConfig or server logs." -ForegroundColor Red
    if ($resClear.raw) { Write-Host ("Raw:`n{0}" -f $resClear.raw) -ForegroundColor DarkGray }
}

$uriPop = "$baseUrl/api/admin/populate?functions=50`&pointsPerFunction=2`&batchSize=50"
Write-Host "`n[2] POST $uriPop" -ForegroundColor Yellow
$resPopulate = Try-Invoke -method Post -uri $uriPop
Write-Host ((" => status: {0}" -f $resPopulate.status)) -ForegroundColor Gray
if ($resPopulate.status -ne 200 -and $resPopulate.status -ne 202) {
    Write-Host "WARNING: admin/populate did not return 200/202. Raw response (if any):" -ForegroundColor Red
    if ($resPopulate.raw) { Write-Host ("Raw:`n{0}" -f $resPopulate.raw) -ForegroundColor DarkGray }
}

$uriUsers = "$baseUrl/api/users?page=0`&size=20"
Write-Host "`n[3] GET $uriUsers (without auth) — expecting 401" -ForegroundColor Yellow
$resUnauth = Try-Invoke -method Get -uri $uriUsers
Write-Host ((" => status: {0}" -f $resUnauth.status)) -ForegroundColor Gray
if ($resUnauth.status -eq 401) {
    Write-Host "OK: unauthenticated request correctly returned 401." -ForegroundColor Green
} else {
    Write-Host ("WARN: unauthenticated request returned {0} (expected 401). Server may not enforce auth on this endpoint." -f $resUnauth.status) -ForegroundColor Magenta
    if ($resUnauth.raw) { Write-Host ("Raw body:`n{0}" -f $resUnauth.raw) -ForegroundColor DarkGray }
}

$uriSeed = "$baseUrl/api/users?page=0`&size=20`&name=seed_user"
$adminHeader = Get-BasicHeader $adminUser $adminPass
Write-Host "`n[4] GET $uriSeed (with seed_user creds) — expecting 200 and roles include ROLE_ADMIN" -ForegroundColor Yellow
$resSeed = Try-Invoke -method Get -uri $uriSeed -headers $adminHeader
Write-Host ((" => status: {0}" -f $resSeed.status)) -ForegroundColor Gray
if ($resSeed.status -eq 200 -and $resSeed.content) {
    try {
        $content = $resSeed.content.content
        if ($content -and $content.Count -ge 1) {
            $first = $content[0]
            Write-Host ("Found user: id={0}, name={1}, accessLvl={2}" -f $first.id, $first.name, $first.accessLvl) -ForegroundColor Gray
            if ($first.roles) {
                Write-Host (("Roles: {0}" -f ($first.roles -join ', '))) -ForegroundColor Green
                if ($first.roles -contains "ROLE_ADMIN") {
                    Write-Host "OK: seed_user has ROLE_ADMIN." -ForegroundColor Green
                } else {
                    Write-Host "WARN: seed_user missing ROLE_ADMIN in returned roles." -ForegroundColor Magenta
                }
            } else {
                Write-Host "WARN: response did not include roles array." -ForegroundColor Magenta
            }
            $seedUserId = $first.id
        } else {
            Write-Host "WARN: response content array empty." -ForegroundColor Magenta
        }
    } catch {
        Write-Host "ERROR parsing JSON response: $_" -ForegroundColor Red
        if ($resSeed.raw) { Write-Host ("Raw body:`n{0}" -f $resSeed.raw) -ForegroundColor DarkGray }
    }
} else {
    Write-Host (("ERROR: expected 200 from protected GET with seed_user but got {0}." -f $resSeed.status)) -ForegroundColor Red
    if ($resSeed.raw) { Write-Host ("Raw response:`n{0}" -f $resSeed.raw) -ForegroundColor DarkGray }
}

Write-Host "`n[5] Create new regular user (POST /api/users) using seed_user creds" -ForegroundColor Yellow
$createBodyObj = @{ name = $newUser; password = $newPass; accessLvl = 1 }
$createBody = $createBodyObj | ConvertTo-Json -Depth 5
$uriCreateUser = "$baseUrl/api/users"
$resCreate = Try-Invoke -method Post -uri $uriCreateUser -body $createBody -headers $adminHeader
Write-Host ((" => status: {0}" -f $resCreate.status)) -ForegroundColor Gray
$newUserId = $null
if ($resCreate.status -in 200,201) {
    try {
        if ($resCreate.content -ne $null) {
            if ($resCreate.content.id) { $newUserId = $resCreate.content.id }
            elseif ($resCreate.content | Get-Member -Name id -ErrorAction SilentlyContinue) { $newUserId = $resCreate.content.id }
            else { $newUserId = $null }
        }
    } catch {}
    Write-Host (("Created user {0} with id = {1}" -f $newUser, $newUserId)) -ForegroundColor Green
} else {
    Write-Host (("Failed to create user; status {0}. Raw:`n{1}" -f $resCreate.status, $resCreate.raw)) -ForegroundColor Red
}

Write-Host "`n[6] Trying to detect role enforcement by deleting a function as the created non-admin user" -ForegroundColor Yellow
$funcId = $null
if ($seedUserId) {
    $uriFuncs = "$baseUrl/api/functions?page=0`&size=$functionsPageSize`&name=`&userId=$seedUserId"
    $rfunc = Try-Invoke -method Get -uri $uriFuncs -headers $adminHeader
    if ($rfunc.status -eq 200 -and $rfunc.content) {
        try {
            $arr = $rfunc.content.content
            if ($arr -and $arr.Count -ge 1) {
                $funcId = $arr[0].id
                Write-Host (("Picked function id {0} belonging to seed_user" -f $funcId)) -ForegroundColor Gray
            }
        } catch {}
    }
}

if (-not $funcId) {
    Write-Host "Could not pick function id from seed_user; trying generic /api/functions/1" -ForegroundColor Magenta
    $funcId = 1
}

$nonAdminHeader = Get-BasicHeader $newUser $newPass
$uriDeleteFunc = "$baseUrl/api/functions/$funcId"
Write-Host (("DELETE {0} as {1} (expect 403 if role checks enforced)" -f $uriDeleteFunc, $newUser)) -ForegroundColor Yellow
$resDelete = Try-Invoke -method Delete -uri $uriDeleteFunc -headers $nonAdminHeader
Write-Host ((" => status: {0}" -f $resDelete.status)) -ForegroundColor Gray
if ($resDelete.status -eq 403) {
    Write-Host "OK: non-admin user correctly received 403 Forbidden for admin-only delete." -ForegroundColor Green
} elseif ($resDelete.status -in 200,204,404) {
    Write-Host (("NOTICE: delete returned {0} (not 403). Might mean endpoint not role-protected or resource absent." -f $resDelete.status)) -ForegroundColor Magenta
    if ($resDelete.raw) { Write-Host ("Raw:`n{0}" -f $resDelete.raw) -ForegroundColor DarkGray }
} else {
    Write-Host (("Unexpected response status {0}. Check server logs." -f $resDelete.status)) -ForegroundColor Red
    if ($resDelete.raw) { Write-Host ("Raw:`n{0}" -f $resDelete.raw) -ForegroundColor DarkGray }
}

Write-Host "`n=== SUMMARY ===" -ForegroundColor Cyan
Write-Host (("admin/populate status: {0}" -f $resPopulate.status)) -ForegroundColor Gray
Write-Host (("unauthenticated GET /api/users -> status (expected 401): {0}" -f $resUnauth.status)) -ForegroundColor Gray
Write-Host (("authenticated seed_user GET /api/users?name=seed_user -> status: {0}" -f $resSeed.status)) -ForegroundColor Gray

Write-Host "`nInterpretation hints:"
if ($resUnauth.status -eq 401 -and $resSeed.status -eq 200) {
    Write-Host " • Auth enforced and seed_user authenticates OK." -ForegroundColor Green
} else {
    Write-Host " • If unauthenticated != 401 or seed_user != 200, investigate SecurityConfig and credentials." -ForegroundColor Magenta
}

Write-Host "`nIf delete-as-non-admin returned 403 => role protections enforced." -ForegroundColor Green
Write-Host "`nIf not => check controller annotations (e.g. @PreAuthorize('hasRole(\"ADMIN\")')) and SecurityConfig requestMatchers." -ForegroundColor Yellow

Write-Host "`nTo get more detail: enable DEBUG logs: add to application.properties:" -ForegroundColor Gray
Write-Host "  logging.level.org.springframework.security=DEBUG" -ForegroundColor Gray

Write-Host "`nDone." -ForegroundColor Cyan
