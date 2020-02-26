<a href="https://github.com/janothan/kgvec2go-walks/actions">![Java CI](https://github.com/janothan/kgvec2go-walks/workflows/Java%20CI/badge.svg)</a>

# KGvec2go Walks
This is the implementation of the walks that were used to train embeddings available on 
<a href="http://www.kgvec2go.org">http://www.kgvec2go.org</a>.
Note that the server that powers the *KGvec2go* Web page is located in repository 
<a href="https://github.com/janothan/kgvec2go-server/">KGvec2go Server</a>. 

The project can be packaged (`mvn package`) and then run as `jar` on a server. You can print the help by running 
the jar with `-help`. If you want a walk through, i.e. being asked by the program for every parameter that is required
rather than running the `jar` with many option parameters, you can execute the program with `-guided`.

Note that depending on the data set the 
computing requirements might be high. For BabelNet, the largest graph supported by this framework, more than 350 GB of 
RAM are required. Do not forget to increase the heap space when running the program (`-Xmx` and `-Xms` – place this 
before the `-jar` command).

You can generate walks for any `NT`, `OWL/XML`, or `TTL` file.
For the lightweigt generation, [`RDF HDT`](http://www.rdfhdt.org/) is also supported.

## Command Line Interface (CLI)

### Introduction
To run the CLI, download this repository and generate a jar file (run `mvn clean install` 
and check the target folder).

You can get the full help menu by running `java -jar <jar_file> -help`.

For convenience, you can start the walk generation with `java -jar <jar_file> -guided` 
and the program will ask for the required parameters.

Alternatively, you can start the program with given parameters directly (see *Example*) 
below.

### Example
To run the walk generation for the <a href="https://protege.stanford.edu/ontologies/pizza/pizza.owl">pizza ontology</a>, 
build the project and run the following command: 

```
java -jar walkGenerator-1.0-SNAPSHOT.jar -set any -res "<path_to_pizza_ontology>" -threads 10 -walks 10 -depth 10 -mode random_duplicate_free -unifyAnonymousNodes false -file "<path_to_file_to_be_written>"
```

Note that for BabelNet, DBnary, DBpedia, and WordNet (RDF) specific implementations are available (controllable via `-set`).
