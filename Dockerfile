FROM maven:3-jdk-8

# Install in /app
WORKDIR /app

RUN apt-get update -y && \
    apt-get install -y python3 python3-pip && \
    rm -f /usr/bin/python && ln -s /usr/bin/python3 /usr/bin/python
# Use Python3 by default instead of 2.7

# Install Python requirements
COPY ./src/main/resources/requirements.txt ./src/main/resources/requirements.txt
RUN pip3 install -r ./src/main/resources/requirements.txt

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

ENTRYPOINT [ "java", "-jar", "/app/jrdf2vec.jar" ]
CMD [ "-help" ]
# Default args if no args passed to docker run
