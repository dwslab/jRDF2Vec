# jRDF2Vec
[![Java CI](https://github.com/dwslab/jRDF2Vec/workflows/Java%20CI/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions)
[![Coverage Status](https://coveralls.io/repos/github/dwslab/jRDF2Vec/badge.svg?branch=master)](https://coveralls.io/github/dwslab/jRDF2Vec?branch=master)
[![License](https://img.shields.io/github/license/dwslab/jRDF2Vec)](https://github.com/dwslab/jRDF2Vec/blob/master/LICENSE)


jRDF2Vec is a Java implementation of <a href="http://rdf2vec.org/">RDF2Vec</a>. 
It supports multi-threaded, in-memory (or disk-access-based) walk generation and training.
You can generate embeddings for any `NT`, `NQ`, `OWL/XML`, [`RDF HDT`](http://www.rdfhdt.org/), or `TTL` file.

Found a bug? Don't hesitate to <a href="https://github.com/dwslab/jRDF2Vec/issues">open an issue</a>.

**How to cite?**
```
Portisch, Jan; Hladik, Michael; Paulheim, Heiko. RDF2Vec Light - A Lightweight Approach for Knowledge Graph Embeddings. Proceedings of the ISWC 2020 Posters & Demonstrations. 2020. [to appear]
```
An open-access version of the paper is available [here](https://arxiv.org/pdf/2009.07659.pdf).

## How to use the jRDF2Vec Command-Line Interface?
Download this project, execute `mvn clean install`.
Alternatively, you can download the packaged JAR of the latest successful: commit <a href="https://github.com/dwslab/jRDF2Vec/tree/jars/jars">here</a>. 

### System Requirements
- Java 8 or later.
- Python 3 with the dependencies described in [requirements.txt](/src/main/resources/requirements.txt) installed.

### Command-Line Interface (jRDF2Vec CLI) for Training and Walk Generation
Use the resulting jar from the `target` directory.

*Minimal Example*
```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -graph ./kg_file.hdt
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
If you intend to use *RDF2VecLight*, you have to use this switch followed by the file path ot the describing the entities for which you require an embedding space. The file should contain one entity (full URI) per line.
- `-numberOfWalks <number>` (default: `100`)<br/>
The number of walks to be performed per entity.
- `-walkGenerationMode <MID_WALKS | MID_WALKS_DUPLICATE_FREE | RANDOM_WALKS | RANDOM_WALKS_DUPLICATE_FREE>` 
(default for light: `MID_WALKS`, default for classic: `RANDOM_WALKS_DUPLICATE_FREE`)<br/>
This parameter determines the mode for the walk generation (multiple walk generation algorithms are available). 
- `-depth <depth>` (default: `4`)<br/>
This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you can also set an odd number. A depth of 1 leads to a sentence in the form `<s p o>`.
- `-threads <number_of_threads>` (default: `(# of available processors) / 2`)<br/>
This parameter allows you to set the number of threads that shall be used for the walk generation as well as for the training.
- `-walkDirectory <directory where walk files shall be generated/reside>`<br/>
The directory where the walks shall be generated into. In case of `-onlyTraining`, the directory where the walks reside.

**Parameters for the Training Configuration**
- `-trainingMode <cbow | sg>` (default: `sg`) <br/>
This parameter controls the mode to be used for the word2vec training. Allowed values are `cbow` and `sg`.
- `-dimension <size_of_vector>` (default: `200`)<br/>
This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).
- `-minCount <number>` (default: `1`)<br/>
This parameter controls the minimum word count for the word2vec training. Unlike in the gensim defaults, this parameter is set to 1 by default because for knowledge graph embeddings, a vector for each node/arc is desired.
- `-noVectorTextFileGeneration` | `-vectorTextFileGeneration`<br/>
A switch which indicates whether a text file with the vectors shall be persisted on the disk. This is enabled by default. Use `-noVectorTextFileGeneration` to disable the file generation.
- `-onlyTraining`<br/>
If added to the call, this switch will deactivate the walk generation part so that only the training is performed. The parameter `-walkDirectory` must be set. If walk generation parameters are specified, they are ignored.
- `-sample` (default: `0.0`)<br/>


### Command-Line Interface (jRDF2Vec CLI) - Additional Services
Besides generating walks and training embeddings, the CLI offers additional services which are described below.

#### Generating a Text Vector File
jRDF is compatible with the <a href="https://github.com/mariaangelapellegrino/Evaluation-Framework">evaluation framework for KG embeddings (GEval)</a>. 
This framework requires the vectors to be present in a text file. If you have a gensim model or vector file, you can
use the following command to generate this file:

```
java -jar jrdf2vec-1.1-SNAPSHOT.jar -generateTextVectorFile ./path-to-your-model-or-vector-file
```

## How to use the jRDF2Vec as library in Java projects?
Stable releases are available through the maven central repository:
```
<dependency>
    <groupId>de.uni-mannheim.informatik.dws</groupId>
    <artifactId>jrdf2vec</artifactId>
    <version>1.0</version>
</dependency>
```


## Run jRDF2Vec using Docker
Optionally, Docker can be used to run jRDF2Vec. This functionality has been added by <a href="https://github.com/vemonet">Vincent Emonet</a>.

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

## Frequently Asked Questions (FAQs)
**I have Python installed, but it is not accessible via command `python`. How to resolve this?**<br/>
Create a file `python_command.txt` in directory `./python_server` (created when first running the jar). Write the command
to call Python 3 in the first line of the file.

**The program starts and immediately shuts down. Nothing seems to happen.**<br/>
Make sure your system is set-up correctly, in particular whether you have installed Python 3 and the required 
dependencies.
