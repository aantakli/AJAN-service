import time

import requests, uuid

from MMIPython.core.utils.thrift_client import ThriftClient
from MMIStandard.core.ttypes import *
from MMIStandard.math.ttypes import *
from MMIStandard.cosim import MCoSimulationAccess
from MMIStandard.services import MSkeletonAccess, MSceneAccess
from MMIStandard.register import MMIRegisterService
from MMIStandard.mmu.ttypes import *
from MMIStandard.constraints.ttypes import *
from MMIStandard.cosim import MCoSimulationEventCallback
from MMIStandard.avatar.ttypes import *

from MMIPython.extensions.MQuaternionExtensions import *
from MMIPython.extensions.MVector3Extensions import *

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TCompactProtocol
from thrift.server import TServer

from MosimAjanDemo import MosimAjanAgent


def getAgentTemplate():
    UE_RandomWalk_Agent = "http://localhost:8090/rdf4j/repositories/agents#AG_635d613b-a6bf-4209-b98f-eb3de2a3ecb8"
    UE_Walk_Agent = "http://localhost:8090/rdf4j/repositories/agents#AG_6720c572-838b-4aff-b4fc-69972df2fedd"
    UE_WeightedWalk_Agent = "http://localhost:8090/rdf4j/repositories/agents#AG_aead87a2-94bd-479a-8be7-bbcca669d380"
    return UE_RandomWalk_Agent


def getInitialKnowledge(ajan_id, agentTemplate, avatar_id, scene_access_port, skeleton_access_port, RegistryPort,
                        cosim_access_port):
    # addInitialization('				ajan:agentInitKnowledge <127.0.0.1:9000/avatars/Test> ;')
    # addInitialization('				ajan:agentInitKnowledge mosim:Scene_1 ;')
    # addInitialization('				ajan:agentInitKnowledge mosim:SkeletonAccess_1 ;')
    # addInitialization('				ajan:agentInitKnowledge mosim:Registry_1 ;')
    # addInitialization('				ajan:agentInitKnowledge mosim:CoSimulator_1 .')
    # addInitKnowledge('<127.0.0.1:9000/avatars/Test> a mosim:Avatar;\n'
    #                  '  mosim:id "a632e2c6-a37b-4ee2-8f3f-4676e44d8a75";\n'
    #                  '  mosim:clPort "8083";\n'
    #                  f'  mosim:avatarID "{avatar_id}";\n'
    #                  '  mosim:transform "X = 5.900000, Y = 1.053885, Z = 14.800000";\n'
    #                  '  mosim:isLocatedAt mosim:InitPosition .')
    # addInitKnowledge('mosim:Scene_1 a mosim:Scene;\n'
    #                  '  mosim:host "127.0.0.1";\n'
    #                  f'  mosim:port "{scene_access_port}" .')
    # addInitKnowledge('mosim:SkeletonAccess_1 a mosim:SkeletonAccess;\n'
    #                  '  mosim:host "127.0.0.1";\n'
    #                  f'  mosim:port "{skeleton_access_port}" .')
    # addInitKnowledge('mosim:Registry_1 a mosim:Registry;\n'
    #                  '  mosim:host "127.0.0.1";\n'
    #                  f'  mosim:port "{RegistryPort}" .')
    # addInitKnowledge('mosim:CoSimulator_1 a mosim:CoSimulator;\n'
    #                  '  mosim:host "127.0.0.1";\n'
    #                  f'  mosim:port "{cosim_access_port}" .')
    pass


MMI_REGISTRY_PORT = 9009
COSIM_ACCESS_PORT = -1
SKELETON_ACCESS_PORT = -1
SCENE_ACCESS_PORT = -1


def SetupMOSIMConnection():
    """

    Setup function to connect to the MOSIM framework.

    The MMI_REGISTRY_PORT is required, which is the port on which the MMI Launcher was started (usually 9009).

    Raises:
        Exception: Exception, in case the different services cannot be found. In this case, check the launcher, whether the services area actually running.

    Returns:
        (CoSimulationAccess, SkeletonAccess, SceneAccess): Thrift Clients for the different services.
    """
    global COSIM_ACCESS_PORT, SKELETON_ACCESS_PORT, SCENE_ACCESS_PORT
    RegisterClient = ThriftClient("127.0.0.1", MMI_REGISTRY_PORT, MMIRegisterService.Client)
    RegisterClient.__enter__()

    session_id = RegisterClient._access.CreateSessionID({})
    services = RegisterClient._access.GetRegisteredServices(session_id)

    for i in range(len(services) - 1, -1, -1):
        # iterate negatively to prefer later added services.
        s = services[i]
        if s.Name == "coSimulationAccess":
            COSIM_ACCESS_PORT = s.Addresses[0].Port
        if s.Name == 'Standalone Skeleton Access':
            SKELETON_ACCESS_PORT = s.Addresses[0].Port
        if s.Name == 'remoteSceneAccessUnreal':
            SCENE_ACCESS_PORT = s.Addresses[0].Port

    if COSIM_ACCESS_PORT < 0:
        raise Exception("Error, could not find the coSimulationAccess service. Please check your MMI Environment.")

    if SKELETON_ACCESS_PORT < 0:
        raise Exception(
            "Error, could not find the Standalone Skeleton Access service. Please check your MMI Environment.")

    CoSimClient = ThriftClient("127.0.0.1", COSIM_ACCESS_PORT, MCoSimulationAccess.Client)
    CoSimClient.__enter__()

    skeletonAccess = ThriftClient("127.0.0.1", SKELETON_ACCESS_PORT, MSkeletonAccess.Client)
    skeletonAccess.__enter__()

    sceneAccess = ThriftClient("127.0.0.1", SCENE_ACCESS_PORT, MSceneAccess.Client)
    sceneAccess.__enter__()

    return CoSimClient, skeletonAccess, sceneAccess


if __name__ == '__main__':
    avatarID = "316fe9ca-3c40-4f8b-9e05-cf628a55f33d"
    agent_id = uuid.uuid4()
    CoSimClient, skeletonAccess, sceneAccess = SetupMOSIMConnection()
    # CreateAgent(avatarID, SCENE_ACCESS_PORT, SKELETON_ACCESS_PORT, 9009, COSIM_ACCESS_PORT, ajanID)
    # getInitialKnowledge(agent_id, getAgentTemplate(), avatarID, SCENE_ACCESS_PORT, SKELETON_ACCESS_PORT,
    #                     9009, COSIM_ACCESS_PORT)
    ajan_agent = MosimAjanAgent(getAgentTemplate(), avatarID, sceneAccess, skeletonAccess, 9009, CoSimClient)
    ajan_agent.create_agent()
    # print(ExecuteAgent(ajanID))
    # time.sleep(20)
    # print(DeleteAgent(ajanID))
