package com.asteroid.duck.pointy.query.result;

import com.asteroid.duck.pointy.indexer.IndexDocType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@Builder
@Jacksonized
public class ResultDoc {
	private final float score;
	private final String id;
	@Singular
	private final Map<String, String> details;
	private final IndexDocType docType;
}
