package com.library.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String fileName;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private Integer cluster;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


}