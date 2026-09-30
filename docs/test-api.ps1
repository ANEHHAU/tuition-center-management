# Script test API tự động
# Chạy: powershell -ExecutionPolicy Bypass -File test-api.ps1

$ErrorActionPreference = "Continue"
$base = "http://localhost:8080"
$pass = 0
$fail = 0

function Test-Api {
    param([string]$Name, [string]$Method, [string]$Url, [string]$Body, [string]$Token, [string]$Expect)
    
    Write-Host ""
    Write-Host "=== TEST: $Name ===" -ForegroundColor Cyan
    
    try {
        $headers = @{}
        if ($Token) { $headers["Authorization"] = "Bearer $Token" }
        if ($Body) { $headers["Content-Type"] = "application/json" }
        
        $params = @{
            Uri = "$base$Url"
            Method = $Method
            Headers = $headers
            ErrorAction = "Stop"
        }
        if ($Body) { $params["Body"] = $Body }
        
        $response = Invoke-RestMethod @params
        $json = $response | ConvertTo-Json -Depth 3 -Compress
        
        if ($Expect -and $json -notmatch $Expect) {
            Write-Host "  FAIL - Response khong match '$Expect'" -ForegroundColor Red
            Write-Host "  Response: $($json.Substring(0, [Math]::Min(200, $json.Length)))"
            $script:fail++
        } else {
            Write-Host "  PASS" -ForegroundColor Green
            # In ra 200 ky tu dau
            $preview = if ($json.Length -gt 200) { $json.Substring(0, 200) + "..." } else { $json }
            Write-Host "  Response: $preview" -ForegroundColor DarkGray
            $script:pass++
        }
    } catch {
        $code = $_.Exception.Response.StatusCode.Value__
        if ($Expect -eq "error") {
            Write-Host "  PASS (expected error, got $code)" -ForegroundColor Green
            $script:pass++
        } else {
            Write-Host "  FAIL - HTTP Error: $code - $($_.Exception.Message)" -ForegroundColor Red
            $script:fail++
        }
    }
}

Write-Host "======================================" -ForegroundColor Yellow
Write-Host "  API TEST SUITE - Tuition Center" -ForegroundColor Yellow  
Write-Host "======================================" -ForegroundColor Yellow

# ========== 1. AUTH ==========
Write-Host "`n--- 1. AUTHENTICATION ---" -ForegroundColor Yellow

# Login Admin
$adminResp = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$ADMIN_TOKEN = $adminResp.token
if ($ADMIN_TOKEN) {
    Write-Host "`n=== TEST: Login Admin ===" -ForegroundColor Cyan
    Write-Host "  PASS - Token: $($ADMIN_TOKEN.Substring(0,30))..." -ForegroundColor Green
    $pass++
} else {
    Write-Host "`n=== TEST: Login Admin ===" -ForegroundColor Cyan
    Write-Host "  FAIL - No token" -ForegroundColor Red
    $fail++
}

# Login Teacher
$teacherResp = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"teacher1","password":"123456"}'
$TEACHER_TOKEN = $teacherResp.token
if ($TEACHER_TOKEN) {
    Write-Host "`n=== TEST: Login Teacher1 ===" -ForegroundColor Cyan
    Write-Host "  PASS - Token: $($TEACHER_TOKEN.Substring(0,30))..." -ForegroundColor Green
    $pass++
} else {
    Write-Host "`n=== TEST: Login Teacher1 ===" -ForegroundColor Cyan
    Write-Host "  FAIL - No token" -ForegroundColor Red
    $fail++
}

# Login Student
$studentResp = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"student1","password":"123456"}'
$STUDENT_TOKEN = $studentResp.token
if ($STUDENT_TOKEN) {
    Write-Host "`n=== TEST: Login Student1 ===" -ForegroundColor Cyan
    Write-Host "  PASS - Token: $($STUDENT_TOKEN.Substring(0,30))..." -ForegroundColor Green
    $pass++
} else {
    Write-Host "`n=== TEST: Login Student1 ===" -ForegroundColor Cyan
    Write-Host "  FAIL - No token" -ForegroundColor Red
    $fail++
}

# Login sai password
Test-Api -Name "Login sai password" -Method POST -Url "/api/auth/login" -Body '{"username":"admin","password":"wrongpass"}' -Expect "error"

# ========== 2. ADMIN - USERS ==========
Write-Host "`n--- 2. ADMIN - USERS ---" -ForegroundColor Yellow

Test-Api -Name "Get Users (all)" -Method GET -Url "/api/admin/users?page=0&size=20&sortBy=name&sortDir=asc" -Token $ADMIN_TOKEN -Expect "content"

Test-Api -Name "Search Users (keyword=an)" -Method GET -Url "/api/admin/users?keyword=an&page=0&size=10" -Token $ADMIN_TOKEN -Expect "content"

Test-Api -Name "Filter Users (role=TEACHER)" -Method GET -Url "/api/admin/users?role=TEACHER&page=0&size=10" -Token $ADMIN_TOKEN -Expect "content"

Test-Api -Name "Filter Users (role=STUDENT)" -Method GET -Url "/api/admin/users?role=STUDENT&page=0&size=10" -Token $ADMIN_TOKEN -Expect "content"

# ========== 3. ADMIN - COURSES ==========
Write-Host "`n--- 3. ADMIN - COURSES ---" -ForegroundColor Yellow

Test-Api -Name "Get Courses" -Method GET -Url "/api/admin/courses" -Token $ADMIN_TOKEN

Test-Api -Name "Get Courses (search)" -Method GET -Url "/api/admin/courses?keyword=Toan" -Token $ADMIN_TOKEN

# ========== 4. ADMIN - GROUPS ==========
Write-Host "`n--- 4. ADMIN - GROUPS ---" -ForegroundColor Yellow

Test-Api -Name "Get Groups" -Method GET -Url "/api/admin/groups" -Token $ADMIN_TOKEN
$groupsResp = Invoke-RestMethod -Uri "$base/api/admin/groups" -Method GET -Headers @{ "Authorization" = "Bearer $ADMIN_TOKEN" }
$firstGroupId = if ($groupsResp.Count -gt 0) { $groupsResp[0].id } else { 1 }

# ========== 5. ADMIN - SESSIONS ==========
Write-Host "`n--- 5. ADMIN - SESSIONS ---" -ForegroundColor Yellow

Test-Api -Name "Get Sessions (groupId=$firstGroupId)" -Method GET -Url "/api/admin/sessions?groupId=$firstGroupId" -Token $ADMIN_TOKEN

# ========== 6. ADMIN - INVOICES ==========
Write-Host "`n--- 6. ADMIN - INVOICES ---" -ForegroundColor Yellow

Test-Api -Name "Get Invoices Revenue Report" -Method GET -Url "/api/admin/invoices/reports/revenue?from=2026-09-01T00:00:00&to=2026-09-30T23:59:59" -Token $ADMIN_TOKEN

# ========== 7. ADMIN - AUDIT LOG ==========
Write-Host "`n--- 7. ADMIN - AUDIT LOG ---" -ForegroundColor Yellow

Test-Api -Name "Get Audit Logs" -Method GET -Url "/api/admin/audit-logs?page=0&size=5" -Token $ADMIN_TOKEN

# ========== 8. PROFILE APIs ==========
Write-Host "`n--- 8. PROFILE APIs ---" -ForegroundColor Yellow

Test-Api -Name "Teacher - Profile" -Method GET -Url "/api/profile" -Token $TEACHER_TOKEN
Test-Api -Name "Student - Profile" -Method GET -Url "/api/profile" -Token $STUDENT_TOKEN

# ========== 10. UNAUTHORIZED ==========
Write-Host "`n--- 10. UNAUTHORIZED ACCESS ---" -ForegroundColor Yellow

Test-Api -Name "No token -> Admin Users (should 401/403)" -Method GET -Url "/api/admin/users" -Expect "error"

Test-Api -Name "Student token -> Admin Users (should 403)" -Method GET -Url "/api/admin/users" -Token $STUDENT_TOKEN -Expect "error"

# ========== SUMMARY ==========
Write-Host ""
Write-Host "======================================" -ForegroundColor Yellow
Write-Host "  KET QUA: $pass PASS / $fail FAIL" -ForegroundColor $(if ($fail -eq 0) { "Green" } else { "Red" })
Write-Host "======================================" -ForegroundColor Yellow
