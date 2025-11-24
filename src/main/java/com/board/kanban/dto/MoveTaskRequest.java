package com.board.kanban.dto;

public record MoveTaskRequest(
        Long ColumnId,
        int nextPosition
) {}
