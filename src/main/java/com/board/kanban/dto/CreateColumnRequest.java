package com.board.kanban.dto;

public record CreateColumnRequest(
        String name,
        Integer wipLimit
) {}
