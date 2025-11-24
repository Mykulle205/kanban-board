package com.board.kanban.controller;

import com.board.kanban.KanbanService;
import com.board.kanban.dto.CreateColumnRequest;
import com.board.kanban.dto.MoveColumnRequest;
import com.board.kanban.dto.UpdateColumnRequest;
import com.board.kanban.model.KanbanColumn;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kanban/columns")
public class ColumnController {

    private final KanbanService kanbanService;

    @PostMapping("/board/{boardId}")
    public KanbanColumn addColumn(@PathVariable Long boardId,
                                  @RequestBody CreateColumnRequest request) {
        return kanbanService.addColumn(boardId, request.name(), request.wipLimit());
    }

    @GetMapping("/{columnId}")
    public KanbanColumn getColumn(@PathVariable Long columnId) {
        return kanbanService.getColumn(columnId);
    }

    @PutMapping("/{columnId}")
    public KanbanColumn updateColumn(@PathVariable Long columnId,
                                     @RequestBody UpdateColumnRequest request) {
        return kanbanService.updateColumn(columnId, request.name(), request.wipLimit());
    }

    @PostMapping("/{columnId}/move")
    public KanbanColumn moveColumn(@PathVariable Long columnId,
                                   @RequestBody MoveColumnRequest request) {
        // usually request.targetBoardId() == current board
        return kanbanService.moveColumn(request.boardId(), columnId, request.nextPosition());
    }

    @DeleteMapping("/{columnId}")
    public void deleteColumn(@PathVariable Long columnId) {
        kanbanService.deleteColumn(columnId);
    }

}
