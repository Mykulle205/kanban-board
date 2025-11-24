package com.board.kanban.dto;

public record CreateBoardRequest(
        String name,
        String description
) {}
