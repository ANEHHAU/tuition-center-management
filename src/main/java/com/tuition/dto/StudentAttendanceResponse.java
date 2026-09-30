package com.tuition.dto;

import com.tuition.entity.Attendance;
import com.tuition.entity.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StudentAttendanceResponse {
    private Long id;
    private Long sessionId;
    private LocalDate sessionDate;
    private String courseName;
    private String teacherName;
    private AttendanceStatus status;
    private String note;

    public static StudentAttendanceResponse fromEntity(Attendance attendance) {
        return StudentAttendanceResponse.builder()
                .id(attendance.getId())
                .sessionId(attendance.getSession().getId())
                .sessionDate(attendance.getSession().getDate())
                .courseName(attendance.getSession().getGroup().getCourse().getName())
                .teacherName(attendance.getSession().getGroup().getCourse().getTeacher().getFullName())
                .status(attendance.getStatus())
                .note(attendance.getNote())
                .build();
    }
}
