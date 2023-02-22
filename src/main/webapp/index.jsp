<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1>Which properties and classes the RDF datasets have in common?</h1>
	<h4>SPARQL endpoint, HDT file, RDF dump file, Ontology, etc...All you need is the URL of your datasets</h4>
	
	<form action="SimilarityServlet">
		<fieldset style="border: none">
			<label for="fname">Datasets</label>
			<textarea id="datasets" name="datasets" rows="4" cols="80">
			https://github.com/Mat-O-Lab/OntoSimMat/raw/main/build/classes/com/relod/servlet/ontoConcreteSource.owl, 
			https://github.com/Mat-O-Lab/OntoSimMat/raw/main/build/classes/com/relod/servlet/pmdco_core.ttl
			</textarea>
			<br/>
			<input type="radio" id="exact" name="opt" value="exact"/> Exact Matches
			<br/>
			<input type="radio" id="dsim" name="opt" value="dsim"/> Similar Matches
			<input type="text" id="simlevel" name="simlevel" value="0.9" /> Similarity level (0.0-1.0) - We recommend a DOMAIN EXPERT to review.
			<br/>
			<input type="checkbox" id="rdf" name="rdf" value="rdf"/> Results in RDF - Generate owl:sameAs and skos:exactMatch
			<br> <br> <input type="submit" value="Submit">
		</fieldset>
	</form>
</body>
</html>
