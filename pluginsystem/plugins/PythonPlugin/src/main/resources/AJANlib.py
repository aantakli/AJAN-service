import rdflib
from enum import Enum

class Status(Enum):
   FRESH = "FRESH"
   RUNNING = "RUNNING"
   SUCCEEDED = "SUCCEEDED"
   FAILED = "FAILED"

class NodeResult:
   status = Status.FRESH
   label = "My NodeStatus is fresh"
   rdf_output = rdflib.Graph()

   def __init__(self, status: Status, label: str, rdf_output: rdflib.Graph):
      self.status = status
      self.label = label
      self.rdf_output = rdf_output


class LeafNode:
   def executeLeafNode(self, input: rdflib.Graph) -> NodeResult:
      """This Method is executed by AJAN itself to get python based NodeResult"""
      pass

   def getClassName(self) -> str:
      """return name of own class"""
      pass