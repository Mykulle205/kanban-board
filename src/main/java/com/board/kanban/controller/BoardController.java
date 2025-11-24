package com.board.kanban.controller;

import com.board.kanban.KanbanService;
import com.board.kanban.dto.CreateBoardRequest;
import com.board.kanban.dto.UpdateBoardRequest;
import com.board.kanban.model.Board;
import com.board.kanban.model.KanbanColumn;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kanban/board")
public class BoardController {

    private final KanbanService kanbanService;

    @PostMapping
    public Board createBoard(@RequestBody CreateBoardRequest request) {
        return kanbanService.createBoard(request.name(), request.description());
    }

    @GetMapping
    public List<Board> getAllBoards() {
        return kanbanService.getAllBoard();
    }

    @GetMapping("/{boardId}")
    public Board getBoard(@PathVariable Long boardId) {
        return kanbanService.getBoard(boardId);
    }

    @PutMapping("/{boardId}")
    public Board updateBoard(@PathVariable Long boardId,
                             @RequestBody UpdateBoardRequest request) {
        return kanbanService.updateBoard(
                boardId,
                request.name(),
                request.description());
    }

    @DeleteMapping("/{boardId}")
    public void deleteBoard(@PathVariable Long boardId) {
        kanbanService.deleteBoard(boardId);
    }

    @GetMapping("/{boardId}/columns")
    public List<KanbanColumn> getAllColumns(@PathVariable Long boardId) {
        return kanbanService.getColumnsForBoard(boardId);
    }


}

