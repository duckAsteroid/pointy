package com.asteroid.duck.pointy.indexer.scan.actions;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.*;
import com.asteroid.duck.pointy.indexer.scan.IndexUpdateJob;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.apache.poi.sl.usermodel.SlideShowFactory.create;

/**
 * Action to add a new file to the index
 */
public class NewFileAction extends IndexAction {

    private final Collection<Path> filenames;

    public NewFileAction(String checksum, Collection<Path> filenames) {
        super(checksum);
        this.filenames = filenames;
    }

    @Override
    public void process(IndexContext ctx) throws IOException {
        Config config = ctx.getConfig();
        Iterator<Path> iter = filenames.iterator();
        while (iter.hasNext()) {

            try {
                Document slideShowDocument = new Document();
                slideShowDocument.add(IndexDocType.SLIDE.asField());

                String filename = IndexUpdateJob.pathString(iter.next());
                SlideShow<?, ?> slideShow = create(new File(filename));
                SlideShowIndexer slideShowIdx = new SlideShowIndexer(checksum, slideShow, filenames);
                List<IndexableField> slideFields = slideShowIdx.index(config);

                // filenames
                filenames.stream()
                        .map(IndexUpdateJob::pathString)
                        .map(path -> new StringField(PipelineStage.FILENAME_FIELD, path, Field.Store.YES))
                        .forEach(slideShowDocument::add);
                slideFields.forEach(slideShowDocument::add);

                // add it to the index
                ctx.getWriter().addDocument(slideShowDocument);

                // Do we need to process the slides too?
                if (config.isSlideTextIndexed() || config.isSlideImageIndexed())
                {
                    Document slideDocument = new Document();
                    slideDocument.add(IndexDocType.SLIDE.asField());
                    // a reference to the parent
                    slideDocument.add(new StringField("parent", checksum, Field.Store.YES));
                    for(Slide<?,?> slide : slideShow.getSlides()) {
                        if (config.isSlideTextIndexed()) {
                            SlideTextIndexer textIndexer = new SlideTextIndexer(checksum, slide);
                            List<IndexableField> slideTextFields = textIndexer.index(config);
                            slideTextFields.forEach(slideDocument::add);
                        }
                        if (config.isSlideImageIndexed()) {
                            Path slidesFolder = ctx.getConfig().getSlideFolder(checksum);
                            SlideImageIndexer imageIndexer = new SlideImageIndexer(slidesFolder, slide);
                            List<IndexableField> slideImageFields = imageIndexer.index(config);
                            slideImageFields.forEach(slideDocument::add);
                        }
                    }
                    ctx.getWriter().addDocument(slideDocument);
                }
                break; // no more iterating
            }
            catch(IOException e) {
                // try next
                if (!iter.hasNext()) {
                    throw new IOException("Tried all filenames", e);
                }
            }
        }
    }
}
