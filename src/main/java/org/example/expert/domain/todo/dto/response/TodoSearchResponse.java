package org.example.expert.domain.todo.dto.response;

public record TodoSearchResponse(
        String title,
        int managerCount,
        int commentCount) {

    public TodoSearchResponse(String title, int managerCount, int commentCount) {
        this.title = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
    }
}
