package com.asteroid.duck.pointy.query.result;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Represents the result of search - a thin JSON wrapper around Lucene index results
 */
@Data
@Jacksonized
@Builder
public class SearchResult {
	@Singular
	private final List<ResultDoc> documents;
}
