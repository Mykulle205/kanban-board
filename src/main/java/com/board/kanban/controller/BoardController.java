package com.board.kanban.controller;

import com.board.kanban.KanbanService;
import com.board.kanban.dto.CreateBoardRequest;
import com.board.kanban.dto.UpdateBoardRequest;
import com.board.kanban.model.Board;
import com.board.kanban.model.KanbanColumn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kanban/board")
public class BoardController {

    private final KanbanService kanbanService;

    @PostMapping
    public ResponseEntity<Board> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        Board board = kanbanService.createBoard(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(board);
    }

    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards() {
        List<Board> boards = kanbanService.getAllBoard();
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<Board> getBoard(@PathVariable Long boardId) {
        Board board = kanbanService.getBoard(boardId);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/{boardId}/columns")
    public ResponseEntity<List<KanbanColumn>> getAllColumns(@PathVariable Long boardId) {
        List<KanbanColumn> columns = kanbanService.getColumnsForBoard(boardId);
        return ResponseEntity.ok(columns);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<Board> updateBoard(@PathVariable Long boardId,
                                             @Valid @RequestBody UpdateBoardRequest request) {
        Board updated = kanbanService.updateBoard(
                boardId,
                request.name(),
                request.description()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId) {
        kanbanService.deleteBoard(boardId);
        return ResponseEntity.noContent().build(); // 204
    }
}
