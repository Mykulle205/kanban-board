package com.board.kanban.repository;

import com.board.kanban.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByColumnIdOrderByPositionAsc(Long columnId);
}