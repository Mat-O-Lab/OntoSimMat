package com.relod.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.eclipse.rdf4j.common.app.logging.LogConfiguration;
import org.eclipse.rdf4j.common.app.logging.base.AbstractLogConfiguration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SimilarityServlet
 */
@WebServlet("/SimilarityServlet")
public class GenericSparqlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public GenericSparqlServlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		boolean bRdf = (request.getParameter("rdf") != null);
		if (request.getParameter("dataset") != null) {
			Map<String, Set<String>> ret = new HashMap<String, Set<String>>();
			String endPoint = request.getParameter("dataset");
			String query = request.getParameter("query");
			ret.put(endPoint, execSparql(query, endPoint));
			String outStr = null;
			if (bRdf) {
				outStr = convRDF((HashMap) ret, 1.0);
			} else {
				outStr = convJSON((HashMap) ret);
			}
			response.getWriter().append(outStr);
		} else if ((request.getParameter("opt") != null) && (request.getParameter("opt").equals("exact"))) {
			Map<String, Set<String>> ret = null;
			String datasets = request.getParameter("datasets");
			String str[] = datasets.split(",");
			if (str.length > 1) {
				Set<String> setDs = new LinkedHashSet<String>();
				for (String p : str) {
					setDs.add(p.trim());
				}
				try {
					ret = generateDatasetSimilarity(setDs);
					String outStr = null;
					if (bRdf) {
						outStr = convRDF((HashMap) ret, 1.0);
					} else {
						outStr = convJSON((HashMap) ret);
					}
					response.getWriter().append(outStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if ((request.getParameter("opt") != null) && (request.getParameter("opt").equals("dsim"))) {
			Map<String, Map<String, String>> ret = null;
			String datasets = request.getParameter("datasets");
			double simLevel = Double.parseDouble(request.getParameter("simlevel"));
			String str[] = datasets.split(",");
			if (str.length > 1) {
				Set<String> setDs = new LinkedHashSet<String>();
				for (String p : str) {
					setDs.add(p.trim());
				}
				try {
					ret = generateDatasetSimilarity(setDs, true, simLevel);
					String outStr = null;
					if (bRdf) {
						outStr = convRDF((HashMap) ret, simLevel);
					} else {
						outStr = convJSON((HashMap) ret);
					}
					response.getWriter().append(outStr);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static IRI makeValidIRI(String iri) {
		URI uri = null;
		try {
			URL url = new URL(iri);
			String nullFragment = null;
			uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
			System.out.println("URI " + uri.toString() + " is OK");
		} catch (MalformedURLException e) {
			System.out.println("URL " + iri + " is a malformed URL");
		} catch (URISyntaxException e) {
			System.out.println("URI " + iri + " is a malformed URL");
		}
		return Values.iri(uri.toString());
	}

	private String convRDF(HashMap map, double simLevel) throws IOException {
		if (map.toString().contains("---")) { // similar
			return generateMappingRDF(map, "http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_sameAs", simLevel);
		}
		return generateMappingRDFExact(map, "http://www.w3.org/2004/02/skos/core#exactMatch");
	}

	private String convJSON(HashMap ret) {
		Gson gson = new Gson();
		Type gsonType = new TypeToken<HashMap>() {
		}.getType();
		return gson.toJson(ret, gsonType);
	}

	/*
	 * Static version of convJSON()
	 */
	private static String convJSON2(HashMap ret) {
		Gson gson = new Gson();
		Type gsonType = new TypeToken<HashMap>() {
		}.getType();
		return gson.toJson(ret, gsonType);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public static Map<String, Set<String>> generateDatasetSimilarity(Set<String> datasets) {
		Map<String, Set<String>> mapExactMatch = new LinkedHashMap<String, Set<String>>();
		String[] array = datasets.stream().toArray(String[]::new);
		for (int i = 0; i < array.length; i++) {
			for (int j = i; j < array.length; j++) {
				try {
					if (array[i].equalsIgnoreCase(array[j]))
						continue;
					mapExactMatch.putAll(getExactMatches(array[i], array[j]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		if (datasets.size() > 1) {
			Map<String, Set<String>> mRet = getOnlyMatchProps(datasets, mapExactMatch);

			String sRet[] = mRet.values().toArray()[0].toString().split(",");
			Set<String> setRet = new LinkedHashSet<String>();
			for (int i = 0; i < sRet.length; i++) {
				// Set to remove duplicates
				setRet.add(sRet[i].trim().replaceAll("s\\u003d", ""));
			}
			System.out.println("Number of matches: " + setRet.size());
			Map<String, Set<String>> myRet = new LinkedHashMap<String, Set<String>>();
			myRet.put(datasets.toString(), setRet);
			return myRet;
		}
		return mapExactMatch;
	}

	public static Map<String, Map<String, String>> generateDatasetSimilarity(Set<String> datasets, boolean bSimilar,
			double simLevel) {
		Map<String, Set<String>> mapExactMatch = new LinkedHashMap<String, Set<String>>();
		Map<String, Map<String, String>> mapSim = new LinkedHashMap<String, Map<String, String>>();
		String[] array = datasets.stream().toArray(String[]::new);
		for (int i = 0; i < array.length; i++) {
			for (int j = i; j < array.length; j++) {
				try {
					if (array[i].equalsIgnoreCase(array[j]))
						continue;
					mapExactMatch.putAll(getExactMatches(array[i], array[j]));
					if (bSimilar) {
						mapSim.putAll(getSimMatches(array[i], array[j], simLevel, mapExactMatch));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return mapSim;
	}

	private static Map<String, Set<String>> getOnlyMatchProps(Set<String> datasets,
			Map<String, Set<String>> mapExactMatch) {
		Map<String, Set<String>> ret = new LinkedHashMap<String, Set<String>>();
		ret.put(datasets.toString(), new HashSet<String>());
		Set<String> sRetain = new HashSet<String>();
		for (Map.Entry<String, Set<String>> entry : mapExactMatch.entrySet()) {
			sRetain.addAll(entry.getValue());
			sRetain.retainAll(entry.getValue());
//			for(String s: entry.getValue()) {
//				if(!sRetain.add(s)) {
//					ret.get(datasets.toString()).add(s);
//				}
//			}
		}
		ret.get(datasets.toString()).addAll(sRetain);
		return ret;
	}

	private static Map<String, Set<String>> getExactMatches(String source, String target)
			throws FileNotFoundException, UnsupportedEncodingException {
		Set<String> propsSource = null;
		Set<String> propsTarget = null;
		final Set<String> propsMatched = new LinkedHashSet<String>();
		final Map<String, Set<String>> mapExactMatch = new LinkedHashMap<String, Set<String>>();
		String s[] = source.split("/");
		String fSource = null;
		String fTarget = null;
		if (s.length > 2) {
			fSource = s[2] + "_" + s[s.length - 1];
		} else {
			fSource = s[s.length - 1];
		}
		String t[] = target.split("/");
		if (t.length > 2) {
			fTarget = t[2] + "_" + t[t.length - 1];
		} else {
			fTarget = t[t.length - 1];
		}
		// final String fileName = OUTPUT_DIR + "/" + fSource + "---" + fTarget +
		// "_Exact.txt";
		final String fileName = fSource + "---" + fTarget;
		propsSource = getProps(source);
		propsTarget = getProps(target);
		if ((propsSource.size() < 1) || (propsTarget.size() < 1)) {
			return mapExactMatch;
		}

		for (String pSource : propsSource) {
			// propsSource.parallelStream().forEach(pSource -> {
			// propsTarget.parallelStream().forEach(pTarget -> {
			for (String pTarget : propsTarget) {
				if (pSource.trim().equalsIgnoreCase(pTarget.trim())) {
					propsMatched.add(getPropLabel(pSource, source));
				}
				// });
			}
			// });
		}
		mapExactMatch.put(fileName, propsMatched);
		// writer.close();
		return mapExactMatch;
	}

	private static Map<String, Map<String, String>> getSimMatches(String source, String target, double threshold,
			Map<String, Set<String>> mapExactMatch) throws FileNotFoundException, UnsupportedEncodingException {
		final Set<String> propsSource = new LinkedHashSet<String>();
		final Set<String> propsTarget = new LinkedHashSet<String>();
		final Map<String, String> propsMatched = new LinkedHashMap<String, String>();
		final Map<String, Map<String, String>> mapSim = new LinkedHashMap<String, Map<String, String>>();

		String s[] = source.split("/");
		String fSource = null;
		String fTarget = null;
		if (s.length > 2) {
			fSource = s[2] + "_" + s[s.length - 1];
		} else {
			fSource = s[s.length - 1];
		}
		String t[] = target.split("/");
		if (t.length > 2) {
			fTarget = t[2] + "_" + t[t.length - 1];
		} else {
			fTarget = t[t.length - 1];
		}
		// final String fileName = OUTPUT_DIR + "/" + fSource + "---" + fTarget +
		// "_Sim.tsv";
		final String fileName = fSource + "---" + fTarget + "_Sim.tsv";
		propsSource.addAll(getProps(source));
		propsTarget.addAll(getProps(target));

		for (Set<String> done : mapExactMatch.values()) {
			propsSource.removeAll(done);
			propsTarget.removeAll(done);
		}

		if ((propsSource.size() < 1) || (propsTarget.size() < 1)) {
			return mapSim;
		}

		// PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		JaccardSimilarity sim = new JaccardSimilarity();
		// propsSource.parallelStream().forEach(pSource -> {
		for (String pSource : propsSource) {
			// propsTarget.parallelStream().forEach(pTarget -> {
			for (String pTarget : propsTarget) {
				String p1 = getURLName(pSource);
				String p2 = getURLName(pTarget);
				double dSim = sim.apply(p1, p2);
				if (dSim >= threshold) {
					propsMatched.put(getPropLabel(pSource, source), getPropLabel(pTarget, target));
				}
				// });
			}
			// });
		}
		mapSim.put(fileName.replaceAll(".tsv", ""), propsMatched);
		// writer.close();
		return mapSim;
	}

	public static String getURLName(String property) {
		String name = null;
		try {
			if (property.indexOf("#") > 0) {
				String[] str = property.split("#");
				name = str[str.length - 1];
				return name.replaceAll("\"", "");
			} else {
				String[] str = property.split("/");
				name = str[str.length - 1];
			}
		} catch (Exception e) {
			System.err.println("Problem with URI: " + property);
		}
		return name.replaceAll("\"", "");
	}

	private static Set<String> getProps(String source) {
		// Put Claus approach here...
		// instead of execute the SPARQL at the Dataset, we query the Dataset Catalog
		// from Claus to obtain a list of properties and classes.
		// This should be faster then query the dataset, because there are some
		// datasets/Endpoints extremely slow, more than 3 minutes.
		// return getPropsClaus(source)

		Set<String> ret, setAnnotation, setClasses, setDataProp, setLabels = null;
		ret = new LinkedHashSet<String>();

		String cSparqlAnnotationProperties = "Select Distinct ?p where {?s ?p ?o}";
		setAnnotation = execSparql(cSparqlAnnotationProperties, source);
		ret.addAll(setAnnotation);

		String cSparqlClasses = "select distinct ?p where {[] a ?p}";
		setClasses = execSparql(cSparqlClasses, source);
		ret.addAll(setClasses);

		String cSparqlObjDataProperties = "SELECT DISTINCT ?p WHERE {  ?p <http://www.w3.org/2000/01/rdf-schema#domain> ?o . }";
		setDataProp = execSparql(cSparqlObjDataProperties, source);
		ret.addAll(setDataProp);

		String cSparqlLabels = "SELECT DISTINCT ?p WHERE { ?prop <http://www.w3.org/2000/01/rdf-schema#label> ?p }";
		setLabels = execSparql(cSparqlLabels, source);
		ret.addAll(setLabels);

		return ret;
	}

	private static Set<String> execSparql(String cSparql, String source) {
		final Set<String> ret = new LinkedHashSet<String>();
		try {
			if (source.indexOf("sparql") > 0) {
				SPARQLRepository repo = new SPARQLRepository(source);
				RepositoryConnection conn = repo.getConnection();
				try {
					TupleQuery tQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, cSparql);
					TupleQueryResult rs = tQuery.evaluate();
					while (rs.hasNext()) {
						ret.add(rs.next().toString().trim().replaceAll("p=", "").replace("[", "").replace("]", ""));
					}
				} finally {
					conn.close();
				}
			} else {
				// ret.addAll(Util.execQueryRDFRes(cSparql, source, -1));
				String sJson = execSparqlFile(cSparql, source);
				sJson = sJson.trim().replaceAll("p=", "").replace("[", "").replace("]", "");
				ret.addAll(transformSet(sJson));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static Set<String> transformSet(String sJson) {
		Set<String> ret = new LinkedHashSet<String>();
		String s[] = sJson.split(",");
		for (int i = 0; i < s.length; i++) {
			ret.add(s[i]);
		}
		return ret;
	}

	private static String execSparqlLocalFile(String cSparql, String source, RDFFormat format) throws IOException {
		// Create a new Repository.
		Repository db = new SailRepository(new MemoryStore());

		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {
			// String filename = "example-data-artists.ttl";
			try (InputStream input = GenericSparqlServlet.class.getResourceAsStream(source)) {
				// add the RDF data from the inputstream directly to our database
				conn.add(input, "", format);
			}

			// We do a simple SPARQL SELECT-query
			TupleQuery query = conn.prepareTupleQuery(cSparql);
			String ret = "";
			// A QueryResult is also an AutoCloseable resource, so make sure it gets closed
			// when done.
			try (TupleQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				for (BindingSet solution : result) {
					// ... and print out the value of the variable binding for ?s and ?n
					// System.out.println(solution.toString());
					ret += solution.toString() + ", ";
				}
			}
			return ret;
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
	}

	private static String execSparqlFile(String cSparql, String source) throws IOException {

		RDFFormat format = guessFormat(source);
		if (!source.startsWith("http")) {
			return execSparqlLocalFile(cSparql, source, format);
		}

		// Create a new Repository.
		Repository db = new SailRepository(new MemoryStore());

		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {
			// String filename = "example-data-artists.ttl";
			URL url = new URL(source);

			try (InputStream input = url.openStream()) {
				// add the RDF data from the inputstream directly to our database
				conn.add(input, "", format);
			}

			// We do a simple SPARQL SELECT-query
			TupleQuery query = conn.prepareTupleQuery(cSparql);
			String ret = "";
			// A QueryResult is also an AutoCloseable resource, so make sure it gets closed
			// when done.
			try (TupleQueryResult result = query.evaluate()) {
				// we just iterate over all solutions in the result...
				for (BindingSet solution : result) {
					// ... and print out the value of the variable binding for ?s and ?n
					// System.out.println(solution.toString());
					ret += solution.toString() + ", ";
					;
				}
			}
			return ret;
		} finally {
			// Before our program exits, make sure the database is properly shut down.
			db.shutDown();
		}
	}

	private static RDFFormat guessFormat(String source) {
		if (source.endsWith(".ttl"))
			return RDFFormat.TURTLE;

		if (source.endsWith(".nt"))
			return RDFFormat.NTRIPLES;

		if (source.endsWith(".owl"))
			return RDFFormat.RDFXML;

		return RDFFormat.RDFXML;
	}

	public static URL getFinalURL(URL url) {
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
			con.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
			con.addRequestProperty("Referer", "https://www.google.com/");
			con.connect();
			// con.getInputStream();
			int resCode = con.getResponseCode();
			if (resCode == HttpURLConnection.HTTP_SEE_OTHER || resCode == HttpURLConnection.HTTP_MOVED_PERM
					|| resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				String Location = con.getHeaderField("Location");
				if (Location.startsWith("/")) {
					Location = url.getProtocol() + "://" + url.getHost() + Location;
				}
				return getFinalURL(new URL(Location));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return url;
	}

	public static void main(String args[]) throws IOException {
		double dSim = 0.9;
		Map<String, Set<String>> ret = null;
		Map<String, Map<String, String>> simRet = null;
		Set<String> setDs = new LinkedHashSet<String>();
		setDs.add("relodOntologySource.ttl");
		setDs.add("relodOntologyTarget.ttl");
		// setDs.add("relodOntologyTarget2.ttl");
		// setDs.add("ontoConcreteSource.owl");
		// setDs.add("pmdco_core.ttl");
		ret = generateDatasetSimilarity(setDs);
		System.out.println("**Exact Matches: " + ret);
		String rdf = generateMappingRDFExact(ret, "http://www.w3.org/2004/02/skos/core#exactMatch");
		String sFile = generateFile(rdf, "skos.exactMatch.nt", "Exact Match File: ");
		System.out.println(sFile);
		simRet = generateDatasetSimilarity(setDs, true, dSim);
		System.out.println("Similar Matches: " + simRet);
		rdf = generateMappingRDF(simRet, "http://www.w3.org/TR/2004/REC-owl-semantics-20040210/#owl_sameAs", dSim);
		sFile = generateFile(rdf, "owl.sameAs.similar.nt", "Similar Match File: ");
		System.out.println(sFile);
	}

	private static String generateFile(String rdf, String fileName, String message) throws IOException {
		File f = new File(fileName);
		if (!f.exists())
			f.createNewFile();
		PrintWriter wSim = new PrintWriter(f.getName(), "UTF-8");
		wSim.println(rdf);
		wSim.close();
		return message + f.getAbsolutePath();
	}

	private static String generateMappingRDF(Map<String, Map<String, String>> mapSim, String propMatch, double dSim)
			throws IOException {
		String rdfOut = "";
		for (Map.Entry<String, Map<String, String>> entry : mapSim.entrySet()) {
			for (Map.Entry<String, String> trip : entry.getValue().entrySet()) {
				String s = getPropLabel(trip.getKey(), entry.getKey().split("---")[0]);
				String o = getPropLabel(trip.getValue(), entry.getKey().split("---")[1].replaceAll(".ttl_Sim", ".ttl"));
				String triple = "<" + s + "> <" + propMatch + "> <" + o + "> .\n";
				rdfOut += triple;
			}
		}
		return rdfOut;
	}

	private static String getPropLabel(String label, String ds) {
		if (label.trim().startsWith("http"))
			return label;

		String cSparql = "SELECT DISTINCT ?p  WHERE { ?p rdfs:label " + label + " . }";
		Set<String> ret = execSparql(cSparql, ds);
		return ret.toArray()[0].toString();
	}

	private static String generateMappingRDFExact(Map<String, Set<String>> mapExact, String propMatch)
			throws IOException {

		String rdfOut = "";
		for (Map.Entry<String, Set<String>> entry : mapExact.entrySet()) {
			String d[] = entry.getKey().split(",");
			String dSource = d[0].trim().replace("[", "");

			for (String propClass : entry.getValue()) {
				// String triple = "<" + dSource + "> <"+ propClass + "> <" + dTarget + "> .\n";
				String prop = propClass.replaceAll("\\[", "").replaceAll("]", "").trim();
				String s = getPropLabel(prop, dSource);
				String triple = "<" + s + "> <" + propMatch + "> <" + s + "> .\n";
				rdfOut += triple;
			}
		}

		return rdfOut;
	}
	
	
}
