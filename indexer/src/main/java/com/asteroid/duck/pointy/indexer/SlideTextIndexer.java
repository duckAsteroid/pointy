package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.Config;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.Slide;

import java.util.LinkedList;
import java.util.List;

import static com.asteroid.duck.pointy.indexer.OptionalField.*;

public class SlideTextIndexer implements PipelineStage {
    private final String checksum;
    private final Slide<?,?> slide;

    public SlideTextIndexer(String checksum, Slide<?, ?> slide) {
        this.checksum = checksum;
        this.slide = slide;
    }

    @Override
    public List<IndexableField> index(Config cfg) {

        LinkedList<IndexableField> tmp = new LinkedList<>();

        if(cfg.getSlideFields().contains(SLIDE_NO))
            tmp.add(new StoredField(SLIDE_NO.name(), slide.getSlideNumber()));

        // extract slide title
        if(cfg.getSlideFields().contains(TITLE)) {
            String title = slide.getTitle();
            if (title != null && !title.isEmpty()) {
                tmp.add(new StringField(TITLE.name(), title, Field.Store.YES));
            }
        }

        // extract slide content text
        if(cfg.getSlideFields().contains(CONTENT)) {
            SlideShowExtractor extractor = new SlideShowExtractor(slide.getSlideShow());
            String content = extractor.getText(slide);
            tmp.add(new TextField(CONTENT.name(), content, Field.Store.YES));
        }

        return tmp;
    }


}
