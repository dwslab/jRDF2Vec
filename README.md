[![Java CI](https://github.com/dwslab/jRDF2Vec/workflows/Java%20CI/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions)
[![Coverage Status](https://coveralls.io/repos/github/dwslab/jRDF2Vec/badge.svg?branch=master)](https://coveralls.io/github/dwslab/jRDF2Vec?branch=master)

# jRDF2vec
jRDF2Vec is a Java implementation of the RDF2Vec. 
It supports multi-threaded, in-memory (or disk-access-based) walk generation and training.
You can generate embeddings for any `NT`, `OWL/XML`, [`RDF HDT`](http://www.rdfhdt.org/), or `TTL` file.

## How to use jRDF2vec?
Download this project, execute `mvn clean install`. 

### System Requirements
- Java 8 or later.
- Python 3 with the dependencies described in [requirements.txt](/src/main/resources/requirements.txt) installed.

### Command-Line Interface (jRDF2Vec CLI)
Use the resulting jar from the `target` directory.

*Minimal Example*
```
java -jar jrdf2vec-1.0-SNAPSHOT.jar -light ./file-to-your-entities.txt -graph ./kg_file.hdt
```

#### Required Parameters
- `-graph <graph_file>`<br/>
The file containing the knowledge graph for which you want to generate embeddings.

#### Optional Parameters
*jRDF2Vec* follows the <a href="https://en.wikipedia.org/wiki/Convention_over_configuration">convention over 
configuration</a> design paradigm to increase usability. You can overwrite the default values by setting one or more
optional parameters.
- `-light <entity_file>`<br/>
If you intend to use *RDF2Vec Light*, you have to use this switch followed by the file path ot the describing the entities
for which you require an embedding space. The file should contain one entity (full URI) per line.
- `-threads <number_of_threads>` (default: `(# of available processors) / 2`)<br/>
This parameter allows you to set the number of threads that shall be used for the walk generation as well as for the 
training.
- `-dimension <size_of_vector>` (default: `200`)<br/>
This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).
- `-depth <depth>` (default: `4`)<br/>
This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you can also set an odd
number. A depth of 1 leads to a sentence in the form `<s p o>`.
- `-trainingMode <cbow|sg>` (default: `cbow`) <br/>
This parameter controls the mode to be used for the word2vec training. Allowed values are `cbow` and `sg`.
- `-numberOfWalks <number>` (default: `100`)<br/>
The number of walks to be performed per entity.

Found a bug? Don't hesitate to <a href="https://github.com/dwslab/jRDF2Vec/issues">open an issue</a>.