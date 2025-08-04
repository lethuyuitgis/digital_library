package com.library.service;

import com.library.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface DocumentService {
    Page<Document> getAllDocuments(Pageable pageable);

    Document getDocumentById(Long id);

    Document saveDocument(Document document);

    void deleteDocument(Long id);

    Page<Document> getDocumentsByCategory(Long categoryId, Pageable pageable);

    Page<Document> getDocumentsByCluster(Integer cluster, Pageable pageable);

    Map<Integer, Long> getClusterStatistics();
}
