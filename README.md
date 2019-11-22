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