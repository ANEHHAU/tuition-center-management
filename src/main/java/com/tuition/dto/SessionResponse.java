package com.tuition.dto;

import com.tuition.entity.Session;
import com.tuition.entity.SessionStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionResponse(
        Long id,
        Long groupId,
        String groupName,
        Long courseId,
        String courseName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        SessionStatus status,
        String note,
        long attendanceCount
) {
    public static SessionResponse fromEntity(Session session, long attendanceCount) {
        return new SessionResponse(
                session.getId(),
                session.getGroup().getId(),
                session.getGroup().getName(),
                session.getGroup().getCourse().getId(),
                session.getGroup().getCourse().getName(),
                session.getDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getRoom(),
                session.getStatus(),
                session.getNote(),
                attendanceCount
        );
    }
}
