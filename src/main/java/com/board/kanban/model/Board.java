package com.board.kanban.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards")
@Getter
@Setter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanbanColumn> columns = new ArrayList<>();

    private String description;
    private Instant createdAt;
    private Instant updatedAt;


    public void addColumn(KanbanColumn column) {
        columns.add(column);
        column.setBoard(this);
    }

    public void removeColumn(KanbanColumn column) {
        columns.remove(column);
        column.setBoard(null);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }






}
