# Walk Generation ALOD Classic
The method presented here is much faster than using Apache Jena TDB as in the original implementation. 

You can generate the files by downloadeding the n-quads file and calling `loadFromNquadsFile`. Relevant parts of 
the file will be loaded into memory. The method will also save a reduced version of the file which can be later on read
using `loadFromOptimizedFile` (this does the same as `loadFromNquadsFile` but is much faster).