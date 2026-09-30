package com.tuition.repository;

import com.tuition.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByGroupIdAndDateBetween(Long groupId, LocalDate from, LocalDate to);

    List<Session> findByGroupIdOrderByDateAsc(Long groupId);

    boolean existsByGroupIdAndDateAndStartTime(Long groupId, LocalDate date, LocalTime startTime);

    /**
     * Tìm sessions của group trong khoảng thời gian
     */
    @Query("SELECT s FROM Session s WHERE s.group.id = :groupId " +
           "AND s.date >= :start AND s.date <= :end " +
           "ORDER BY s.date ASC, s.startTime ASC")
    List<Session> findSessionsInRange(@Param("groupId") Long groupId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}
