package com.board.kanban.dto;

public record MoveColumnRequest(
        Long boardId,
        int nextPosition
) {}
