import uuid

from AjanAgent import AjanAgent


class MosimAjanAgent:
    """
    MOSIM Wrapper for the base Ajan agent
    """
    ajan_agent = None
    agent_id = None

    def __init__(self, agent_template, avatar_id, scene_access_port, skeleton_access_port, RegistryPort,
                            cosim_access_port, url=None, headers=None):
        self.agent_id = uuid.uuid4()
        self.ajan_agent = AjanAgent(self.agent_id, agent_template, url, headers)
        self.ajan_agent.add_prefix('@prefix mosim: <http://www.dfki.de/mosim-ns#> .')
        self.addInitKnowledge('<127.0.0.1:9000/avatars/Test>', 'Avatar', {
            "id": 'a632e2c6-a37b-4ee2-8f3f-4676e44d8a75',
            "clPort": '8083',
            "avatarID": avatar_id,
            "transform": 'X = 5.900000, Y = 1.053885, Z = 14.800000',
            "isLocatedAt": 'mosim:InitPosition'
        })
        self.addInitKnowledge('Scene_1', 'Scene', {
            "host": '127.0.0.1',
            "port": scene_access_port
        })
        self.addInitKnowledge('SkeletonAccess_1', 'SkeletonAccess', {
            "host": '127.0.0.1',
            "port": skeleton_access_port
        })
        self.addInitKnowledge('Registry_1', 'Registry', {
            "host": '127.0.0.1',
            "port": RegistryPort
        })
        self.addInitKnowledge('CoSimulator_1', 'CoSimulator', {
            "host": '127.0.0.1',
            "port": cosim_access_port
        })

    def create_agent(self):
        """
        creates the agent
        """
        self.ajan_agent.create_agent()

    def execute_agent(self):
        """
        execute the agent
        """
        self.ajan_agent.execute_agent()

    def addInitKnowledge(self, subject, obj, properties=None, prefix='mosim'):
        """
        add the given knowledge to the list of knowledge and uses later for constructing initial knowledge graph
        """
        self.ajan_agent.addInitKnowledge(prefix, subject, obj, properties)

    def addPrefix(self, prefix):
        """
        adds the given prefix to the list of prefixes and uses later for constructing initial knowledge graph
        """
        self.ajan_agent.add_prefix(prefix)
