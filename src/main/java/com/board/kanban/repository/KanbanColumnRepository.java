package com.board.kanban.repository;

import com.board.kanban.model.KanbanColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {
    List<KanbanColumn> findByBoardIdOrderByPositionAsc(Long boardId);
}
