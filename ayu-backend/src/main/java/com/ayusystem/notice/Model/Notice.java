package com.ayusystem.notice.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String author; // Should link to User entity in a full implementation

    @Column(nullable = false)
    private LocalDateTime postedAt = LocalDateTime.now();

    public Notice() {
    }

    public Notice(String author, String content, Long id, LocalDateTime postedAt, String title) {
        this.author = author;
        this.content = content;
        this.id = id;
        this.postedAt = postedAt;
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
