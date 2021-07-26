package com.asteroid.duck.pointy.indexer.scan.actions;

import com.asteroid.duck.pointy.Config;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

public interface IndexActionContext {
    Config getConfig();
    IndexReader getReader();
    IndexWriter getWriter();
    IndexSearcher getSearcher();
}
