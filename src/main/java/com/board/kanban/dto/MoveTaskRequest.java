package com.board.kanban.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoveTaskRequest(
        @NotNull Long columnId,
        @PositiveOrZero int nextPosition
) {}
