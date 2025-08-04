package com.library.service.impl;

import com.library.entity.Document;
import com.library.repository.DocumentRepository;
import com.library.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
}
