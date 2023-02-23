# OntoSimMat
RDF Ontology and Dataset matching for Material Sciences domain

![ontologyMatching](https://user-images.githubusercontent.com/9248325/221026245-85526659-4463-4934-aaa2-3d3a3d1741dd.png)


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
- Choose two URIs from RDF Ontologies and/or Datasets that you want to match and click on Submit button.
![image](https://user-images.githubusercontent.com/9248325/220661737-71fc47f3-2965-4fa5-b8da-4eb608fbb823.png)

## Exact Matches 
Exact Matches gives you all properties and classes which are EXACT the same in both Ontology/Datasets. As following picture illustrates.
![image](https://user-images.githubusercontent.com/9248325/220660020-a4240132-04be-4269-ba1a-082967216bd1.png)

## Similar Matches
Similar Matches return the properties and classes with certain level of similarity from 0.0 to 1.0. As following picture illustrates for a similiarity level of 0.8.
![image](https://user-images.githubusercontent.com/9248325/220660472-30cddbff-6fad-4efe-a774-84fc27fef001.png)
The similar matches should be verified/validated by a Domain expert.

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

# As developer
The repository is LITERALLY an eclipse IDE project ready to use. Ofc, you can use your prefered java IDE.

Details about the implementation algorithm in this paper:
https://www.semantic-web-journal.net/system/files/swj2457.pdf
