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
            "isLocatedAt": {"exclude_value_as_string":True,
                            "value": 'mosim:InitPosition'},
        })
        self.addInitKnowledge('Scene_1', 'Scene', {
            "host": '127.0.0.1',
            "port": scene_access_port
        }, prefix_subject=True)
        self.addInitKnowledge('SkeletonAccess_1', 'SkeletonAccess', {
            "host": '127.0.0.1',
            "port": skeleton_access_port
        }, prefix_subject=True)
        self.addInitKnowledge('Registry_1', 'Registry', {
            "host": '127.0.0.1',
            "port": RegistryPort
        }, prefix_subject=True)
        self.addInitKnowledge('CoSimulator_1', 'CoSimulator', {
            "host": '127.0.0.1',
            "port": cosim_access_port
        }, prefix_subject=True, end=True)

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

    def addInitKnowledge(self, subject, obj, properties=None, prefix='mosim', prefix_subject=False, end=False):
        """
        add the given knowledge to the list of knowledge and uses later for constructing initial knowledge graph
        """
        self.ajan_agent.addInitKnowledge(prefix, subject, obj, properties, prefix_subject, end)

    def addPrefix(self, prefix):
        """
        adds the given prefix to the list of prefixes and uses later for constructing initial knowledge graph
        """
        self.ajan_agent.add_prefix(prefix)

    def delete_agent(self):
        self.ajan_agent.delete_agent()
