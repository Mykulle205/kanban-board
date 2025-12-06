package com.board.kanban.dto;

import com.board.kanban.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank String name,
        String description,
        TaskPriority taskPriority,
        String assignee,
        LocalDate dueDate
) {}
