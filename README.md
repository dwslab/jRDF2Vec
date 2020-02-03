# KGvec2go Walks
This is the implementation of the *KGvec2go* walks that wre used to train embeddings.
Note that the server that powers *KGvec2go* is located in repository 
<a href="https://github.com/janothan/kgvec2go-server/">KGvec2go Server</a>. 

The project can be packaged (`mvn package`) and then run as `jar` on a server. You can print the help by running 
the jar with `-help`. If you want a walk through, i.e. being asked by the program for every parameter that is required
rather than running the `jar` with many option parameters, you can execute the program with `-guided`.

Note that depending on the data set the 
computing requirements might be high. For BabelNet, the largest graph supported by this framework, more than 350 GB of 
RAM are required. Do not forget to increase the heap space when runnign the program (`-Xmx` and `-Xmx`).

You can generate walks for any `NT`, `OWL/XML`, or `TTL` file.

## Example
To run the walk generation for the <a href="https://protege.stanford.edu/ontologies/pizza/pizza.owl">pizza ontology</a>, 
build the project and run the following command: 

```
java -jar walkGenerator-1.0-SNAPSHOT.jar -set any -res "<path_to_pizza_ontology>" -threads 10 -walks 10 -depth 10 -duplicateFree true -unifyAnonymousNodes false -file "<path_to_file_to_be_written>"
```

Note that for BabelNet, DBnary, DBpedia, and WordNet (RDF) specific implementations are available (controllable via `-set`).
