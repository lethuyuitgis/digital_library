package com.library.clustering;

import com.library.entity.Document;
import com.library.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import weka.clusterers.SimpleKMeans;
import weka.core.*;
import weka.core.stopwords.Rainbow;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClusteringService {

    private final DocumentRepository documentRepository;

    public void performClustering(int numClusters) throws Exception {
        // B1: Lọc tài liệu hợp lệ
        List<Document> documents = documentRepository.findAll().stream()
                .filter(doc -> doc != null && doc.getContent() != null && !doc.getContent().trim().isEmpty())
                .limit(1000)
                .collect(Collectors.toList());

        if (documents.isEmpty()) return;

        // B2: Chuyển dữ liệu sang Weka Instances
        Instances data = convertToWekaInstances(documents);
        if (data == null || data.numInstances() == 0) return;

        // B3: Cấu hình vector hóa văn bản
        StringToWordVector filter = new StringToWordVector();
        filter.setLowerCaseTokens(true);
        filter.setStopwordsHandler(new Rainbow()); // Dùng danh sách stopword phổ biến
        filter.setTFTransform(true);
        filter.setIDFTransform(true);
        filter.setWordsToKeep(1000);
        filter.setOutputWordCounts(true);
        filter.setInputFormat(data);


        Instances filteredData = Filter.useFilter(data, filter);

        // B4: KMeans clustering
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(numClusters);
        kmeans.buildClusterer(filteredData);

        // B5: Gán cluster cho từng tài liệu
        int[] labels = new int[filteredData.numInstances()];
        for (int i = 0; i < filteredData.numInstances(); i++) {
            labels[i] = kmeans.clusterInstance(filteredData.instance(i));
        }

        for (int i = 0; i < documents.size(); i++) {
            documents.get(i).setCluster(labels[i]);
        }

        documentRepository.saveAll(documents);

        // B6: Log số lượng mỗi cluster (để fix chart nếu cần)
        for (int c = 0; c < numClusters; c++) {
            int clusterIndex = c;
            long count = documents.stream().filter(doc -> doc.getCluster() == clusterIndex).count();
            System.out.println("Cluster " + c + ": " + count + " documents");
        }

        // B7: Tính silhouette score (tùy chọn)
        double silhouetteScore = calculateSilhouetteScore(filteredData, labels, kmeans, numClusters);
        System.out.println("Silhouette Score: " + silhouetteScore);
    }

    private Instances convertToWekaInstances(List<Document> documents) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("content", (List<String>) null)); // text attribute
        Instances data = new Instances("Documents", attributes, documents.size());

        for (Document doc : documents) {
            DenseInstance instance = new DenseInstance(1);
            instance.setValue(attributes.get(0), doc.getContent());
            data.add(instance);
        }

        return data;
    }

    private double calculateSilhouetteScore(Instances data, int[] labels, SimpleKMeans kmeans, int numClusters) {
        int n = data.numInstances();
        double[] a = new double[n];
        double[] b = new double[n];

        for (int i = 0; i < n; i++) {
            int clusterI = labels[i];
            double sumA = 0.0;
            int countA = 0;

            for (int j = 0; j < n; j++) {
                if (i != j && labels[j] == clusterI) {
                    double dist = kmeans.getDistanceFunction().distance(data.instance(i), data.instance(j));
                    sumA += dist;
                    countA++;
                }
            }
            a[i] = countA > 0 ? sumA / countA : 0.0;

            double minB = Double.MAX_VALUE;
            for (int c = 0; c < numClusters; c++) {
                if (c != clusterI) {
                    double sumB = 0.0;
                    int countB = 0;
                    for (int j = 0; j < n; j++) {
                        if (labels[j] == c) {
                            double dist = kmeans.getDistanceFunction().distance(data.instance(i), data.instance(j));
                            sumB += dist;
                            countB++;
                        }
                    }
                    if (countB > 0) {
                        double avgB = sumB / countB;
                        minB = Math.min(minB, avgB);
                    }
                }
            }
            b[i] = minB != Double.MAX_VALUE ? minB : 0.0;
        }

        double totalSilhouette = 0.0;
        for (int i = 0; i < n; i++) {
            double s = (b[i] - a[i]) / Math.max(a[i], b[i]);
            totalSilhouette += s;
        }
        return totalSilhouette / n;
    }
}
