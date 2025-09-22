package org.example.expert.domain.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class UserBulkInsertTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int TOTAL = 5_000_000;
    private static final int BATCH_SIZE = 10_000; // 배치 크기

    @Test
//    @Disabled("bulkInsert 테스트는 필요시에 로컬 DB에서 수동 실행하기")
    void JDBC로_500만_유저_데이터를_생성한다() throws Exception {

        long start = System.currentTimeMillis();

        String encodedPassword = passwordEncoder.encode("password123");

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            final String sql = "INSERT INTO users (created_at, email, modified_at, nickname, password, user_role) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 1; i <= TOTAL; i++) {
                    LocalDateTime now = LocalDateTime.now();
                    String email = "user" + i + "@example.com";
                    String nickname = randomNickname(i);
                    String role = "USER";

                    ps.setObject(1, now);
                    ps.setString(2, email);
                    ps.setObject(3, now);
                    ps.setString(4, nickname);
                    ps.setString(5, encodedPassword);
                    ps.setString(6, role);

                    ps.addBatch();

                    if (i % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        conn.commit();
                    }
                }

                // 남은 데이터 처리
                ps.executeBatch();
                conn.commit();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Bulk insert completed in ms: " + (end - start));
    }

    private static String randomNickname(int idx) {
        return "nick_" + idx + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}

