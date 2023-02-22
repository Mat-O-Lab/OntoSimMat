# OntoSimMat
RDF Ontology and Dataset matching for Material Sciences domain

Requirements:
- Java JRE (> 1.8) and environment variables setup.
- A servlet container, e.g, [Apache Tomcat®](https://tomcat.apache.org/)

# How to deploy the app with TomCat:
Copy https://github.com/Mat-O-Lab/OntoSimMat/blob/main/target/OntoSimMat.war into ```<tomcat dir>/webapp/OntoSimMat.war```

Copy [Gson](https://jar-download.com/artifacts/com.google.code.gson/gson/2.8.2/source-code) and [rdf4j](https://rdf4j.org/download/) libraries into ```<tomcat dir>/lib/```

start tomCat:
```bash
<tomcat dir>/bin/startup.bat
```

Your app will be available at http://localhost:8080/OntoSimMat

# Example of use:



# Works as command-line API:
```bash
curl http://localhost:8080/OntoSimMat/SimilarityServlet?datasets=%09%09%09https%3A%2F%2Fgithub.com%2FMat-O-Lab%2FOntoSimMat%2Fraw%2Fmain%2Fbuild%2Fclasses%2Fcom%2Frelod%2Fservlet%2FontoConcreteSource.owl%2C+%0D%0A%09%09%09https%3A%2F%2Fgithub.com%2FMat-O-Lab%2FOntoSimMat%2Fraw%2Fmain%2Fbuild%2Fclasses%2Fcom%2Frelod%2Fservlet%2Fpmdco_core.ttl%0D%0A%09%09%09&opt=exact&simlevel=0.7&rdf=rdf
```
This example should return RDF triples containing the exact match RDF mapping.
such as:
```
<http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2000/01/rdf-schema#subClassOf> .
<http://www.w3.org/2002/07/owl#Class> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2002/07/owl#Class> .
<http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> .
<http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> .
<http://www.w3.org/2000/01/rdf-schema#range> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2000/01/rdf-schema#range> .
<http://www.w3.org/2000/01/rdf-schema#comment> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2000/01/rdf-schema#comment> .
<http://www.w3.org/2000/01/rdf-schema#label> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2000/01/rdf-schema#label> .
<http://www.w3.org/2002/07/owl#DatatypeProperty> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2002/07/owl#DatatypeProperty> .
<http://www.w3.org/2002/07/owl#ObjectProperty> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2002/07/owl#ObjectProperty> .
<http://www.w3.org/2000/01/rdf-schema#domain> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2000/01/rdf-schema#domain> .
<http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> .
<http://www.w3.org/2002/07/owl#Ontology> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/2002/07/owl#Ontology> .
<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#exactMatch> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> .
```



Details about the implementation algorithm in this paper:
https://www.semantic-web-journal.net/system/files/swj2457.pdf
