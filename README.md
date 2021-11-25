# jRDF2Vec
[![Java CI](https://github.com/dwslab/jRDF2Vec/workflows/Java%20CI/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions)
[![Publish Docker image](https://github.com/dwslab/jRDF2Vec/actions/workflows/publish-docker.yml/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions/workflows/publish-docker.yml)
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
- Python 3.8 with the dependencies described in [requirements.txt](/src/main/resources/requirements.txt) installed.<br> 
  (Conda users can directly use the [environment.yml](/src/main/resources/environment.yml) file.)

You can check if you set up the environment (Python 3 + dependencies) correctly by running:
```bash
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
The latter framework requires the vectors to be present in a text file. If you have a gensim model or vector file, 
you can use the following command to generate this file:

```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -generateTextVectorFile ./path-to-your-model-or-vector-file
```
You can find the file (named `vectors.txt`) in the directory where the model/vector file is located.
If you want to specify the file name/path yourself, you can use option `-newFile <file_path>`.

*(2) Subset of the  Vocabulary*<br/>
If you want to write a `vectors.txt` file that contains only a subset of the vocabulary, you can additionally 
specify the entities of interest using the `-light <entity_file>` option (The `<entity_file>` should contain one entity 
(full URI) per line.):

```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -generateTextVectorFile ./path-to-your-model-or-vector-file -light ./path-to-entity-file
```
You can find the file (named `vectors.txt`) in the directory where the model/vector file is located.
If you want to specify the file name/path yourself, you can use option `-newFile <file_path>`.
If the vector concepts contain surrounding tags that you want to remove in the process, use option `-noTags`.
This command also works if `./path-to-your-model-or-vector-file` is an existing vector text file that shall be reduced.

#### Generating a Vocabulary Text File
jRDF2vec provides functionality to print all concepts for which a vector has been trained.
One word of the vocabulary will be printed per line to a file named `vocabulary.txt`.
The model or vector file needs to be specified. If you have a gensim model or vector file, you can
use the following command to generate this file:

```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -generateVocabularyFile ./path-to-your-model-or-vector-file
```

#### Converting a Text Vector File
jRDF2vec generates a `vectors.txt` file where one line represents a vector. This is the format also used by 
[GloVe](https://github.com/stanfordnlp/GloVe), for instance. 
In some cases, however, other file formats are required. You can use jRDF2vec to convert text vector files to other
common formats. The vector file does not have to be generated by jRDF2vec.

*(1) Converting to w2v Format*<br/>
To create a word2vec formatted file from the text file, you can use the following command:
```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -convertToW2V <txt_file_path> <new_file.w2v>
```

*(2) Converting to kv Format*<br/>
The provided txt file (first parameter) can be either in `txt` format or in `w2v` format. Make sure you use the 
correct file ending (`.txt`/`.w2v`).

You can run the command as follows:
```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -convertToKv <txt_file_path> <new_file.kv>
```

*(3) Converting to Tensorflow Projector Format*<br/>
If you want to visualize your embedding space by using the [Tensorflow Projector](http://projector.tensorflow.org/),
you can do so by converting your `vectors.txt` file to the two files required by the tool. Use the following command:
```
java -jar jrdf2vec-1.1-SNAPSHOT.jar -convertToTfProjector <txt_file_path> [<vectors.tsv> <metadata.tsv>]
```
Two additional `.tsv` files will be generated. You can find them in the same directory where `<txt_file_path>` is 
located.

Optionally, you can specify the paths of the files to be written as indicated in the command above.

#### Analyzing the Embedding Vocabulary
For RDF2Vec, it is not always guaranteed that all concepts in the graph appear in the embedding space. For example,
some concepts may only appear in the object position of statements and may never be reached by random walks.
In addition, the word2vec configuration parameters may filter out infrequent words depending on the configuration (see
`-minCount` above, for example). To analyze such rather seldom cases, you can use the `-analyzeVocab` function specified
as follows:

```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -analyzeVocab <model> <training_file|entity_file>
```
- `<model>` refers to any model representation such as gensim model file, `.kv` file, or `.txt` file. Just make sure
  you use the correct file endings.
  
- `<training_file|entity_file>` refers either to the NT/TTL etc. file that has been used to train the model *or* to a 
  text file containing the concepts you want to check (one concept per line in the text file, make sure the file ending is 
  `.txt`).
  

A report will be printed. For large models, you may want to redirect that into a file (`[...] &> somefile.txt)`.

#### Merge of All Walk Files Into One
By default, jRDF2vec serializes walks in different gzipped files. If you require a single,
uncompressed file, you can use the `-mergeWalks` keyword. You need to provide a
`-walkDirectory <dir>` and you can optionally specify the output file using `-o <file_path>`.
(Files not ending with `.gz` in `<dir>` will be skipped.)

```bash
java -jar jrdf2vec-1.1-SNAPSHOT.jar -mergeWalks -walkDirectory <dir> -o <file_to_write>
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

[![Publish Docker image](https://github.com/dwslab/jRDF2Vec/actions/workflows/publish-docker.yml/badge.svg)](https://github.com/dwslab/jRDF2Vec/actions/workflows/publish-docker.yml) 

Optionally, Docker can be used to run jRDF2Vec. This functionality has been added by <a href="https://github.com/vemonet">Vincent Emonet</a>.

### Run

The Docker image can be used with the same arguments as the Jar file, refer to the documentation above for more details on the different jRDF2Vec arguments.

Test run to get the help message:

```bash
docker run -it --rm ghcr.io/dwslab/jrdf2vec -help
```

The best way to mount your local files in the docker container is to mount a folder on `/data` in the container:

* On Linux and MacOS: use `$(pwd)` to mount the current working directory
* On Windows:  use `${PWD}` to mount the current working directory (and make the command in one line)

Here is an example generating embeddings using sample config files for DBpedia found in [`src/test/resources`](https://github.com/dwslab/jRDF2Vec/tree/master/src/test/resources) in this repository. Use this command from the root folder of this repository on Linux or MacOS, change the `$(pwd)` to `${PWD}` for Windows:

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

Build from source code:

```bash
docker build -t ghcr.io/dwslab/jrdf2vec .
```

## Developer Documentation
The most recent JavaDoc sites generated from the latest commit can be found <a href="https://dwslab.github.io/jRDF2Vec/">here</a>.<br/>

## Special Applications

### Ordered RDF2Vec ("Putting RDF2vec in Order")
The following steps are necessary to obtain ordered RDF2vec embeddings (see publication [Putting RDF2vec in Order](https://arxiv.org/pdf/2108.05280.pdf) for conceptional details).

**Step 1: Generate Walks**<br/>
Run jRDF2Vec to generate only walks (option [`-onlyWalks`](#optional-parameters)) on your desired dataset.

**Step 2: Merge the Walks in a single, uncompressed file**<br/>
By default, jRDF2Vec serializes the walks in multiple gzipped files. For this application, however, we need a single,
uncompressed walk file.

You can use the [corresponding jRDF2Vec command line service](#merge-of-all-walk-files-into-one) to do so.

**Step 3: Compile wang2vec**<br/>
Download the C implementation of [wang2vec from GitHub](https://github.com/wlin12/wang2vec).
Compile the files with `make`.

**Step 4: Run and have fun**<br/>
Run the compiled wang2vec implementation on the merged walk file from step 2. In case you receive a `segfault` error,
set the capping parameter to 1 (`-cap 1`).

*Call Syntax*<br/>
```bash
./word2vec -train <your walk file> -output <desired file to be written> - type <2 (cwindow) or 3 (structured 
skipgram>) -size <vector size> -threads <number of threads> -min-count 0 -cap 1  
```

*Exemplary Call*<br/>
```bash
./word2vec -train walks.txt -output v100.txt -type 3 -size 100 -threads 4 -min-count 0 -cap 1  
```

**Not working? Contact us or open an issue.**

Please do not forget to cite the corresponding papers:

```
(1)  Portisch, Jan; Paulheim, Heiko. Putting RDF2vec in Order. In: Proceedings of the International Semantic Web 
Conference - Posters and Demos, ISWC 2021. 2021. 

(2) Ling, Wang; Dyer, Chris; Black, Alan; Trancoso, Isabel. Two/too simple adaptations of word2vec for syntax 
problems. In: NAACL HLT 2015. pp. 1299–1304. ACL (2015)
```


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