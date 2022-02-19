FROM condaforge/mambaforge:4.10.3-7
# Alternativa mamba image with python 3.8: 4.9.2-5
LABEL org.opencontainers.image.source="https://github.com/dwslab/jRDF2Vec"

# Install in /app
WORKDIR /app

COPY ./src/main/resources/environment.yml ./src/main/resources/environment.yml

# Create the conda environment with mamba (faster than conda)
RUN mamba env create -f src/main/resources/environment.yml

# Make RUN commands use the new environment:
SHELL ["conda", "run", "-n", "jrdf2vec_env", "/bin/bash", "-c"]
ENV PATH /opt/conda/envs/jrdf2vec_env/bin:$PATH

# Install Java and maven with conda
RUN mamba install -y openjdk=11 maven


# Install HDT dependencies
RUN git clone https://github.com/rdfhdt/hdt-java.git && \
    cd ./hdt-java/ && \
    mvn clean install

# Only rebuild dependencies if change in pom.xml
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src

# Build jRDF2Vec, skip tests
RUN mvn -Dmaven.test.skip=true package && \
    mv ./target/jrdf2vec-*-SNAPSHOT.jar /app/jrdf2vec.jar


# Use /data folder to mount input files when running the container
WORKDIR /data
VOLUME /data

ENTRYPOINT ["conda", "run", "--no-capture-output", "-n", "jrdf2vec_env", "java", "-jar", "/app/jrdf2vec.jar"]

# Default args if no args passed to docker run
CMD [ "-help" ]
