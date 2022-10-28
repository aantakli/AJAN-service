import AJANlib
from rdflib import Graph, URIRef, BNode, Literal
from rdflib.namespace import RDF, XSD

class MyNode(AJANlib.LeafNode):
	def executeLeafNode(self, input: Graph):
		output = Graph()
		agent = URIRef('http://www.ajan.de/ajan-ns#Agent')
		label = URIRef('http://www.ajan.de/python-ns#label')
		
		for agent in input.subjects(RDF.type, agent):
			output.add((agent, label, Literal('is an Python Agent', datatype=XSD.string)))
		
		return AJANlib.NodeResult(status=AJANlib.Status.SUCCEEDED, label='added statements!', rdf_output=output)

	def getClassName(self):
		return 'MyNode'