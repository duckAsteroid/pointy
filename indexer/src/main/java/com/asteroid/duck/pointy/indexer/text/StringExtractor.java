package com.asteroid.duck.pointy.indexer.text;

import com.asteroid.duck.pointy.indexer.Fields;
import com.asteroid.duck.pointy.indexer.PipelineStage;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.usermodel.Slide;

import java.util.function.Function;
import java.util.stream.Stream;

public class StringExtractor implements PipelineStage {
    private Function<Slide<?,?>, String> extractor;
    private final Fields field;

    public StringExtractor(Fields field) {
        this.field = field;
    }

    @Override
    public Stream<IndexableField> extract(Slide<?, ?> slide) {
        return null;
    }
}
