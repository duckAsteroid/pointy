package com.asteroid.duck.pointy.indexer.metadata;

public enum CoreFields {
	CHECKSUM_FIELD("checksum"),
	FILENAME_FIELD("filename"),
	PARENT("parent"),
	THUMBNAIL_PATH_FIELD("thumbnailPath");
	private final String fieldName;

	CoreFields(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}
}
