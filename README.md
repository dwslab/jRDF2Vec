# jRDF2vec
[![Java CI](https://github.com/dwslab/jRDF2Vec/workflows/Java%20CI/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions)
[![Coverage Status](https://coveralls.io/repos/github/dwslab/jRDF2Vec/badge.svg?branch=master)](https://coveralls.io/github/dwslab/jRDF2Vec?branch=master)

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
```bash
java -jar jrdf2vec-1.0-SNAPSHOT.jar -light ./file-to-your-entities.txt -graph ./kg_file.hdt
```

#### Required Parameters
- `-graph <graph_file>`<br/>
The file containing the knowledge graph for which you want to generate embeddings.

#### Optional Parameters
*jRDF2Vec* follows the <a href="https://en.wikipedia.org/wiki/Convention_over_configuration">convention over 
configuration</a> design paradigm to increase usability. You can overwrite the default values by setting one or more optional parameters.

- `-light <entity_file>`<br/>
If you intend to use *RDF2Vec Light*, you have to use this switch followed by the file path ot the describing the entities for which you require an embedding space. The file should contain one entity (full URI) per line.
- `-onlyWalks`<br>
If added to the call, this switch will deactivate the training part so that only walks are generated. If training parameters are specified, they are ignored. The walk generation also works with the `-light` parameter.
- `-threads <number_of_threads>` (default: `(# of available processors) / 2`)<br/>
This parameter allows you to set the number of threads that shall be used for the walk generation as well as for the training.
- `-dimension <size_of_vector>` (default: `200`)<br/>
This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).
- `-depth <depth>` (default: `4`)<br/>
This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you can also set an odd number. A depth of 1 leads to a sentence in the form `<s p o>`.
- `-trainingMode <cbow | sg>` (default: `sg`) <br/>
This parameter controls the mode to be used for the word2vec training. Allowed values are `cbow` and `sg`.
- `-numberOfWalks <number>` (default: `100`)<br/>
The number of walks to be performed per entity.
- `-walkGenerationMode <MID_WALKS | MID_WALKS_DUPLICATE_FREE | RANDOM_WALKS | RANDOM_WALKS_DUPLICATE_FREE>` 
(default for light: `MID_WALKS`, default for classic: `RANDOM_WALKS_DUPLICATE_FREE`)<br/>
This parameter determines the mode for the walk generation (multiple walk generation algorithms are available). 

Found a bug? Don't hesitate to <a href="https://github.com/dwslab/jRDF2Vec/issues">open an issue</a>.

## Run using Docker

[![Publish Docker image](https://github.com/dwslab/jRDF2Vec/actions/workflows/publish-docker.yml/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions/workflows/publish-docker.yml) 

### Run

The Docker image can be used with the same arguments as the Jar file, refer to the documentation above for more details on the different arguments.

Test run to get help message:

```bash
docker run -it --rm ghcr.io/dwslab/jrdf2vec -help
```

The best way to mount your local files in the docker container is to mount a folder on `/data` in the container:

* On Linux and MacOS: use `$(pwd)` to mount the current working directory
* On Windows:  use `${PWD}` to mount the current working directory (and make the command in one line)

Here is an example generating embeddings using sample config files for DBpedia found in [`src/test/resources`](https://github.com/dwslab/jRDF2Vec/tree/master/src/test/resources) (to run from the root folder of this repository, on Linux or MacOS, change the `$(pwd)` for Windows):

```bash
docker run -it --rm \
  -v $(pwd):/data \
  ghcr.io/dwslab/jrdf2vec \
  -light /data/src/test/resources/sample_dbpedia_entity_file.txt \
  -graph /data/src/test/resources/sample_dbpedia_nt_file.nt
```

> Embeddings will be generated in the folders `walks` and `python_server` from where you ran the command.

### Build

A new docker image is automatically built and published to the GitHub Container Registry by a [GitHub Actions workflow](https://github.com/dwslab/jRDF2Vec/actions/workflows/publish-docker.yml): 

* The `latest` image tag is updated everytime a commit is pushed to the `master` branch
* A new image tag is created for every new release published following the scheme `v0.0.0`

From source code:

```bash
docker build -t ghcr.io/dwslab/jrdf2vec .
```