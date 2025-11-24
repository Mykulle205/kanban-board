package com.board.kanban;

import com.board.kanban.model.Board;
import com.board.kanban.model.KanbanColumn;
import com.board.kanban.model.Task;
import com.board.kanban.model.TaskPriority;
import com.board.kanban.repository.BoardRepository;
import com.board.kanban.repository.KanbanColumnRepository;
import com.board.kanban.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class KanbanService {

    private final BoardRepository boardRepository;
    private final KanbanColumnRepository kanbanColumnRepository;
    private final TaskRepository taskRepository;

    // ------------------Boards------------------

    /**
     * Create a board. Description is optional (can be null/blank).
     */
    public Board createBoard(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Board name must not be empty");
        }

        Board board = new Board();
        board.setName(name.trim());

        if (description != null && !description.isBlank()) {
            board.setDescription(description.trim());
        }

        return boardRepository.save(board);

    }

    public Board createBoard(String name) {
        return createBoard(name, null);
    }

    public Board getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(()-> new IllegalArgumentException("Board not found" + boardId));
    }

    public List<Board> getAllBoard() {
        return boardRepository.findAll();
    }

    public Board updateBoard(Long boardId, String name, String description) {
        Board board = getBoard(boardId);

        if (name != null && !name.isBlank()) {
            board.setName(name.trim());
        }
        if (description != null) {
            board.setDescription(description.isBlank() ? null : description.trim());
        }

        return boardRepository.save(board);
    }

    public void deleteBoard(Long boardId) {
        Board board = getBoard(boardId);
        boardRepository.delete(board);
    }

    // ------------------Kanban Columns------------------

    public KanbanColumn addColumn(Long boardId, String name, Integer wipLimit){
        Board board = getBoard(boardId);
        KanbanColumn column = new KanbanColumn();

        int position = board.getColumns().size();

        column.setName(name);
        column.setWipLimit(wipLimit);
        column.setPosition(position);
        column.setBoard(board);
        board.addColumn(column);

        return kanbanColumnRepository.save(column);
    }

    public KanbanColumn getColumn(Long columnId) {
        return kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new IllegalArgumentException("Column not found: " + columnId));
    }

    public List<KanbanColumn> getColumnsForBoard(Long boardId) {
        return kanbanColumnRepository.findByBoardIdOrderByPositionAsc(boardId);
    }

    public KanbanColumn updateColumn(Long columnId, String name, Integer wipLimit) {
        KanbanColumn column = getColumn(columnId);

        if (name != null && !name.isBlank()) {
            column.setName(name.trim());
        }
        if (wipLimit != null) {
            column.setWipLimit(wipLimit);
        }

        return kanbanColumnRepository.save(column);
    }

    /**
     * Move a column within a board to a new index (0-based).
     */
    public KanbanColumn moveColumn(Long boardId, Long columnId, int targetPosition) {
        List<KanbanColumn> columns =
                kanbanColumnRepository.findByBoardIdOrderByPositionAsc(boardId);

        KanbanColumn column = columns.stream()
                .filter(c -> c.getId().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Column not in board: " + columnId));

        columns.remove(column);

        int index = Math.max(0, Math.min(targetPosition, columns.size()));
        columns.add(index, column);

        renumberColumns(columns);

        return column;
    }

    public void deleteColumn(Long columnId) {
        KanbanColumn column = getColumn(columnId);
        Board board = column.getBoard();

        board.removeColumn(column);
        kanbanColumnRepository.delete(column);

        // renumber remaining columns
        List<KanbanColumn> remaining =
                kanbanColumnRepository.findByBoardIdOrderByPositionAsc(board.getId());
        renumberColumns(remaining);
    }

    private void renumberColumns(List<KanbanColumn> columns) {
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setPosition(i);
        }
    }

    // ------------------ Tasks ------------------

    public Task addTask(Long columnId,
                        String name,
                        String description,
                        TaskPriority priority,
                        String assignee,
                        LocalDate dueDate) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Task title must not be empty");
        }

        KanbanColumn column = getColumn(columnId);

        List<Task> existing = taskRepository.findByColumnIdOrderByPositionAsc(columnId);
        int position = existing.size();

        Task task = new Task();
        task.setName(name.trim());
        task.setDescription(description);
        task.setPriority(priority);
        task.setAssignee(assignee);
        task.setDueDate(dueDate);
        task.setPosition(position);

        column.addTask(task);

        return taskRepository.save(task);
    }

    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    public List<Task> getTasksForColumn(Long columnId) {
        return taskRepository.findByColumnIdOrderByPositionAsc(columnId);
    }

    public Task updateTask(Long taskId,
                           String name,
                           String description,
                           TaskPriority priority,
                           String assignee,
                           LocalDate dueDate) {

        Task task = getTask(taskId);

        if (name != null && !name.isBlank()) {
            task.setName(name.trim());
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (assignee != null) {
            task.setAssignee(assignee);
        }
        if (dueDate != null) {
            task.setDueDate(dueDate);
        }

        return taskRepository.save(task);
    }

    /**
     * Move a task to another column (or within the same column) at a target index.
     */
    public Task moveTask(Long taskId, Long targetColumnId, int targetPosition) {
        Task task = getTask(taskId);

        KanbanColumn sourceColumn = task.getColumn();
        KanbanColumn targetColumn = getColumn(targetColumnId);

        sourceColumn.removeTask(task);

        List<Task> sourceTasks =
                taskRepository.findByColumnIdOrderByPositionAsc(sourceColumn.getId());
        renumberTasks(sourceTasks);

        List<Task> targetTasks =
                taskRepository.findByColumnIdOrderByPositionAsc(targetColumn.getId());

        int index = Math.max(0, Math.min(targetPosition, targetTasks.size()));
        targetTasks.add(index, task);
        renumberTasks(targetTasks);

        task.setColumn(targetColumn);

        return task;
    }

    public void deleteTask(Long taskId) {
        Task task = getTask(taskId);
        KanbanColumn column = task.getColumn();

        column.removeTask(task);

        taskRepository.delete(task);

        // renumber remaining tasks in that column
        List<Task> remaining =
                taskRepository.findByColumnIdOrderByPositionAsc(column.getId());
        renumberTasks(remaining);
    }

    private void renumberTasks(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPosition(i);
        }
    }
}
