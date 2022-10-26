# Python Module example

import rdflib
from enum import Enum

class Status(Enum):
   FRESH = 0
   RUNNING = 1
   SUCCEEDED = 2
   FAILED = 3

class NodeResult:
   status = Status.FRESH
   label = "My NodeStatus is fresh"
   rdf_output = ""

   def __init__(self, status: Status, label: str):
      self.status = status
      self.label = label


class LeafNode:
   def executeLeafNode(self, input: rdflib.Graph) -> NodeResult:
      """This Method is executed by AJAN itself to get python based NodeResult"""
      pass

   def getClassName(self) -> str:
      """return name of own class"""
      pass