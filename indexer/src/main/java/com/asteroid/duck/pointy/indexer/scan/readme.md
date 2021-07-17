This package contains classes that scan the file system for
target file types (e.g. powerpoints). From these files we produce a 
"hash" value based on the content. This is used to identify
multiple instances of the same file. Each combination of
a hash value, and a filename is known as a `Candidate`.

Next we move on to creating an `IndexAction` for each`Candidate`. 
These define how each action is to be processed in the indexing.

Let's start with an empty index and the following files:
1. `c:/a/b/one.pptx`, hash=`0xCAFE`
2. `c:/b/c/1.pptx`, hash=`0xCAFE`
3. `c:/d/e/two.ppt`, hash=`0x1234`
4. `c:/f/g/three.pptx`, hash=`0x5678`

After initial scanning from `c:/`. This would result in the following actions:
1. NewFile(`0xCAFE`, paths=[`c:/a/b/one.pptx`,`c:/b/c/1.pptx`])
2. NewFile(`0x1234`, paths=[`c:/d/e/two.ppt`])
3. NewFile(`0x5678`, paths=[`c:/f/g/three.pptx`])

Now let's assume the index has persisted, and the files have changed as follows:
1. `c:/a/b/one.pptx`, hash=`0xCAFE` // no change
2. `c:/b/c/1.pptx`, hash=`0x9876` // new content, existing file
3. `c:/d/e/two.ppt` // deleted
3. `c:/x/y/three.pptx`, hash=`0x5678` // new location for file
4. `c:/f/g/four.pptx`, hash=`0xABCD` // new file

This would result in the following operations:
1. UpdatePaths(`0xCAFE`, [`c:/a/b/one.pptx`])
2. NewFile(`0x9876`, [`c:/b/c/1.pptx`])
3. Remove(`0x1234`)
4. UpdatePaths(`0x5678`, [`c:/x/y/three.pptx`])
5. NewFile(`0xABCD`, [`c:/f/g/four.pptx`])

After processing the document `0x1234` would contain no file references
and would be purged from the index.