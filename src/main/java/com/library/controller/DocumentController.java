package com.library.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.clustering.ClusteringService;
import com.library.entity.Document;
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
    private Map<String, Long> convertClusterStatsToChartFormat(Map<Integer, Long> stats) {
        return stats.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getKey() == -1 ? "Not Clustered" : "Cluster " + e.getKey(),
                        java.util.Map.Entry::getValue,
                        (a, b) -> b,
                        java.util.LinkedHashMap::new
                ));
    }

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
}
