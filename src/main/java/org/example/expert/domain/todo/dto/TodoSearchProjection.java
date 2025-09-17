package org.example.expert.domain.todo.dto;

import com.querydsl.core.annotations.QueryProjection;

public record TodoSearchProjection(
        String title,
        int managerCount,
        int commentCount) {

    @QueryProjection
    public TodoSearchProjection {
    }
}
