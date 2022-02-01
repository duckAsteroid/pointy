package com.asteroid.duck.pointy.query.rest;

import com.asteroid.duck.pointy.Config;
import com.asteroid.duck.pointy.indexer.IndexDocType;
import com.asteroid.duck.pointy.indexer.metadata.CoreFields;
import com.asteroid.duck.pointy.indexer.metadata.OptionalField;
import com.asteroid.duck.pointy.query.result.ResultDoc;
import com.asteroid.duck.pointy.query.result.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static com.asteroid.duck.pointy.indexer.metadata.CoreFields.CHECKSUM_FIELD;

@RestController
@RequestMapping("rest")
public class Controller {
	private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

	private final IndexReader reader;
	private final IndexSearcher searcher;
	private final Analyzer analyzer;
	private final QueryParser parser;

	public Controller(Config cfg) throws IOException {
		Path indexFolder = cfg.getIndexFolder();
		Directory indexDirectory = FSDirectory.open(indexFolder);
		IndexWriterConfig indexConfig= new IndexWriterConfig(cfg.getLuceneAnalyzer());
		this.reader = DirectoryReader.open(indexDirectory);
		this.searcher = new IndexSearcher(reader);
		this.analyzer = cfg.getLuceneAnalyzer();
		this.parser = new QueryParser(OptionalField.CONTENT.name(), analyzer);
	}

	@PostMapping(path = "search", produces = MediaType.APPLICATION_JSON_VALUE)
	public SearchResult search(@RequestBody String expr, @RequestParam(value = "size", defaultValue = "10") int size) throws ParseException, IOException {
		SearchResult.SearchResultBuilder resultBuilder = SearchResult.builder();
		Query query = parser.parse(expr);
		TopDocs docs = searcher.search(query, size);
		for(ScoreDoc doc : docs.scoreDocs) {
			ResultDoc.ResultDocBuilder docBuilder = ResultDoc.builder();
			docBuilder.score(doc.score);
			Document document = reader.document(doc.doc);
			docBuilder.id(document.get(CoreFields.CHECKSUM_FIELD.getFieldName()));
			IndexDocType docType = IndexDocType.parse(document.get(IndexDocType.FIELD_NAME));
			docBuilder.docType(docType);
			if(docType == IndexDocType.SLIDESHOW) {
				docBuilder.detail("filename", document.get(CoreFields.FILENAME_FIELD.getFieldName()));
			} else if (docType == IndexDocType.SLIDE) {
				String parentId = document.get(CoreFields.PARENT.getFieldName());
				docBuilder.detail("parentId", parentId);
				getCurrentDocument(parentId).ifPresent((parent) -> {
					docBuilder.detail("parentFilename", parent.get(CoreFields.FILENAME_FIELD.getFieldName()));
				});
				docBuilder.detail("slideNo", document.get(OptionalField.SLIDE_NO.name()));
			}
			resultBuilder.document(docBuilder.build());
		}
		return resultBuilder.build();
	}

	private Optional<Document> getCurrentDocument(String hashcode) {
		try {
			TopDocs topDocs = searcher.search(new TermQuery(getChecksumTerm(hashcode)), 1);
			if (topDocs.totalHits.value > 0) {
				return Optional.of(reader.document(topDocs.scoreDocs[0].doc));
			}
		}
		catch(IOException e) {
			LOG.error("Error processing index action", e);
		}
		return Optional.empty();
	}

	public Term getChecksumTerm(String checksum) {
		return new Term(CHECKSUM_FIELD.getFieldName(), checksum);
	}
}
