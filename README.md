# pointy
This project provides a tool for indexing and searching power-point files. 
It will monitor a set of directories for files (`.ppt` or `.pptx`) and will
index those into a Lucene search index. It uses Apache POI to read the slides.
This permits access to text and metadata on an individual slide basis. While this
indexing occurs the tools can generate a thumbnail image of each slide. 

The unit of search or "document" is the slide - rather than the presentation.

The system looks at file contents (rather than filenames) for uniqueness - using
a hash algorithm on file content to identify the same document.

The project is comprised of multiple sub-projects:

* *core* - Contains core classes for representing documents in the index, performing
checksums etc.
* *indexer* - A class that will walk over a set of folders and update an index.
* *query* - A library for reading and querying the index and thumbnail library
