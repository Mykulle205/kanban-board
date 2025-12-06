package com.board.kanban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateColumnRequest(
        @NotBlank String name,
        @PositiveOrZero Integer wipLimit
) {}
