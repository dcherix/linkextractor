package eu.cherix.linkextractor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

@SpringBootApplication
public class LinkextractorApplication implements CommandLineRunner {

    @Autowired
    private Orchestrator orchestrator;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LinkextractorApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Override
    public void run(String... strings) throws Exception {
        this.orchestrator.run(Paths.get("output.tsv"));
        exit(0);
    }
}
