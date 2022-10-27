import AJANlib
import rdflib
import sys
import inspect
import importlib


def makemodule(code):
    codeobj = compile(code, 'DynLeafNode', 'exec')
    module_spec = importlib.machinery.ModuleSpec('DynLeafNode', None)
    newmodule = importlib.util.module_from_spec(module_spec)
    exec(codeobj, newmodule.__dict__)
    return newmodule

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    codeInString = str(sys.argv[1])
    rdf_input = str(sys.argv[2])

    """codeInString = 'import AJANlib\n' \
                   'import rdflib\n' \
                   'class MyNode(AJANlib.LeafNode):\n' \
                   '    def executeLeafNode(self, input: rdflib.Graph):\n' \
                   '        return AJANlib.NodeResult(AJANlib.Status.SUCCEEDED, "myNode (SUCCEEDED)")\n' \
                   '    def getClassName(self):\n' \
                   '        return "MyNode"\n'"""

    try:
        dynNode = makemodule(codeInString)
    except:
        print("ERROR")
        print("Source cannot be imported as Module!")
        sys.exit()

    try:
        for name, obj in inspect.getmembers(dynNode):
            if inspect.isclass(obj):
                if issubclass(obj, AJANlib.LeafNode):
                    ownNode = obj()
                    print(ownNode.getClassName())
                    result = ownNode.executeLeafNode(input=rdflib.Graph().parse(data=rdf_input))
                    print(result.status)
                    print(result.label)
                    print('RDF--------')
                    print(result.rdf_output.serialize(format="turtle"))
                    print('--------RDF')
                    sys.exit()
                    
        print("ERROR")
        print("Source not type of AJANlib.LeafNode!")
        sys.exit()

    except:
        print("ERROR")
        print("Problem executing source!")
        sys.exit()

    print("ERROR")
    print("No LeafNode Class defined!")
    sys.exit()