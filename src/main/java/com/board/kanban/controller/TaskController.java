package com.board.kanban.controller;

import com.board.kanban.KanbanService;
import com.board.kanban.dto.CreateTaskRequest;
import com.board.kanban.dto.MoveTaskRequest;
import com.board.kanban.dto.UpdateTaskRequest;
import com.board.kanban.model.Task;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kanban/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final KanbanService kanbanService;

    @PostMapping("/column/{columnId}")
    public ResponseEntity<Task> addTask(@PathVariable Long columnId,
                                        @Valid @RequestBody CreateTaskRequest request) {

        Task task = kanbanService.addTask(
                columnId,
                request.name(),
                request.description(),
                request.taskPriority(),
                request.assignee(),
                request.dueDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable Long taskId) {
        Task task = kanbanService.getTask(taskId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/column/{columnId}")
    public ResponseEntity<List<Task>> getTasksForColumn(@PathVariable Long columnId) {
        List<Task> tasks = kanbanService.getTasksForColumn(columnId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId,
                                           @Valid @RequestBody UpdateTaskRequest request) {

        Task updated = kanbanService.updateTask(
                taskId,
                request.name(),
                request.description(),
                request.taskPriority(),
                request.assignee(),
                request.dueDate()
        );

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{taskId}/move")
    public ResponseEntity<Task> moveTask(@PathVariable Long taskId,
                                         @Valid @RequestBody MoveTaskRequest request) {

        Task moved = kanbanService.moveTask(
                taskId,
                request.columnId(),
                request.nextPosition()
        );

        return ResponseEntity.ok(moved);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        kanbanService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
