package com.library;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.library.clustering.ClusteringService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DigitalLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalLibraryApplication.class, args);
    }
    @Bean
    public CommandLineRunner init(ClusteringService clusteringService) {
        return args -> clusteringService.performClustering(3); // Số cụm mặc định là 3, bạn có thể thay đổi
    }
}