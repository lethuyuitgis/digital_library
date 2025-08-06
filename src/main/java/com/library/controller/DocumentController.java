package com.library.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.clustering.ClusteringService;
import com.library.entity.Document;
import com.library.repository.DocumentRepository;
import com.library.service.CategoryService;
import com.library.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final CategoryService categoryService;
    private final ClusteringService clusteringService;
    private final DocumentRepository documentRepository;

    @GetMapping
    public String listDocuments(Model model,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) Integer cluster,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) throws JsonProcessingException {

        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documentPage;

        if (categoryId != null) {
            documentPage = documentService.getDocumentsByCategory(categoryId, pageable);
        } else if (cluster != null) {
            documentPage = documentService.getDocumentsByCluster(cluster, pageable);
        } else {
            documentPage = documentService.getAllDocuments(pageable);
        }

        model.addAttribute("documents", documentPage.getContent());
        model.addAttribute("totalPages", documentPage.getTotalPages());
        model.addAttribute("totalItems", documentPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size); // <-- cần cho dropdown "10 / page"

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("cluster", cluster);
        Map<Integer, Long> clusterStats = documentService.getClusterStatistics();
        model.addAttribute("clusterStats", clusterStats);
        ObjectMapper objectMapper = new ObjectMapper();
        String clusterStatsJson = objectMapper.writeValueAsString(clusterStats);
        model.addAttribute("clusterStatsJson", clusterStatsJson);

        return "documents";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("document", new Document());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "document-form";
    }

    @PostMapping("/save")
    public String saveDocument(@ModelAttribute Document document) {
        documentService.saveDocument(document);
        return "redirect:/documents";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "document-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return "redirect:/documents";
    }

    @PostMapping("/cluster")
    public String performClustering(@RequestParam int numClusters) {
        clusteringService.performClustering(numClusters);
        return "redirect:/documents";
    }
    @GetMapping("/view/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        model.addAttribute("document", document);
        return "view-document";
    }

    @GetMapping("/{id}/similar")
    public String getSimilarDocuments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<Document> similarDocuments = documentService.getSimilarDocuments(id, size, page);

        model.addAttribute("similarDocuments", similarDocuments.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", similarDocuments.getTotalPages());
        model.addAttribute("totalItems", similarDocuments.getTotalElements()); // cần cho hiển thị tổng số bản ghi
        model.addAttribute("size", size); // để giữ dropdown chọn size
        model.addAttribute("documentId", id); // cần cho pagination link

        return "similar-documents";
    }

}
