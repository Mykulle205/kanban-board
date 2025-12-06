package com.board.kanban.controller;

import com.board.kanban.KanbanService;
import com.board.kanban.dto.CreateColumnRequest;
import com.board.kanban.dto.MoveColumnRequest;
import com.board.kanban.dto.UpdateColumnRequest;
import com.board.kanban.model.KanbanColumn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kanban/columns")
public class ColumnController {

    private final KanbanService kanbanService;

    @PostMapping("/board/{boardId}")
    public ResponseEntity<KanbanColumn> addColumn(@PathVariable Long boardId,
                                                  @Valid @RequestBody CreateColumnRequest request) {

        KanbanColumn column = kanbanService.addColumn(
                boardId,
                request.name(),
                request.wipLimit()
        );

        // 201 for resource creation
        return ResponseEntity.status(HttpStatus.CREATED).body(column);
    }

    @GetMapping("/{columnId}")
    public ResponseEntity<KanbanColumn> getColumn(@PathVariable Long columnId) {
        KanbanColumn column = kanbanService.getColumn(columnId);
        return ResponseEntity.ok(column);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<KanbanColumn> updateColumn(@PathVariable Long columnId,
                                                     @Valid @RequestBody UpdateColumnRequest request) {

        KanbanColumn updated = kanbanService.updateColumn(
                columnId,
                request.name(),
                request.wipLimit()
        );

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{columnId}/move")
    public ResponseEntity<KanbanColumn> moveColumn(@PathVariable Long columnId,
                                                   @Valid @RequestBody MoveColumnRequest request) {
        // usually request.boardId() == current board
        KanbanColumn moved = kanbanService.moveColumn(
                request.boardId(),
                columnId,
                request.nextPosition()
        );

        return ResponseEntity.ok(moved);
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(@PathVariable Long columnId) {
        kanbanService.deleteColumn(columnId);
        return ResponseEntity.noContent().build(); // 204
    }
}
