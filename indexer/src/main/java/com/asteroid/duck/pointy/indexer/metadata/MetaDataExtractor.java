package com.asteroid.duck.pointy.indexer.metadata;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.POIDocument;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.sl.usermodel.SlideShow;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts core meta data from the file types
 * FIXME Should use Apache Tika for this!? https://tika.apache.org/1.26/parser.html
 */
public abstract class MetaDataExtractor {

    public static Optional<MetaDataExtractor> create(SlideShow<?,?> slideShow) {
        Object document = slideShow.getPersistDocument();
        if (document instanceof POIDocument) {
            SummaryInformation summaryInformation = ((POIDocument) document).getSummaryInformation();
            return Optional.of(new OldFormat(summaryInformation));
        }
        else if (document instanceof POIXMLDocument) {
            POIXMLProperties pOIXMLProperties;
            POIXMLProperties.CoreProperties coreProperties = ((POIXMLDocument) document).getProperties().getCoreProperties();
            return Optional.of(new NewFormat(coreProperties));
        }
        else {
            return Optional.empty();
        }
    }

    public List<IndexableField> extract(Set<MetaDataField> fields) {
        return fields.stream().map(this::extract).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    public Optional<IndexableField> extract(MetaDataField field) {
        switch (field) {
            case AUTHOR:
                return field(field.name(), getAuthor(), false, true);
            case TITLE:
                return field(field.name(), getTitle(), true, true);
            case KEYWORDS:
                return field(field.name(), getKeywords(), true, true);
            case COMMENTS:
                return field(field.name(), getComments(), true, true);
            case CREATED_DATE:
                return dateField(field.name(), getCreated());
            case LAST_SAVED_DATE:
                return dateField(field.name(), getLastSaved());
            case LAST_AUTHOR:
                return field(field.name(), getLastAuthor(), false, true);
        }
        return Optional.empty();
    }

    private static Optional<IndexableField> field(String fieldName, Optional<String> value, boolean text, boolean store)
    {
        IndexableField result = null;
        if (value.isPresent()) {
           Field.Store s = store ? Field.Store.YES : Field.Store.NO;
           if (text) {
               result = new TextField(fieldName, value.get(), s);
           }
           else {
               result = new StringField(fieldName, value.get(), s);
           }
        }
        return Optional.ofNullable(result);
    }

    private static Optional<IndexableField> dateField(String fieldName, Optional<Date> value)
    {
        LongPoint result = null;
        if (value.isPresent()) {
            result = new LongPoint(fieldName, value.get().getTime());
        }
        return Optional.ofNullable(result);
    }

    public abstract Optional<String> getAuthor();
    public abstract Optional<String> getLastAuthor();
    public abstract Optional<String> getTitle();
    public abstract Optional<String> getComments();
    public abstract Optional<String> getKeywords();
    public abstract Optional<Date> getCreated();
    public abstract Optional<Date> getLastSaved();

    private static class OldFormat extends MetaDataExtractor {
        private final SummaryInformation summaryInformation;

        private OldFormat(SummaryInformation summaryInformation) {
            this.summaryInformation = summaryInformation;
        }

        @Override
        public Optional<String> getAuthor() {
            return Optional.ofNullable(summaryInformation.getAuthor());
        }

        @Override
        public Optional<String> getLastAuthor() {
            return Optional.ofNullable(summaryInformation.getLastAuthor());
        }

        @Override
        public Optional<String> getTitle() {
            return Optional.ofNullable(summaryInformation.getTitle());
        }

        @Override
        public Optional<String> getComments() {
            return Optional.ofNullable(summaryInformation.getComments());
        }

        @Override
        public Optional<String> getKeywords() {
            return Optional.ofNullable(summaryInformation.getKeywords());
        }

        @Override
        public Optional<Date> getCreated() {
            return Optional.ofNullable(summaryInformation.getCreateDateTime());
        }

        @Override
        public Optional<Date> getLastSaved() {
            return Optional.ofNullable(summaryInformation.getLastSaveDateTime());
        }
    }

    private static class NewFormat extends MetaDataExtractor {
        private final POIXMLProperties.CoreProperties coreProperties;

        private NewFormat(POIXMLProperties.CoreProperties coreProperties) {
            this.coreProperties = coreProperties;
        }

        @Override
        public Optional<String> getAuthor() {
            return Optional.ofNullable(coreProperties.getCreator());
        }

        @Override
        public Optional<String> getLastAuthor() {
            return Optional.ofNullable(coreProperties.getLastModifiedByUser());
        }

        @Override
        public Optional<String> getTitle() {
            return Optional.ofNullable(coreProperties.getTitle());
        }

        @Override
        public Optional<String> getComments() {
            return Optional.ofNullable(coreProperties.getDescription());
        }

        @Override
        public Optional<String> getKeywords() {
            return Optional.ofNullable(coreProperties.getKeywords());
        }

        @Override
        public Optional<Date> getCreated() {
            return Optional.ofNullable(coreProperties.getCreated());
        }

        @Override
        public Optional<Date> getLastSaved() {
            return Optional.ofNullable(coreProperties.getModified());
        }
    }
}
