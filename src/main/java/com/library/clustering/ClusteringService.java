package com.library.clustering;

import com.library.entity.Document;
import com.library.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import smile.clustering.KMeans;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClusteringService {

    private final DocumentRepository documentRepository;
    private static final int MAX_DOCUMENTS = 300;
    private static final int MAX_VOCAB_SIZE = 1000;
    private static final int MIN_TERM_FREQ = 3;

    public void performClustering(int numClusters) {
        List<Document> documents = documentRepository.findAll()
                .stream()
                .limit(MAX_DOCUMENTS)
                .collect(Collectors.toList());

        if (documents.isEmpty()) return;

        String[] texts = documents.stream()
                .map(Document::getContent)
                .toArray(String[]::new);
        double[][] vectors = createTFIDFVectors(texts);

        KMeans kmeans = KMeans.fit(vectors, numClusters);
        int[] labels = kmeans.y;

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            doc.setCluster(labels[i]);
            documentRepository.save(doc);
        }
    }

    private double[][] createTFIDFVectors(String[] texts) {
        Map<String, Integer> globalFreq = new HashMap<>();
        List<List<String>> tokenizedDocs = new ArrayList<>();

        for (String text : texts) {
            List<String> tokens = Arrays.stream(text.toLowerCase().split("\\s+"))
                    .map(token -> token.replaceAll("[^a-zA-Z]", "")) // bỏ dấu câu
                    .filter(token -> token.length() > 1) // bỏ token ngắn
                    .toList();

            for (String token : tokens) {
                globalFreq.put(token, globalFreq.getOrDefault(token, 0) + 1);
            }

            tokenizedDocs.add(tokens);
        }

        // Chọn từ phổ biến nhất
        List<String> vocabList = globalFreq.entrySet().stream()
                .filter(e -> e.getValue() >= MIN_TERM_FREQ)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(MAX_VOCAB_SIZE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Map<String, Integer> vocabIndex = new HashMap<>();
        for (int i = 0; i < vocabList.size(); i++) {
            vocabIndex.put(vocabList.get(i), i);
        }

        int numDocs = tokenizedDocs.size();
        int vocabSize = vocabList.size();
        double[][] tfMatrix = new double[numDocs][vocabSize];
        double[] docFreq = new double[vocabSize];

        for (int i = 0; i < numDocs; i++) {
            List<String> doc = tokenizedDocs.get(i);
            Map<String, Long> termCounts = doc.stream()
                    .filter(vocabIndex::containsKey)
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            Set<String> seenTerms = new HashSet<>();

            for (Map.Entry<String, Long> entry : termCounts.entrySet()) {
                int index = vocabIndex.get(entry.getKey());
                tfMatrix[i][index] = entry.getValue() / (double) doc.size();

                if (seenTerms.add(entry.getKey())) {
                    docFreq[index]++;
                }
            }
        }

        double[] idf = new double[vocabSize];
        for (int i = 0; i < vocabSize; i++) {
            idf[i] = Math.log((double) numDocs / (1 + docFreq[i]));
        }

        double[][] tfidf = new double[numDocs][vocabSize];
        for (int i = 0; i < numDocs; i++) {
            for (int j = 0; j < vocabSize; j++) {
                tfidf[i][j] = tfMatrix[i][j] * idf[j];
            }
        }

        return tfidf;
    }

}
