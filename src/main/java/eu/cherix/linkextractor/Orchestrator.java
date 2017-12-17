package eu.cherix.linkextractor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @since 17.12.17
 */
@Component
public class Orchestrator {

    public static final String BASE_URI = "https://en.wikipedia.org/wiki/Europe";
    @Autowired
    private ExecutorService executorService;
    private final int maxDepth = 1;
    @Autowired
    private LinkStorage linkStorage;
    private Path outputPath;

    public void run(Path outputPath) throws ExecutionException, InterruptedException {
        this.outputPath = outputPath;
        Future<?> future = this.executorService.submit(new LinkExtractor(URI.create(BASE_URI), this.linkStorage, 0, this));
        future.get();
        this.executorService.shutdown();
        while (!this.executorService.isTerminated()) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        this.linkStorage.get(this.outputPath);
    }

    public void add(List<URI> links, int depth) {
        if (depth < this.maxDepth) {
            new HashSet<>(links).forEach(link -> this.executorService.submit(new LinkExtractor(link, this.linkStorage, depth + 1, this)));
        }
    }

}
