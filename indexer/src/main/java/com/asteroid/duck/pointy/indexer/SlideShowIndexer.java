package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.metadata.MetaDataExtractor;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShow;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates the fields for a Lucene index document from the slideshow
 */
public class SlideShowIndexer implements PipelineStage {

    private static final String PATH_FIELD = "path";

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
        tmp.addAll(locations());

        if (cfg.getShowFields().contains(OptionalField.CONTENT))
            tmp.add(content());
        tmp.addAll(metadata(cfg));
        return Collections.unmodifiableList(tmp);
    }

    public StringField checksum() {
        return new StringField(CHECKSUM_FIELD, checksum, Field.Store.YES);
    }

    public TextField content() {
        SlideShowExtractor<?,?> extractor = new SlideShowExtractor<>(slideShow);
        return new TextField(OptionalField.CONTENT.name(), extractor.getText(), Field.Store.NO);
    }

    public Collection<StringField> locations() {
        return locations.stream()
                .map(Path::toString)
                .map(path -> new StringField(PATH_FIELD, path, Field.Store.YES))
                .collect(Collectors.toList());
    }

    public List<IndexableField> metadata(Config cfg) {
        Optional<MetaDataExtractor> extractor = MetaDataExtractor.create(slideShow);
        if (extractor.isPresent()) {
            return extractor.get().extract(cfg.getMetaDataFields());
        }
        return Collections.emptyList();
    }
}