package com.tuition.dto;

import com.tuition.entity.SessionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionRequest(
        @NotNull(message = "Mã nhóm không được để trống")
        Long groupId,

        @NotNull(message = "Ngày học không được để trống")
        LocalDate date,

        @NotNull(message = "Giờ bắt đầu không được để trống")
        LocalTime startTime,

        @NotNull(message = "Giờ kết thúc không được để trống")
        LocalTime endTime,

        String room,

        SessionStatus status,

        String note
) {
}
