package com.asteroid.duck.pointy.indexer.scan.actions;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static com.asteroid.duck.pointy.indexer.PipelineStage.CHECKSUM_FIELD;


public abstract class IndexAction {
    private static final Logger LOG = LoggerFactory.getLogger(IndexAction.class);

    protected final String checksum;

    protected IndexAction(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public Term getDocumentID() {
        return new Term(CHECKSUM_FIELD, getChecksum());
    }

    public Optional<Document> document(IndexContext ctx)  {
        try {
            TopDocs topDocs = ctx.getSearcher().search(new TermQuery(getDocumentID()), 1);
            if (topDocs.totalHits.value > 0) {
                return Optional.of(ctx.getReader().document(topDocs.scoreDocs[0].doc));
            }
        }
        catch(IOException e) {
            LOG.error("Error processing index action", e);
        }
        return Optional.empty();
    }

    public void safeProcess(IndexContext ctx) {
        try {
            process(ctx);
        }
        catch(IOException e) {
            LOG.error("Error processing", e);
        }
    }

    protected abstract void process(IndexContext ctx) throws IOException;
}
