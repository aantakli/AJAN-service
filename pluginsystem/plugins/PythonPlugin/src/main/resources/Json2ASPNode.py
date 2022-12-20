import json
import AJANlib
from rdflib import Graph, URIRef, BNode, Literal

# Node logic to convert ./testFile.json into AIToC specific ASP rules
class Json2ASPNode(AJANlib.LeafNode):

    asp_type_predicates = {
          'Smooth through hole': 'smooth_through_hole'
        }

    def readProperty(self, rule_set, inst, prop):
        for key in prop:
            predicate = 'has_{}'.format(key.lower())
            rule_set.append(predicate + '(' + inst + ',\\x22' + prop[key] + '\\x22).')

    def readPartProperty(self, rule_set, properties):
        index = properties['ID'].lower()
        subsystem = properties['Subsystem'].lower()
        rule_set.append(subsystem + '({}).'.format(index))
        self.readProperty(rule_set, index, properties)
        return index

    def setTypePredicate(self, asp_rules, index, keyword, component_instance):
        asp_predicate = self.asp_type_predicates.get(keyword)
        instance = asp_predicate + '_{}'.format(index)
        asp_rules.append('has_' + asp_predicate + '(' + component_instance + ',' + instance + ').')
        asp_instance = asp_predicate + '({}).'.format(instance)
        asp_rules.append(asp_instance)
        return instance

    def getASP(self, json_txt):
        fixedQuotes = json_txt.replace('\\x27', '\\x22')
        print(fixedQuotes)
        annotation = json.loads(fixedQuotes)
        asp_rules = []
        component_instance = ''
        index = 1

        for item in annotation['Constraints']:
            if item['ID'] == 'PartProperties':
                if item['Properties']:
                    component_instance = self.readPartProperty(asp_rules, item['Properties'])
            if item['ID'] in self.asp_type_predicates.keys():
                instance = self.setTypePredicate(asp_rules, index, item['ID'], component_instance)
                if item['Properties']:
                    self.readProperty(asp_rules, instance, item['Properties'])
                index = index + 1
        return asp_rules

    def executeLeafNode(self, input: Graph):
        output = Graph()
        for s, p, o in input:
            if p == URIRef('http://www.ajan.de/ajan-mapping-ns#jsonString'):
                resulting_rules = ' '.join(map(str, self.getASP(o)))
                output.add((BNode(), URIRef('http://www.ajan.de/behavior/asp-ns#asRules'), Literal(resulting_rules)))
                print(resulting_rules)

        return AJANlib.NodeResult(status=AJANlib.Status.SUCCEEDED, label='Converted JSON to ASP!', rdf_output=output)

    def getClassName(self):
        return 'Json2ASPNode'
