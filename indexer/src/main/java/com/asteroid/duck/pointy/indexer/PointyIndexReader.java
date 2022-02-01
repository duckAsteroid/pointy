package com.asteroid.duck.pointy.indexer;

import com.asteroid.duck.pointy.indexer.metadata.CoreFields;
import com.asteroid.duck.pointy.indexer.scan.IterableIndex;
import io.github.duckasteroid.progress.ProgressMonitor;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class PointyIndexReader {
	private static final Logger LOG = LoggerFactory.getLogger(PointyIndexReader.class);
	private final IndexReader reader;

	public PointyIndexReader(IndexReader reader) {
		this.reader = reader;
	}

	public SetValuedMap<String, String> currentIndex(ProgressMonitor monitor) {
		SetValuedMap<String, String> current = new HashSetValuedHashMap<>();
		if (reader != null) {
			IterableIndex iterableIndex = new IterableIndex(reader, CoreFields.CHECKSUM_FIELD.getFieldName(), CoreFields.FILENAME_FIELD.getFieldName());
			monitor.setSize(iterableIndex.size());
			for (Document doc : iterableIndex) {
				String hash = doc.get(CoreFields.CHECKSUM_FIELD.getFieldName());
				String[] filenames = doc.getValues(CoreFields.FILENAME_FIELD.getFieldName());
				current.putAll(hash, Arrays.asList(filenames));
				monitor.worked(1);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Current index contains "+current.size()+" pieces of content");
		}
		monitor.done();
		return current;
	}
}
