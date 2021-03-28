import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        Directory dir = new RAMDirectory();
        try (IndexWriter writer = new IndexWriter(dir, conf)) {
            Document doc1 = new Document();
            doc1.add(new TextField("pet", "cat", Field.Store.YES));
            doc1.add(new TextField("pet", "cat", Field.Store.YES));
            writer.addDocument(doc1);
            Document doc2 = new Document();
            doc2.add(new TextField("pet", "cat", Field.Store.YES));
            writer.addDocument(doc2);
        }
        try (DirectoryReader ireader = DirectoryReader.open(dir)) {
            IndexSearcher isearcher = new IndexSearcher(ireader);
            // Parse a simple query that searches for "text":
            QueryParser parser = new QueryParser("pet", analyzer);
            Query query = parser.parse("cat");
            ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;
            // Iterate through the results:
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = isearcher.doc(hits[i].doc);
                System.out.println(i + ":"+ hits[i].score + " - " +
                        Stream.of(hitDoc.getValues("pet")).collect(Collectors.joining(",")));
            }
        }
    }
}
