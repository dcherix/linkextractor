package eu.cherix.linkextractor;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @since 17.12.17
 */
@Component
public class LinkStorage {

    private static final Logger log = getLogger(LinkStorage.class);
    private final List<Map<URI, Long>> maps = new LinkedList<>();

    public void store(List<URI> links) {
        this.countOccurences.andThen(this.maps::add).apply(links);
    }

    public void get(Path path) {
        Map<URI, Long> aggregated = this.maps.stream().map(Map::entrySet).flatMap(Collection::stream)
                                             .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("link\toccurrence");
            for (Map.Entry<URI, Long> e : aggregated.entrySet()) {
                writer.newLine();
                writer.write(e.getKey().toString() + "\t" + e.getValue());
            }
        }
        catch (IOException e) {
            log.error("Error on writing result file", e);
            return;
        }
        log.info("Result writed in file: {}", path);
    }

    private final Function<List<URI>, Map<URI, Long>> countOccurences = links -> {
        Map<URI, Long> map = new HashMap<>();
        links.forEach(uri -> {
            if (!map.containsKey(uri)) {
                map.put(uri, 1L);
            }
            else {
                map.put(uri, map.get(uri) + 1);
            }
        });
        return map;
    };
}
