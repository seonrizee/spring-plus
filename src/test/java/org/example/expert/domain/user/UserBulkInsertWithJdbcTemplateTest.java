package org.example.expert.domain.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class UserBulkInsertWithJdbcTemplateTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int TOTAL = 5_000_000;
    private static final int BATCH_SIZE = 10_000; // 배치 크기

    @Test
//    @Disabled("bulkInsert 테스트는 필요시에 로컬 DB에서 수동 실행하기")
    @DisplayName("JdbcTemplate Batch로 500만 사용자 생성")
    void JDBC로_500만_유저_데이터를_생성한다() {

        long start = System.currentTimeMillis();

        String encodedPassword = passwordEncoder.encode("password123");

        final String sql = "INSERT INTO users (created_at, email, modified_at, nickname, password, user_role) VALUES (?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 1; i <= TOTAL; i++) {
            LocalDateTime now = LocalDateTime.now();
            String email = "user" + i + "@example.com";
            String nickname = randomNickname(i);
            String role = "USER";

            batchArgs.add(new Object[]{now, email, now, nickname, encodedPassword, role});

            if (i % BATCH_SIZE == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear(); // 리스트 초기화
            }
        }

        // 남은 데이터 처리
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        long end = System.currentTimeMillis();
        System.out.println("Bulk insert with JdbcTemplate completed in ms: " + (end - start));
    }

    private static String randomNickname(int idx) {
        return "nick_" + idx + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}