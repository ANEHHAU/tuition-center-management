package com.tuition.dto;

import com.tuition.entity.AttendanceStatus;
import com.tuition.entity.Session;
import com.tuition.entity.SessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class StudentSessionResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long courseId;
    private String courseName;
    private String teacherName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private SessionStatus status;
    private String note;
    // Student's own attendance for this session
    private AttendanceStatus myAttendanceStatus;
    private String myAttendanceNote;

    public static StudentSessionResponse fromEntity(Session session) {
        return StudentSessionResponse.builder()
                .id(session.getId())
                .groupId(session.getGroup().getId())
                .groupName(session.getGroup().getName())
                .courseId(session.getGroup().getCourse().getId())
                .courseName(session.getGroup().getCourse().getName())
                .teacherName(session.getGroup().getCourse().getTeacher().getFullName())
                .date(session.getDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .room(session.getRoom())
                .status(session.getStatus())
                .note(session.getNote())
                .build();
    }
}
