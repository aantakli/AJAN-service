
import AJANlib
import rdflib


class MyNode(AJANlib.LeafNode):
    def executeLeafNode(self, input: rdflib.Graph):
        return AJANlib.NodeResult(AJANlib.Status.SUCCEEDED, "myNode (SUCCEEDED)")

    def getClassName(self):
        return "MyNode"