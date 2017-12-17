package eu.cherix.linkextractor;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @since 11.12.17
 */
public class LinkExtractor implements Runnable {

    private static final Logger log = getLogger(LinkExtractor.class);
    public static final int TIMEOUT = 10000;
    private final URI linkToExtract;
    private final LinkStorage linkStorage;
    private final int depth;
    private final Orchestrator orchestrator;

    public LinkExtractor(URI linkToExtract, LinkStorage linkStorage, int depth, Orchestrator orchestrator) {
        this.linkToExtract = linkToExtract;
        this.linkStorage = linkStorage;
        this.depth = depth;
        this.orchestrator = orchestrator;
    }

    @Override
    public void run() {
        log.trace("Extracting links from {}", this.linkToExtract);
        List<URI> uris = Collections.emptyList();
        try {
            Connection.Response response = Jsoup.connect(this.linkToExtract.toString()).timeout(0).followRedirects(true).execute();
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Document doc   = response.parse();
                Elements links = doc.select("a[href]");
                uris = links.stream().map(e -> e.attr("href")).map(URI::create).map(this.linkToExtract::resolve).map(URI::normalize)
                            .collect(toList());
            }
            else {
                log.error("Calling {} returned {}", this.linkToExtract, response.statusCode());
            }
        }
        catch (IOException e) {
            log.error("Error during link extraction from page {}", this.linkToExtract, e);
            return;
        }
        log.trace("Succesfully extracted links from {}", this.linkToExtract);
        this.linkStorage.store(uris);
        this.orchestrator.add(uris, this.depth);
    }
}
