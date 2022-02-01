package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.metadata.CoreFields;
import com.asteroid.duck.pointy.indexer.metadata.MetaDataExtractor;
import com.asteroid.duck.pointy.indexer.metadata.OptionalField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShow;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;

/**
 * Creates the text based fields for a Lucene index document from the individual slides
 */
public class SlideShowIndexer implements IndexFieldProvider {

    public static final String PATH_FIELD = "pathContent";

    private final String checksum;
    private final SlideShow<?, ?> slideShow;
    private final Collection<Path> locations;

    public SlideShowIndexer(String checksum, SlideShow<?, ?> slideShow, Collection<Path> locations) {
        this.checksum = checksum;
        this.slideShow = slideShow;
        this.locations = locations;
    }

    public List<IndexableField> index(Config cfg) {
        // FIXME use config to limit fields
        LinkedList<IndexableField> tmp = new LinkedList<>();

        tmp.add(checksum());
        tmp.add(pathContent());

        if (cfg.getShowFields().contains(OptionalField.CONTENT))
            tmp.add(content());
        tmp.addAll(metadata(cfg));
        return Collections.unmodifiableList(tmp);
    }

    public StringField checksum() {
        return new StringField(CoreFields.CHECKSUM_FIELD.getFieldName(), checksum, Field.Store.YES);
    }

    public TextField content() {
        SlideShowExtractor<?,?> extractor = new SlideShowExtractor<>(slideShow);
        return new TextField(OptionalField.CONTENT.name(), extractor.getText(), Field.Store.NO);
    }

    public TextField pathContent() {
        String pathContent = locations.stream()
                .flatMap(path -> StreamSupport.stream(path.spliterator(), true))
                .map(Path::toString)
                .collect(joining(" "));
        return new TextField(PATH_FIELD, pathContent, Field.Store.NO);
    }

    public List<IndexableField> metadata(Config cfg) {
        Optional<MetaDataExtractor> extractor = MetaDataExtractor.create(slideShow);
        if (extractor.isPresent()) {
            return extractor.get().extract(cfg.getMetaDataFields());
        }
        return Collections.emptyList();
    }
}
