package com.board.kanban.dto;

public record UpdateColumnRequest(
        String name,
        Integer wipLimit
) {}
