<#
.SYNOPSIS
End-to-End Test Script cho Tuition Center Management

.DESCRIPTION
Script này chạy tuần tự các test scenarios bằng Invoke-RestMethod và Assert kết quả.
Trả về exit code 0 nếu tất cả đều PASS.
#>

$ErrorActionPreference = "Stop"
$BASE_URL = "http://localhost:8080/api"
$totalTests = 12
$passedTests = 0

function Print-Result ($step, $name, $success) {
    if ($success) {
        Write-Host "[$step/$totalTests] $name $(('.') * (40 - $name.Length)) PASS" -ForegroundColor Green
        $script:passedTests++
    } else {
        Write-Host "[$step/$totalTests] $name $(('.') * (40 - $name.Length)) FAIL" -ForegroundColor Red
        exit 1
    }
}

try {
    # Kiểm tra server
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -Method Get -ErrorAction Stop
    } catch {
        Write-Host "Server không phản hồi trên cổng 8080. Vui lòng chạy ứng dụng Spring Boot trước." -ForegroundColor Red
        exit 1
    }

    # [1/12] Admin login
    $adminLogin = Invoke-RestMethod -Uri "$BASE_URL/auth/login" -Method Post -Body (@{ username="admin"; password="password" } | ConvertTo-Json) -ContentType "application/json"
    $adminToken = $adminLogin.token
    Print-Result 1 "Admin login" ($null -ne $adminToken)

    # [2/12] Tạo teacher1
    $headersAdmin = @{ Authorization = "Bearer $adminToken"; "Content-Type" = "application/json" }
    $teacherData = @{ username="e2e_teacher"; password="password"; email="e2e_teacher@test.com"; fullName="E2E Teacher"; role="TEACHER"; phone="0123456789"; status="ACTIVE" }
    $newTeacher = Invoke-RestMethod -Uri "$BASE_URL/admin/users" -Method Post -Body ($teacherData | ConvertTo-Json) -Headers $headersAdmin
    Print-Result 2 "Tạo teacher1" ($null -ne $newTeacher.id)

    # [3/12] Teacher1 login
    $teacherLogin = Invoke-RestMethod -Uri "$BASE_URL/auth/login" -Method Post -Body (@{ username="e2e_teacher"; password="password" } | ConvertTo-Json) -ContentType "application/json"
    $teacherToken = $teacherLogin.token
    $headersTeacher = @{ Authorization = "Bearer $teacherToken"; "Content-Type" = "application/json" }
    Print-Result 3 "Teacher1 login" ($null -ne $teacherToken)

    # [4/12] Teacher1 tạo course
    $courseData = @{ name="Toán 9 E2E"; price=100000; status="ACTIVE" }
    $course = Invoke-RestMethod -Uri "$BASE_URL/teacher/courses" -Method Post -Body ($courseData | ConvertTo-Json) -Headers $headersTeacher
    Print-Result 4 "Teacher1 tạo course" ($course.price -eq 100000)

    # [5/12] Teacher1 tạo group
    $groupData = @{ name="Toán 9A E2E"; courseId=$course.id; startDate="2026-10-01"; endDate="2026-10-31"; status="ACTIVE" }
    $group = Invoke-RestMethod -Uri "$BASE_URL/teacher/groups" -Method Post -Body ($groupData | ConvertTo-Json) -Headers $headersTeacher
    Print-Result 5 "Teacher1 tạo group" ($group.name -eq "Toán 9A E2E")

    # [6/12] Tạo học sinh
    $studentData = @{ username="e2e_student"; password="password"; email="e2e_student@test.com"; fullName="E2E Student"; role="STUDENT"; phone="0987654321"; status="ACTIVE" }
    $student = Invoke-RestMethod -Uri "$BASE_URL/admin/users" -Method Post -Body ($studentData | ConvertTo-Json) -Headers $headersAdmin
    Print-Result 6 "Tạo student1" ($null -ne $student.id)

    # [7/12] Thêm HS vào group
    $enrollData = @{ studentId=$student.id; groupId=$group.id; status="ACTIVE" }
    $enroll = Invoke-RestMethod -Uri "$BASE_URL/teacher/groups/$($group.id)/enrollments" -Method Post -Body ($enrollData | ConvertTo-Json) -Headers $headersTeacher
    Print-Result 7 "Thêm HS vào group" ($enroll.studentId -eq $student.id)

    # [8/12] Tạo session
    $sessionData = @{ groupId=$group.id; date="2026-10-05"; status="SCHEDULED" }
    $session = Invoke-RestMethod -Uri "$BASE_URL/teacher/sessions" -Method Post -Body ($sessionData | ConvertTo-Json) -Headers $headersTeacher
    Print-Result 8 "Tạo session" ($null -ne $session.id)

    # [9/12] Điểm danh (PRESENT)
    $attendData = @( @{ studentId=$student.id; status="PRESENT"; note="Test" } )
    $attend = Invoke-RestMethod -Uri "$BASE_URL/teacher/sessions/$($session.id)/attendance" -Method Post -Body ($attendData | ConvertTo-Json) -Headers $headersTeacher
    Print-Result 9 "Điểm danh (PRESENT)" ($attend.Count -eq 1)

    # [10/12] Generate invoice
    $invoice = Invoke-RestMethod -Uri "$BASE_URL/teacher/invoices/generate?month=10&year=2026&studentId=$($student.id)" -Method Post -Headers $headersTeacher
    Print-Result 10 "Generate invoice" ($invoice.totalAmount -eq 100000)

    # [11/12] Student1 login và xem nợ
    $studentLogin = Invoke-RestMethod -Uri "$BASE_URL/auth/login" -Method Post -Body (@{ username="e2e_student"; password="password" } | ConvertTo-Json) -ContentType "application/json"
    $studentToken = $studentLogin.token
    $headersStudent = @{ Authorization = "Bearer $studentToken"; "Content-Type" = "application/json" }
    $debt = Invoke-RestMethod -Uri "$BASE_URL/student/invoices/current-debt" -Method Get -Headers $headersStudent
    Print-Result 11 "Student1 xem nợ" ($debt.debt -eq 100000)

    # [12/12] Phân quyền (Student gọi API Admin -> 403)
    try {
        $forbid = Invoke-RestMethod -Uri "$BASE_URL/admin/users" -Method Get -Headers $headersStudent -ErrorAction Stop
        Print-Result 12 "Phân quyền (Student gọi API Admin)" $false
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 403) {
            Print-Result 12 "Phân quyền (Student gọi API Admin)" $true
        } else {
            Print-Result 12 "Phân quyền (Student gọi API Admin)" $false
        }
    }

    Write-Host "`nTổng: $passedTests/$totalTests PASS" -ForegroundColor Green
    exit 0

} catch {
    Write-Host "Lỗi không xác định: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
