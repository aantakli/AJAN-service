import requests


class AjanAgent:
    """
    Ajan Agent base class which helps to communicate and create Ajan agent using initial knowledge and a given template
    """
    prefixes = list()
    agent_initialization = list()
    agent_initKnowledge = list()
    initialKnowledge = None
    agent_id = None

    URL = None
    __headers = None

    def __init__(self, agentId, agentTemplate, url=None, headers=None):
        self.__headers = {'Content-type': 'text/turtle'} if headers is None else headers
        self.URL = "http://localhost:8080/ajan/agents/" if url is None else url
        self.__setupPrefixes()
        self.agent_id = agentId
        self.__setupInitialization(self.agent_id, agentTemplate)

    def __setupPrefixes(self):
        """
        Sets up the initial prefixes needed
        """
        self.prefixes.append('@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .')
        self.prefixes.append('@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .')
        self.prefixes.append('@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .')
        self.prefixes.append('@prefix ajan: <http://www.ajan.de/ajan-ns#> .')
        self.prefixes.append('@prefix inv: <http://www.dfki.de/inversiv#> .')
        self.prefixes.append('@prefix : <http://www.dfki.de/inversiv#> .')
        self.prefixes.append('@prefix bt: <http://www.ajan.de/behavior/bt-ns#> .')
        self.prefixes.append('@prefix http: <http://www.w3.org/2006/http#> .')

    def __setupInitialization(self, agentId: str, agentTemplate: str):
        """
        Sets up the initial initialization subjects
        """
        self.agent_initialization.append(':initialization rdf:type ajan:AgentInitialisation ;')
        self.agent_initialization.append(f'				ajan:agentId "{agentId}";')
        self.agent_initialization.append(f'				ajan:agentTemplate <{agentTemplate}> ;')

    def __addInitialization(self, initialization: str) -> None:
        """
        adds initialization subjects
        """
        if initialization not in self.agent_initialization:
            self.agent_initialization.append(initialization)

    def __addInitKnowledge(self, initKnowledge: str) -> None:
        """
        adds initial knowledge
        """
        if initKnowledge not in self.agent_initKnowledge:
            self.agent_initKnowledge.append(initKnowledge)

    def __constructInitialKnowledge(self) -> None:
        """
        construct the initial knowledge graph
        """
        prefix = "\n".join(self.prefixes)
        initialisation = "\n".join(self.agent_initialization)
        returnString = "\n\n".join([prefix, initialisation])
        if self.agent_initKnowledge:
            initKnowledge = "\n\n".join(self.agent_initKnowledge)
            returnString = "\n\n".join([returnString, initKnowledge])
        self.initialKnowledge = returnString

    def addInitKnowledge(self, prefix: str, subject: str, obj: str, properties: dict = None,
                         prefix_subject: bool = False, end=False) -> None:
        """
        add the given knowledge to the list of knowledge and uses later for constructing initial knowledge graph
        """
        subject = subject if not prefix_subject else f'{prefix}:{subject}'
        self.__addInitialization(f'				ajan:agentInitKnowledge {subject} ;' if not end else f'				ajan:agentInitKnowledge {subject} .')
        string = f'{subject} a {prefix}:{obj}'
        if properties is not None:
            for key in properties:
                string += ';\n '
                if isinstance(properties[key], str) or isinstance(properties[key], int) \
                        or isinstance(properties[key], float):
                    string += f' {prefix}:{key} "{properties[key]}"'
                else:
                    if isinstance(properties[key], dict):
                        if properties[key]['exclude_value_as_string']:
                            string += f' {prefix}:{key} {properties[key]["value"]}'
        string += ' .'
        self.__addInitKnowledge(string)

    def add_prefix(self, prefix: str):
        """
        adds the given prefix to the list of prefixes and uses later for constructing initial knowledge graph
        """
        self.prefixes.append(prefix)

    def create_agent(self):
        """
        created the initial knowledge graph with current prefixes, subjects and initial knowledge data
        """
        self.__constructInitialKnowledge()
        return requests.post(self.URL, data=self.initialKnowledge, headers=self.__headers)

    def execute_agent(self):
        """
        executes the agent
        """
        requestUrl = self.URL + str(self.agent_id) + "?capability=execute"
        return requests.post(requestUrl, headers=self.__headers)

    def delete_agent(self):
        requests.delete(self.URL + str(self.agent_id), headers=self.__headers)
