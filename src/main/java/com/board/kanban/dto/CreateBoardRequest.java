package com.board.kanban.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBoardRequest(
        @NotBlank String name,
        String description
) {}
