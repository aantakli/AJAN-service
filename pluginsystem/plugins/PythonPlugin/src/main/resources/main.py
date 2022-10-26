# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

import AJANlib
import rdflib
import sys
import inspect

import myNode

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    rdf_input = str(sys.argv[1])
    for name, obj in inspect.getmembers(myNode):
        if inspect.isclass(obj):
            ownNode = obj()
            if isinstance(ownNode, AJANlib.LeafNode):
                result = ownNode.executeLeafNode(input=rdflib.Graph().parse(data=rdf_input))
                print(result.label)
                sys.exit()

    print("ERROR: No LeafNode Class defined!")
    sys.exit()
