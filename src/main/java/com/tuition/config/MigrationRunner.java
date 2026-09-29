package com.tuition.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE study_groups DROP COLUMN course_name");
            log.info("Dropped legacy column course_name from study_groups");
        } catch (Exception e) {
            log.info("Column course_name already dropped or does not exist");
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN created_by_id");
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN created_by_id BIGINT");
            log.info("Reset created_by_id column in users");
        } catch (Exception e) {
            log.info("Failed to reset created_by_id column");
        }
    }
}
