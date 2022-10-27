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

    dynNode = makemodule(codeInString)

    for name, obj in inspect.getmembers(dynNode):
        if inspect.isclass(obj):
            ownNode = obj()
            if isinstance(ownNode, AJANlib.LeafNode):
                result = ownNode.executeLeafNode(input=rdflib.Graph().parse(data=rdf_input))
                print(result.label)
                sys.exit()

    print("ERROR: No LeafNode Class defined!")
    sys.exit()