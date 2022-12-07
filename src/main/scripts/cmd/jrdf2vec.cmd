SET jrdf2vec_location="<your path>\jrdf2vec-1.3-SNAPSHOT.jar"
conda activate jrdf2vec_env && ^
java -Xmx10G -jar %jrdf2vec_location% %*