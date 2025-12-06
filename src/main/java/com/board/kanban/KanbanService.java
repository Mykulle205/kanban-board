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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KanbanService {

    private final BoardRepository boardRepository;
    private final KanbanColumnRepository kanbanColumnRepository;
    private final TaskRepository taskRepository;

    // ------------------ Boards ------------------

    public Board createBoard(String name, String description) {

        Board board = new Board();
        board.setName(name.trim());

        if (description != null && !description.isBlank()) {
            board.setDescription(description.trim());
        }

        Board saved = boardRepository.save(board);
        log.info("Created board id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    public Board createBoard(String name) {
        return createBoard(name, null);
    }

    public Board getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> {
                    log.warn("Board not found while fetching board id={}", boardId);
                    return new IllegalArgumentException("Board id=" + boardId + " not found");
                });
    }

    public List<Board> getAllBoard() {
        log.info("Fetching all boards");
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

        Board saved = boardRepository.save(board);
        log.info("Updated board id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    public void deleteBoard(Long boardId) {
        Board board = getBoard(boardId);
        boardRepository.delete(board);
        log.info("Deleted board id={}", boardId);
    }

    // ------------------ Kanban Columns ------------------

    public KanbanColumn addColumn(Long boardId, String name, Integer wipLimit) {
        Board board = getBoard(boardId);
        KanbanColumn column = new KanbanColumn();

        int position = board.getColumns().size();

        column.setName(name.trim());
        column.setWipLimit(wipLimit);
        column.setPosition(position);
        column.setBoard(board);
        board.addColumn(column);

        KanbanColumn saved = kanbanColumnRepository.save(column);
        log.info("Added column id={} to board id={} position={} wipLimit={}",
                saved.getId(), boardId, position, wipLimit);
        return saved;
    }

    public KanbanColumn getColumn(Long columnId) {
        return kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> {
                    log.warn("Column not found while fetching column id={}", columnId);
                    return new IllegalArgumentException("Column id=" + columnId + " not found");
                });
    }

    public List<KanbanColumn> getColumnsForBoard(Long boardId) {
        getBoard(boardId);
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

        KanbanColumn saved = kanbanColumnRepository.save(column);
        log.info("Updated column id={} name='{}' wipLimit={}",
                saved.getId(), saved.getName(), saved.getWipLimit());
        return saved;
    }

    /**
     * Move a column within a board to a new index (0-based).
     */
    public KanbanColumn moveColumn(Long boardId, Long columnId, int targetPosition) {
        Board board = getBoard(boardId);
        List<KanbanColumn> columns =
                kanbanColumnRepository.findByBoardIdOrderByPositionAsc(boardId);

        KanbanColumn column = columns.stream()
                .filter(c -> c.getId().equals(columnId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Column id={} not part of board id={}", columnId, boardId);
                    return new IllegalArgumentException("Column id=" + columnId + " not found in board id=" + boardId);
                });

        int originalPosition = column.getPosition();
        columns.remove(column);

        int index = Math.max(0, Math.min(targetPosition, columns.size()));
        columns.add(index, column);

        renumberColumns(columns);

        log.info("Moved column id={} in board id={} from position={} to position={}",
                columnId, board.getId(), originalPosition, index);
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

        log.info("Deleted column id={} from board id={}", columnId, board.getId());
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

        KanbanColumn column = getColumn(columnId);
        Integer wipLimit = column.getWipLimit();

        // current tasks in DB for that column
        List<Task> existing = taskRepository.findByColumnIdOrderByPositionAsc(columnId);

        // enforce WIP before mutating state
        if (wipLimit != null && existing.size() + 1 > wipLimit) {
            int attemptedSize = existing.size() + 1;
            log.warn("WIP limit {} would be exceeded for column {} while creating task (attemptedTasks={})",
                    wipLimit, columnId, attemptedSize);

            throw new IllegalStateException(
                    "WIP limit " + wipLimit + " would be exceeded for column " + columnId
            );
        }

        int position = existing.size();

        Task task = new Task();
        task.setName(name.trim());
        task.setDescription(description);
        task.setPriority(priority);
        task.setAssignee(assignee);
        task.setDueDate(dueDate);
        task.setPosition(position);

        column.addTask(task);

        Task saved = taskRepository.save(task);
        log.info("Created task id={} in column id={} position={}",
                saved.getId(), columnId, position);
        return saved;
    }

    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found while fetching task id={}", taskId);
                    return new IllegalArgumentException("Task id=" + taskId + " not found");
                });
    }

    public List<Task> getTasksForColumn(Long columnId) {
        getColumn(columnId);
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

        Task saved = taskRepository.save(task);
        log.info("Updated task id={}", saved.getId());
        return saved;
    }

    public Task moveTask(Long taskId, Long targetColumnId, int targetPosition) {
        Task task = getTask(taskId);

        KanbanColumn sourceColumn = task.getColumn();
        Long sourceColumnId = sourceColumn.getId();
        KanbanColumn targetColumn = getColumn(targetColumnId);

        // Same-column move
        if (sourceColumnId.equals(targetColumnId)) {
            List<Task> tasks =
                    taskRepository.findByColumnIdOrderByPositionAsc(sourceColumnId);

            tasks.removeIf(t -> t.getId().equals(taskId));

            int index = Math.max(0, Math.min(targetPosition, tasks.size()));
            tasks.add(index, task);

            renumberTasks(tasks);

            log.info("Reordered task id={} within column id={} to position={}",
                    taskId, sourceColumnId, index);
            return task;
        }

        // Cross-column move
        List<Task> targetTasks =
                taskRepository.findByColumnIdOrderByPositionAsc(targetColumnId);
        Integer wipLimit = targetColumn.getWipLimit();

        if (wipLimit != null && targetTasks.size() + 1 > wipLimit) {
            int attemptedSize = targetTasks.size() + 1;
            log.warn("WIP limit {} would be exceeded for column {} while moving task {} (attemptedTasks={})",
                    wipLimit, targetColumnId, taskId, attemptedSize);

            throw new IllegalStateException(
                    "WIP limit " + wipLimit + " would be exceeded for column " + targetColumnId
            );
        }

        // 1) detach from source and renumber source column
        sourceColumn.removeTask(task);
        List<Task> sourceTasks =
                taskRepository.findByColumnIdOrderByPositionAsc(sourceColumnId);
        renumberTasks(sourceTasks);

        int index = Math.max(0, Math.min(targetPosition, targetTasks.size()));
        targetTasks.add(index, task);
        task.setColumn(targetColumn);
        renumberTasks(targetTasks);

        log.info("Moved task id={} from column id={} to column id={} position={}",
                taskId, sourceColumnId, targetColumnId, index);
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

        log.info("Deleted task id={} from column id={}", taskId, column.getId());
    }

    private void renumberTasks(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPosition(i);
        }
    }
}
