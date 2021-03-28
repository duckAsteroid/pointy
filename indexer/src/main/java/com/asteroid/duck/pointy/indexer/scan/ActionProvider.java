package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.indexer.Fields;
import org.apache.commons.math3.util.Pair;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * For every path, and every content ID - what action does the indexing process need to perform.
 */
public class ActionProvider {
    /*
    private final IndexSearcher reader;
    private final Map<String, Map<Path, ActionType>> results;

    public ActionProvider(IndexReader reader) {
        this.reader = new IndexSearcher(reader);
        this.results = new HashMap<>(initialActions());
    }

    public Map<String, Map<Path, ActionType>> initialActions() {
        // read the documents from the index reader
        return StreamSupport.stream(new IterableIndex(reader.getIndexReader()).spliterator(), false)
                // array of FQ paths for each checksum
                .map(doc -> new Pair<String, String[]>(doc.get(Fields.FILE_CHECKSUM.name()), doc.getValues(Fields.FQ_PATH.name())))
                // stream of deletes as default action
                .collect(Collectors.toMap(Pair::getKey, pair -> {
                    Map<Path, ActionType> result = new HashMap<>();
                    Arrays.stream(pair.getValue()).map(Paths::get).forEach(path -> result.put(path, ActionType.DELETE));
                    return result;
                }));
    }

    public Map<Path, Action> actionTypeMap(String checksum) {
        return results.getOrDefault(checksum, Collections.emptyMap());
    }

    public Optional<Action> actionType(String checksum, Path p) {
        return Optional.ofNullable(actionTypeMap(checksum).get(p));
    }

    public Set<Path> paths(String checksum) {
        return actionTypeMap(checksum).keySet();
    }

    public Stream<Pair<Path, ActionType>> pathActions(String checksum) {
        return actionTypeMap(checksum).entrySet().stream()
                .map(entry -> new Pair<Path, ActionType>(entry.getKey(), entry.getValue()));
    }

    public ActionType addAction(String checksum, Path path, ActionType actionType) {
        Map<Path, ActionType> pathActions;
        if (!results.containsKey(checksum)) {
            pathActions = new HashMap<>();
            results.put(checksum, pathActions);
        } else {
            pathActions = results.get(checksum);
        }
        return pathActions.put(path, actionType);
    }

*/
}
