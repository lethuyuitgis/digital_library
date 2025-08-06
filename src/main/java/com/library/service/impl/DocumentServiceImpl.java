package com.library.service.impl;

import com.library.entity.Document;
import com.library.repository.DocumentRepository;
import com.library.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    @Override
    public Page<Document> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    @Override
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    @Override
    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    @Override
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    @Override
    public Page<Document> getDocumentsByCategory(Long categoryId, Pageable pageable) {
        return documentRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Document> getDocumentsByCluster(Integer cluster, Pageable pageable) {
        return documentRepository.findByCluster(cluster, pageable);
    }

    public Map<Integer, Long> getClusterStatistics() {
        return documentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getCluster() != null ? doc.getCluster() : -1,
                        Collectors.counting()
                ));
    }

    @Override
    public Page<Document> getSimilarDocuments(Long documentId, int topN, int page) {
        Document currentDoc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        Integer currentCluster = currentDoc.getCluster();

        if (currentCluster == null) {
            return Page.empty(); // Trả về trang rỗng nếu không có cụm
        }

        Pageable pageable = PageRequest.of(page, topN);
        Page<Document> documents = documentRepository.findByCluster(currentCluster, pageable);

        // Lọc tài liệu hiện tại và xây dựng lại Page
        List<Document> filteredContent = documents.getContent()
                .stream()
                .filter(doc -> !doc.getId().equals(documentId)) // Loại bỏ tài liệu hiện tại
                .collect(Collectors.toList());

        return new PageImpl<>(filteredContent, pageable, documents.getTotalElements());
    }
}
