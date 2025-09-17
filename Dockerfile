FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine

# Install dependencies
RUN apk update && \
    apk add --no-cache supervisor wget ca-certificates curl libstdc++ python3 py3-pip py3-wheel && \
    apk add --no-cache libstdc++=11.2.1_git20220219-r2 --repository https://dl-cdn.alpinelinux.org/alpine/v3.16/main --allow-untrusted && \
    apk list -I | grep -E 'libstdc++'

WORKDIR /app

# Copy application files
COPY triplestore-0.1-war-exec.jar executionservice-0.1.jar .env ./
COPY docker ./
COPY executionservice/use-case ./executionservice/use-case
COPY pluginsystem/plugins ./pluginsystem/plugins

# Set permissions and make scripts executable
RUN chmod +x /app/startup.sh /app/create.sh && \
    chmod -R +rx /app/pluginsystem/plugins/PythonPlugin/target/classes && \
    chmod -R +rx /app/pluginsystem/plugins/ASPPlugin/target/classes

## Setup ASPPlugin and PythonPlugin
#RUN python3 -m pip install --upgrade pip && \
#    pip install clingo && \
#    mkdir -p /usr/lib/python3.9/scrpt && \
#    echo python3 /usr/lib/python3.9/site-packages/clingo > /usr/lib/python3.9/scrpt/clingo && \
#    chmod +x /usr/lib/python3.9/scrpt/clingo && \
#    cp /usr/bin/python3 /app/pluginsystem/plugins/PythonPlugin/target/classes/nix_venv/bin/python && \
#    chmod +x /usr/lib/python3.9/site-packages/clingo

ENV PATH="$PATH:/usr/lib/python3.9/scrpt"

WORKDIR /logs
VOLUME /logs
VOLUME /app/executionservice/use-case

EXPOSE 8080/tcp
EXPOSE 8090/tcp

ENV url="http://localhost:8090/rdf4j"
ENV repoURL="http://localhost:8090/rdf4j/repositories/"
ENV DloadTTLFiles="true"
ENV DpublicHostName="localhost"

ENTRYPOINT ["/bin/sh", "/app/startup.sh"]
