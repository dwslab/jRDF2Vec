# jRDF2Vec
[![Java CI](https://github.com/dwslab/jRDF2Vec/workflows/Java%20CI/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions)
[![Coverage Status](https://coveralls.io/repos/github/dwslab/jRDF2Vec/badge.svg?branch=master)](https://coveralls.io/github/dwslab/jRDF2Vec?branch=master)
[![License](https://img.shields.io/github/license/dwslab/jRDF2Vec)](https://github.com/dwslab/jRDF2Vec/blob/master/LICENSE)


jRDF2Vec is a Java implementation of <a href="http://rdf2vec.org/">RDF2Vec</a>. 
It supports multi-threaded, in-memory (or disk-access-based) walk generation and training.
You can generate embeddings for any `NT`, `NQ`, `OWL/XML`, [`RDF HDT`](http://www.rdfhdt.org/), 
[`TDB 1`](https://jena.apache.org/documentation/tdb/), or `TTL` file.

Found a bug? Don't hesitate to <a href="https://github.com/dwslab/jRDF2Vec/issues">open an issue</a>.

**How to cite?**
```
Portisch, Jan; Hladik, Michael; Paulheim, Heiko. RDF2Vec Light - A Lightweight Approach for Knowledge Graph Embeddings. Proceedings of the ISWC 2020 Posters & Demonstrations. 2020. [to appear]
```
An open-access version of the paper is available [here](https://arxiv.org/pdf/2009.07659.pdf).

## How to use the jRDF2Vec Command-Line Interface?
Download this project, execute `mvn clean install`.
Alternatively, you can download the packaged JAR of the latest successful: commit 
<a href="https://github.com/dwslab/jRDF2Vec/tree/jars/jars">here</a>. 

### System Requirements
- Java 8 or later.
- Python 3 with the dependencies described in [requirements.txt](/src/main/resources/requirements.txt) installed.

You can check if you set up the environment (Python 3 + dependencies) correctly by running:
```
java -jar jrdf2vec-1.1-SNAPSHOT.jar -checkInstallation
```
The command line output will list missing requirements or print `Installation is ok ✔`.


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
- `-depth <depth>` (default: `4`)<br/>
  This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you can also set an odd number. A depth of 1 leads to a sentence in the form `<s p o>`.
- `-walkGenerationMode <MID_WALKS | MID_WALKS_DUPLICATE_FREE | RANDOM_WALKS | RANDOM_WALKS_DUPLICATE_FREE>` 
(default for light: `MID_WALKS`, default for classic: `RANDOM_WALKS_DUPLICATE_FREE`)<br/>
This parameter determines the mode for the walk generation (multiple walk generation algorithms are available). 
- `-threads <number_of_threads>` (default: `(# of available processors) / 2`)<br/>
This parameter allows you to set the number of threads that shall be used for the walk generation as well as for the training.
- `-walkDirectory <directory where walk files shall be generated/reside>`<br/>
The directory where the walks shall be generated into. In case of `-onlyTraining`, the directory where the walks reside.
- `-embedText`<br>
If added to the call, this switch will also generate walks that contain textual fragments of datatype properties.

**Parameters for the Training Configuration**
- `-onlyTraining`<br/>
If added to the call, this switch will deactivate the walk generation part so that only the training is performed. The parameter `-walkDirectory` must be set. If walk generation parameters are specified, they are ignored.
- `-trainingMode <cbow | sg>` (default: `sg`) <br/>
This parameter controls the mode to be used for the word2vec training. Allowed values are `cbow` and `sg`.
- `-dimension <size_of_vector>` (default: `200`)<br/>
This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).
- `-minCount <number>` (default: `1`)<br/>
This parameter controls the minimum word count for the word2vec training. Unlike in the gensim defaults, this parameter is set to 1 by default because for knowledge graph embeddings, a vector for each node/arc is desired.
- `-noVectorTextFileGeneration` | `-vectorTextFileGeneration`<br/>
A switch which indicates whether a text file with the vectors shall be persisted on the disk. This is enabled by default. Use `-noVectorTextFileGeneration` to disable the file generation.
- `-sample <rate>` (default: `0.0`)<br/>
The threshold for configuring which higher-frequency words are randomly downsampled, a useful range is, according to the gensim framework, (0, 1e-5).
- `-window <window_size>` (default: `5`)<br/>
The size of the window in the training process.
- `-epochs <number_of_epochs>` (default: `5`)<br/>
The number of epochs to use in training.
- `-port <port_number>` (default: `1808`)<br/>
The port that shall be used for the server.

**Advanced Parameters**
- `-continue <existing_walk_directory>`<br/>
In some cases, old walks need to be re-used (e.g. if the program was interrupted after 48h). 
With the `-continue` option, the walk generation can be continued; this means that old walks will be re-used and only
missing walks are generated. This does not work for MID_WALKS (and flavors). If you do not need to generate additional 
walks use `-onlyTraining` instead.
  

### Command-Line Interface (jRDF2Vec CLI) - Additional Services
Besides generating walks and training embeddings, the CLI offers additional services which are described below.

#### Generating a Vector Text File

*(1) Full Vocabulary*<br/>
jRDF2vec is compatible with the <a href="https://github.com/mariaangelapellegrino/Evaluation-Framework">evaluation 
framework for KG embeddings (GEval)</a>. 
This framework requires the vectors to be present in a text file. If you have a gensim model or vector file, you can
use the following command to generate this file:

```
java -jar jrdf2vec-1.1-SNAPSHOT.jar -generateTextVectorFile ./path-to-your-model-or-vector-file
```
You can find the file (named `vectors.txt`) in the directory where the model/vector file is located.

*(2) Subset of the  Vocabulary*<br/>
If you want to write a `vectors.txt` file that contains only a subset of the vocabulary, you can alternatively 
specify the entities of interest using the `-light <entity_file>` option (The `<entity_file>` should contain one entity 
(full URI) per line.):

```
java -jar jrdf2vec-1.1-SNAPSHOT.jar -generateTextVectorFile ./path-to-your-model-or-vector-file -light ./path-to-entity-file
```

#### Generating a Vocabulary Text File
jRDF2vec provides functionality to print all concepts for which a vector has been trained.
One word of the vocabulary will be printed per line to a file named `vocabulary.txt`.
The model or vector file needs to be specified. If you have a gensim model or vector file, you can
use the following command to generate this file:

```
java -jar jrdf2vec-1.1-SNAPSHOT.jar -generateVocabularyFile ./path-to-your-model-or-vector-file
```

#### Analyzing the Embedding Vocabulary
For RDF2Vec, it is not always guaranteed that all concepts in the graph appear in the embedding space. For example,
some concepts may only appear in the object position of statements and may never be reached by random walks.
In addition, the word2vec configuration parameters may filter out infrequent words depending on the configuration (see
`-minCount` above, for example). To analyze such rather seldom cases, you can use the `-analyzeVocab` function specified
as follows:

```
java -jar jrdf2vec-1.1-SNAPSHOT.jar -analyzeVocab <model> <training_file|entity_file>
```
- `<model>` refers to any model representation such as gensim model file, `.kv` file, or `.txt` file. Just make sure
you use the correct file endings.
  
- `<training_file|entity_file>` refers either to the NT/TTL etc. file that has been used to train the model *or* to a 
text file containing the concepts you want to check (one concept per line in the text file, make sure the file ending is 
`.txt`).
  
A report will be printed. For large models, you may want to redirect that into a file (`[...] &> somefile.txt)`.

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

**Can I run the command multiple times in parallel on the same machine?**<br/>
Yes, you can. You need to make sure that for each command, you use (1) a different `-port` and (2) a different 
`-walkDirectory`.