package com.library.config;


import com.library.entity.Category;
import com.library.entity.Document;
import com.library.repository.CategoryRepository;
import com.library.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DocumentRepository documentRepository;

    private final CategoryRepository categoryRepository;

    private static final String DATA_PATH = "/Users/lethitranthuy/Desktop/digital_library/src/main/resources/data/20news-bydate-train";

    @Override
    public void run(String... args) throws Exception {
        if (documentRepository.count() > 0) return;

        Map<String, Category> categories = createCategories();
        loadDocuments(categories);

        System.out.println("Data initialization completed.");
    }

    private Map<String, Category> createCategories() {
        Map<String, Category> categories = new HashMap<>();
        File trainDir = new File(DATA_PATH);
        if (!trainDir.exists() || !trainDir.isDirectory()) {
            throw new IllegalStateException("Data directory not found: " + DATA_PATH);
        }

        for (File categoryDir : trainDir.listFiles(File::isDirectory)) {
            Category category = new Category();
            category.setName(categoryDir.getName());
            categories.put(categoryDir.getName(), categoryRepository.save(category));
        }
        return categories;
    }

    private void loadDocuments(Map<String, Category> categories) throws Exception {
        File trainDir = new File(DATA_PATH);
        int maxDocuments = 500; // Giới hạn 500 tài liệu
        int count = 0;
        for (File categoryDir : trainDir.listFiles(File::isDirectory)) {
            Category category = categories.get(categoryDir.getName());
            for (File file : categoryDir.listFiles(File::isFile)) {
                if (count >= maxDocuments) break;
                try {
                    Document doc = new Document();
                    String content = Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
                    String title = extractSubject(content);
                    if (title.isEmpty()) {
                        title = file.getName();
                    }
                    doc.setTitle(title);
                    doc.setFileName(file.getName());
                    doc.setContent(content);
                    doc.setCategory(category);
                    documentRepository.save(doc);
                    count++;
                } catch (Exception e) {
                    System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
                }
            }
            if (count >= maxDocuments) break;
        }
    }

    private String extractSubject(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith("Subject:")) {
                return line.substring("Subject:".length()).trim();
            }
        }
        return "";
    }
}