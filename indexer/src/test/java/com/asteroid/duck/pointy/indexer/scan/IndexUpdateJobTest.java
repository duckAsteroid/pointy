package com.asteroid.duck.pointy.indexer.scan;

import com.asteroid.duck.pointy.indexer.scan.actions.IndexAction;
import io.github.duckasteroid.progress.ProgressMonitorFactory;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class IndexUpdateJobTest {

	@Test
	void process() {
		SetValuedMap<String, String> index = new HashSetValuedHashMap<>();
		index.put("0xCAFE","c:/a/b/one.pptx"); // 1. `c:/a/b/one.pptx`, hash=`0xCAFE`
		index.put("0xCAFE","c:/b/c/1.pptx"); // 2. `c:/b/c/1.pptx`, hash=`0xCAFE`
		index.put("0x1234","c:/d/e/two.ppt"); // 3. `c:/d/e/two.ppt`, hash=`0x1234`
		index.put("0x5678","c:/f/g/three.pptx"); // 4. `c:/f/g/three.pptx`, hash=`0x5678`

		IndexUpdateJob subject = new IndexUpdateJob(index);
		List<Candidate> scanResult = new ArrayList<>();
		// 1. `c:/a/b/one.pptx`, hash=`0xCAFE` // no change
		scanResult.add(new Candidate(Paths.get("c:/a/b/one.pptx"), "0xCAFE"));
		// 2. `c:/b/c/1.pptx`, hash=`0x9876` // new content, existing file
		scanResult.add(new Candidate(Paths.get("c:/b/c/1.pptx"), "0x9876"));
		// 3. `c:/d/e/two.ppt` // deleted
		// nothing in new stream
		// 4. `c:/x/y/three.pptx`, hash=`0x5678` // new location for file
		scanResult.add(new Candidate(Paths.get("c:/x/y/three.pptx"), "0x5678"));
		// 5. `c:/f/g/four.pptx`, hash=`0xABCD`
		scanResult.add(new Candidate(Paths.get("c:/f/g/four.pptx"), "0xABCD"));

		// create actions
		subject.process(scanResult.stream(), ProgressMonitorFactory.newMonitor("Test", 1));
		// check actions are what we expect
		List<IndexAction> actions = subject.getActions().collect(Collectors.toList());
		assertEquals(5, actions.size());


	}
}
