package com.board.kanban.controller;

import com.board.kanban.KanbanService;
import com.board.kanban.dto.CreateTaskRequest;
import com.board.kanban.dto.MoveTaskRequest;
import com.board.kanban.dto.UpdateTaskRequest;
import com.board.kanban.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/kanban/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final KanbanService kanbanService;

    @PostMapping("/column/{columnId}")
    public Task addTask(@PathVariable Long columnId,
                        @RequestBody CreateTaskRequest request) {
        return kanbanService.addTask(
                columnId,
                request.name(),
                request.description(),
                request.taskPriority(),
                request.assignee(),
                request.dueDate()
        );
    }

    @GetMapping("/{taskId}")
    public Task getTask(@PathVariable Long taskId) {
        return kanbanService.getTask(taskId);
    }

    @GetMapping("/column/{columnId}")
    public List<Task> getTasksForColumn(@PathVariable Long columnId) {
        return kanbanService.getTasksForColumn(columnId);
    }

    @PutMapping("/{taskId}")
    public Task updateTask(@PathVariable Long taskId,
                           @RequestBody UpdateTaskRequest request) {
        return kanbanService.updateTask(
                taskId,
                request.name(),
                request.description(),
                request.taskPriority(),
                request.assignee(),
                request.dueDate()
        );
    }

    @PostMapping("/{taskId}/move")
    public Task moveTask(@PathVariable Long taskId,
                         @RequestBody MoveTaskRequest request) {
        return kanbanService.moveTask(taskId, request.ColumnId(), request.nextPosition());
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable Long taskId) {
        kanbanService.deleteTask(taskId);
    }
}
