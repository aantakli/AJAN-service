Vision Language Plugin
======================

This plugin reads an image and converts it to RDF triples using the help of Image-to-Text models and LLMs. 
The plugin uses [Ollama](https://ollama.com/) framework to run the models and convert the image to RDF triples.
The plugin uses a local version of the Ollama framework, so ensure the Ollama framework is installed on your system.
Also, enough space should be ensured inorder to download the models and run the plugin.
There is two ways to run Ollama, using docker file provided and downloading the platform specific version of the tool.
The docker file is identified with issues such as slow start, inefficient GPU usage etc. 
So, it is recommended to download the platform specific version of the tool.

## Prerequisites
### Installation
#### Platform Specific Version
1. Navigate to the [Ollama Downloads page](https://ollama.com/download) and download the platform specific version of the tool.
2. Depending on your platform, download the tool and install it.
3. Ensure the 'OLLAMA_MODELS' environment variable is set if you would like to change the default path.
   - We recommend to set the path to the models folder (<AJAN-Service Path>/pluginsystem/plugins/VisionNLPPlugin/ollama/models).
#### Docker Version
1. Ensure that the Docker Engine is installed on your system and running.
2. Ensure that the port number `11434` is free to use.
2. Run the provided docker compose file (docker_compose.yml) to run the Ollama tool.
   - `docker-compose -f docker_compose.yml up`

### Download Models
1. Download the models according to the [Ollama Library page](https://ollama.com/library).
   - We recommend downloading the [Llama3](https://ollama.com/library/llama3) and [Llava](https://ollama.com/library/llava) Models
   - For example, to run llama3 the command `ollama run llama3` can be used. This automatically downloads the model to the models directory (mentioned in OLLAMA_MODELS environment variable) and runs the model.
2. Ensure the models are downloaded and the Ollama tool is installed properly before using the plugin.

