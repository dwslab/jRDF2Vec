# Technical Documentation

## Class Model

**Walk Generators/`IWalkGenerator`**
- All walk generators implement `IWalkGenerator`.
- Walk generators offer the service to create walks (`List<String>`) for single entities. 
- Walk generators are not responsible for the actual execution of the walk generation or multi-threading related issues.


**`WalkGenerationManager`**
- provides walk generation services by choosing the correct `WalkManager`and, therefore, implicitly calling the correct
walk generation module.
- Responsible for thread management.  


## How to add a walk generation flavor?
- Add your mode to enum `WalkGenerationMode` and extend method `getModeFromString`.
- Add a capability interface extending `IWalkGenerationCapability` which defines a method returning `List<String>`.
- Implement the capability in the desired `IWalkGenerator`s of your choice.
- Add a strategy-method mapping to the `run()` method of `DefaultEntityWalkRunnable`.
- Implement a Runnable (package `de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.runnables`).
- → The main method will support the walk generation mode out-of-the-box.