# jRDF2vec
[![Java CI](https://github.com/dwslab/jRDF2Vec/workflows/Java%20CI/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions)
[![Coverage Status](https://coveralls.io/repos/github/dwslab/jRDF2Vec/badge.svg?branch=master)](https://coveralls.io/github/dwslab/jRDF2Vec?branch=master)
[![License](https://img.shields.io/github/license/dwslab/jRDF2Vec)](https://github.com/dwslab/jRDF2Vec/blob/master/LICENSE)


jRDF2Vec is a Java implementation of <a href="http://rdf2vec.org/">RDF2Vec</a>. 
It supports multi-threaded, in-memory (or disk-access-based) walk generation and training.
You can generate embeddings for any `NT`, `OWL/XML`, [`RDF HDT`](http://www.rdfhdt.org/), or `TTL` file.

Found a bug? Don't hesitate to <a href="https://github.com/dwslab/jRDF2Vec/issues">open an issue</a>.

## How to use the jRDF2vec Command-Line Interface?
Download this project, execute `mvn clean install`. 

### System Requirements
- Java 8 or later.
- Python 3 with the dependencies described in [requirements.txt](/src/main/resources/requirements.txt) installed.

### Command-Line Interface (jRDF2Vec CLI)
Use the resulting jar from the `target` directory.

*Minimal Example*
```bash
java -jar jrdf2vec-1.0-SNAPSHOT.jar -light ./file-to-your-entities.txt -graph ./kg_file.hdt
```

#### Required Parameters
- `-graph <graph_file>`<br/>
The file containing the knowledge graph for which you want to generate embeddings.

#### Optional Parameters
*jRDF2Vec* follows the <a href="https://en.wikipedia.org/wiki/Convention_over_configuration">convention over 
configuration</a> design paradigm to increase usability. You can overwrite the default values by setting one or more optional parameters.

**Parameters for the Walk Configuration**
- `-onlyWalks`<br>
If added to the call, this switch will deactivate the training part so that only walks are generated. If training parameters are specified, they are ignored. The walk generation also works with the `-light` parameter.
- `-light <entity_file>`<br/>
If you intend to use *RDF2Vec Light*, you have to use this switch followed by the file path ot the describing the entities for which you require an embedding space. The file should contain one entity (full URI) per line.
- `-numberOfWalks <number>` (default: `100`)<br/>
The number of walks to be performed per entity.
- `-walkGenerationMode <MID_WALKS | MID_WALKS_DUPLICATE_FREE | RANDOM_WALKS | RANDOM_WALKS_DUPLICATE_FREE>` 
(default for light: `MID_WALKS`, default for classic: `RANDOM_WALKS_DUPLICATE_FREE`)<br/>
This parameter determines the mode for the walk generation (multiple walk generation algorithms are available). 
- `-depth <depth>` (default: `4`)<br/>
This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you can also set an odd number. A depth of 1 leads to a sentence in the form `<s p o>`.
- `-threads <number_of_threads>` (default: `(# of available processors) / 2`)<br/>
This parameter allows you to set the number of threads that shall be used for the walk generation as well as for the training.

**Parameters for the Training Configuration**
- `-trainingMode <cbow | sg>` (default: `sg`) <br/>
This parameter controls the mode to be used for the word2vec training. Allowed values are `cbow` and `sg`.
- `-dimension <size_of_vector>` (default: `200`)<br/>
This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).
- `-minCount <number>` (default: `1`)<br/>
The minimum word count for the word2vec training. Unlike in the gensim defaults, this parameter is set to 1 by default because for knowledge graph embeddings, a vector for each node/arc is desired.

## Run jRDF2Vec using Docker
Optionally, Docker can be used to run jRDF2Vec.

### Run

The image can be pulled from [DockerHub 🐳](https://hub.docker.com/repository/docker/vemonet/jrdf2vec)

Test run to get help message:

```bash
docker run -it --rm vemonet/jrdf2vec
```

Mount volumes on `/data` in the container to provide input files and generate embeddings:

* `$(pwd)` to use current working directory on Linux and MacOS
* `${PWD}` to use current working directory on Windows (also make the command a one-line)

```bash
docker run -it --rm \
  -v $(pwd)/src/test/resources:/data \
  vemonet/jrdf2vec \
  -light /data/sample_dbpedia_entity_file.txt \
  -graph /data/sample_dbpedia_nt_file.nt
```

> Embeddings will be generated in the shared volume (`/data` in the container).

### Build

From source code:

```bash
docker build -t jrdf2vec .
```

## Developer Documentation
The most recent JavaDoc sites generated from the latest commit can be found <a href="https://dwslab.github.io/jRDF2Vec/">here</a>.<br/>