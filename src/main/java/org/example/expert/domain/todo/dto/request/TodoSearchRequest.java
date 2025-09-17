package org.example.expert.domain.todo.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record TodoSearchRequest(
        String title,
        @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
        String nickname
) {
}
