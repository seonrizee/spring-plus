package org.example.expert.domain.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserSearchPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
//    @Disabled("성능 측정은 테스트는 필요시에 로컬 DB에서 수동 실행하기")
    @WithMockUser
    void 닉네임으로_유저_검색_성능_측적_테스트() throws Exception {

        // 500만 건 데이터 중 존재하는 닉네임으로 검색
        String existingNickname = "nick_2600000_02f209d5";

        int iterations = 10; // 정확한 측정을 위해 10번 반복하여 평균값 사용
        List<Long> durations = new ArrayList<>();

        // 워밍업
        mockMvc.perform(get("/users").param("nickname", existingNickname));

        // 실제 측정
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();

            mockMvc.perform(get("/users")
                            .param("nickname", existingNickname))
                    .andExpect(status().isOk());

            long end = System.currentTimeMillis();
            durations.add(end - start);
            System.out.printf("[측정 결과] Iteration %d: %d ms%n", i + 1, (end - start));
        }

        double average = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.printf("[측정 결과] 닉네임 검색 평균 응답 시간 (10회): %.2f ms%n", average);
    }
}