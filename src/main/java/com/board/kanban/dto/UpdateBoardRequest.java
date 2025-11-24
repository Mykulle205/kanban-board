package com.board.kanban.dto;

public record UpdateBoardRequest(
        String name,
        String description
) {}
