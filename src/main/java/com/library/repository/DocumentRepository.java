package com.library.repository;

import com.library.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findAll(Pageable pageable);
    Page<Document> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Document> findByCluster(Integer cluster, Pageable pageable);
}