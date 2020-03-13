# In Memory Walk Generation
Preferred way to generate random walks.


## Applications
- `GenerateClassicWalksApplication`<br/>
The starting point for the walk generation for the ALOD Classic data set.
- `GenerateClassicReverseWalksApplication`<br/>
*After* running `GenerateClassicWalksApplication`, this application can be run to generate reverse walks *in addition*. For the training in the Python project you have to move the resulting files in the same directory as those generated in the normal walk generation. 
- `GenerateXlWalksApplication`<br/>
If you want to generate XL walks, use this application. Note that the memory requirements for generation are very high. The requirements for training will be at least 350 GB of RAM for 200-dimensional embeddings. 