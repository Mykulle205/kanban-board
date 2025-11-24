package com.board.kanban.dto;

import com.board.kanban.model.TaskPriority;
import java.time.LocalDate;

public record CreateTaskRequest(
        String name,
        String description,
        TaskPriority taskPriority,
        String assignee,
        LocalDate dueDate
) {}
