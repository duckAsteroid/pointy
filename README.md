# pointy
This project provides a tool for indexing and searching power-point files. 
It will monitor a set of directories for files (`.ppt` or `.pptx`) and will
index those into a Lucene search index. It uses Apache POI to read the slides.
At the same time it generates a set of thumbnail images (`.png`) for each slide
in the presentation. 

The unit of search or "document" is the slide - rather than the presentation.

The system looks at file contents (rather than filenames) for uniqueness 