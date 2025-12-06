package com.board.kanban.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoveColumnRequest(
        @NotNull Long boardId,
        @PositiveOrZero int nextPosition
) {}
