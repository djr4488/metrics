package com.djr4488.metrics.health.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "test_entities")
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(name = "test_column")
    private String testColumn;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTestColumn() {
        return testColumn;
    }

    public void setTestColumn(String testColumn) {
        this.testColumn = testColumn;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", testColumn='" + testColumn + '\'' +
                '}';
    }
}
