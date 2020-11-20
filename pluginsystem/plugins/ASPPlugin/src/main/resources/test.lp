_t(_b("genid-8384786bef434adaa27efeaea3bbc047-blank1"),"http://www.dfki.de/inversiv-ns#ist","http://www.dfki.de/inversiv-ns#Schaf").
_t(_b("genid-8384786bef434adaa27efeaea3bbc047-blank2"),"http://www.dfki.de/inversiv-ns#ist","http://www.dfki.de/inversiv-ns#Schaf").
_t("http://localhost:8080/ajan/agents/Angela","http://www.dfki.de/inversiv-ns#sagt","http://www.dfki.de/inversiv-ns#Hallo").
_t("http://localhost:8080/ajan/agents/Angela","http://www.dfki.de/inversiv-ns#sagt",_l("5.5","http://www.w3.org/2001/XMLSchema#decimal")).
_t("http://localhost:8080/ajan/agents/Angela","http://www.dfki.de/inversiv-ns#sagt",_l("true","http://www.w3.org/2001/XMLSchema#boolean")).
_t("http://localhost:8080/ajan/agents/Angela","http://www.dfki.de/inversiv-ns#sagt",_l("1519644455503","http://www.w3.org/2001/XMLSchema#string")).
_t("http://www.dfki.de/inversiv-ns#Erdogan","http://www.dfki.de/inversiv-ns#liebt",_b("genid-8384786bef434adaa27efeaea3bbc047-blank1")).

xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#string" .
xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#boolean" .
xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#decimal" .
xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#float" .
xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#double" .
xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#short" .
xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#integer" .
xsd(_l(L,T)) :- _t(_,_,_l(L,T)), T=="http://www.w3.org/2001/XMLSchema#long" .

_t(P,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#Property") :- _t(S,P,O) .
_t(_l(L,T),"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#XMLLiteral") :- _t(S,P,_l(L,T)), xsd(_l(L,T)) .
:- _t(_l(L,T),"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#XMLLiteral"), not xsd(_l(L,T)) .

literal(_l(L,T)) :- _t(_,_,_l(L,T)) .
literal(_l(L,T)) :- xsd(_l(L,T)) .

_t(_l(L,T),"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#Literal") :- _t(S,P,_l(L,T)), literal(_l(L,T)) .
:- _t(_l(L,T),"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#Literal"), not literal(_l(L,T)) .

_t(U,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",X) :- _t(A,"http://www.w3.org/2000/01/rdf-schema#domain",X), _t(U,A,V) .
_t(V,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",X) :- _t(A,"http://www.w3.org/2000/01/rdf-schema#range",X), _t(U,A,V) .
_t(X,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",B) :- _t(A,"http://www.w3.org/2000/01/rdf-schema#domain",B), _t(X,C,Y), _t(C,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",B) .
_t(Y,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",B) :- _t(A,"http://www.w3.org/2000/01/rdf-schema#range",B), _t(X,C,Y), _t(C,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",B) .
_t(U,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#Resource") :- _t(U,A,X) .
_t(V,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#Resource") :- _t(U,A,V) .
_t(U,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",X) :- _t(U,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",V), _t(V,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",X) .
_t(U,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",U) :- _t(U,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#Property") .
_t(U,B,Y) :- _t(A,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",B), _t(U,A,Y) .
_t(U,"http://www.w3.org/2000/01/rdf-schema#subClassOf","http://www.w3.org/2000/01/rdf-schema#Resource") :- _t(U,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#Class") .
_t(V,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",X) :- _t(U,"http://www.w3.org/2000/01/rdf-schema#subClassOf",X), _t(V,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",U) .
_t(U,"http://www.w3.org/2000/01/rdf-schema#subClassOf",U) :- _t(U,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#Class") .
_t(U,"http://www.w3.org/2000/01/rdf-schema#subClassOf",X) :- _t(U,"http://www.w3.org/2000/01/rdf-schema#subClassOf",V), _t(V,"http://www.w3.org/2000/01/rdf-schema#subClassOf",X) .
_t(U,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf","http://www.w3.org/2000/01/rdf-schema#member") :- _t(U,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#cmp") .
_t(U,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf","http://www.w3.org/2000/01/rdf-schema#Literal") :- _t(U,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/2000/01/rdf-schema#Datatype") .
