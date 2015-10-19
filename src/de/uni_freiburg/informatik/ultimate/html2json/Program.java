package de.uni_freiburg.informatik.ultimate.html2json;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Program {

	/**
	 * h1 ist der Name der erzeugten .json (id) wenn id="Ultimate" gilt, wird
	 * keine interface section gesucht und die datei automatisch home.json
	 * genannt alles was zwischen h1 und h2 ist ist der content vom toplevel,
	 * i.e. description h2 ist der title einer section alles was nach h2 und vor
	 * dem nächsten h2 oder dem ende kommt ist content dieser section
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		if (args == null || args.length != 2) {
			System.out
					.println("Use two parameters, first is input URL/file, second is output directory");
			System.exit(-1);
		}

		String url = args[0];
		String basedir = args[1];

		Document doc = null;
		File f = new File(url);
		try {
			if (f.exists()) {
				doc = getDocumentFromFile(f);
			} else {
				doc = getDocumentFromUrl(url);
			}
		} catch (Exception ex) {
			System.out.println("Could not open input " + url + ": "
					+ ex.getMessage());
			System.exit(-2);
		}
		JsonPageDescriptor obj = null;

		try {
			obj = createDescriptorFromDocument(doc);
		} catch (Exception ex) {
			System.out.println("Could not parse document " + url + ": "
					+ ex.getMessage());
			System.exit(-3);
		}

//		TestJSoup(doc);
		
		try {
			obj.writeToFile(basedir);
		} catch (IOException ex) {
			System.out.println("Could not write file in directory " + basedir
					+ ": " + ex.getMessage());
			System.exit(-4);
		}
		System.out.println("Finished conversion of " + url);
		System.exit(0);
	}

	private static Document getDocumentFromUrl(String url) throws IOException {
		return Jsoup.connect(url).get();
	}

	private static Document getDocumentFromFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF8"));
		String eol = System.getProperty("line.separator");
		StringBuffer text = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			text.append(line).append(eol);
		}
		br.close();
		return Jsoup.parse(text.toString());
	}

	private static JsonPageDescriptor createDescriptorFromDocument(Document doc)
			throws IOException {

		Element topId = doc.select("* > h1").first();
		if (topId == null) {
			throw new IllegalArgumentException(
					"Document does not contain title (no <h1> tag found)");
		}
		Elements topContent = doc.select(buildTopContentSelector(topId));

		String topIdName = topId.text();
		if (topIdName.equals("Ultimate")) {
			topIdName = "home";
		}

		Map<String, String> sectionTitle2sectionContent = new LinkedHashMap<String, String>();
		String interfaceDescription = null;

		Elements sectionTitles = doc.select("* > h2");
		for (int i = 0; i < sectionTitles.size(); i++) {
			Element current = sectionTitles.get(i);
			Element next = i + 1 < sectionTitles.size() ? sectionTitles
					.get(i + 1) : null;
			Elements sectionContent = doc.select(buildSectionSelector(current,
					next));

			if (!topIdName.equals("home")
					&& current.text().toLowerCase().equals("web interface")) {
				interfaceDescription = sectionContent.toString();
			} else {

				sectionTitle2sectionContent.put(current.text(),
						sectionContent.toString());
			}

		}

		return new JsonPageDescriptor(topIdName, topContent.toString(),
				sectionTitle2sectionContent, interfaceDescription);
	}

//	private static void TestJSoup(Document doc) throws IOException {
//		Element topId = doc.select("* > h1").first();
//		Elements topContent = doc.select(buildTopContentSelector(topId));
//		System.out.println(topId.text());
//		System.out.println(topContent);
//		System.out.println();
//
//		Elements sectionTitles = doc.select("* > h2");
//		for (int i = 0; i < sectionTitles.size(); i++) {
//			Element current = sectionTitles.get(i);
//			Element next = i + 1 < sectionTitles.size() ? sectionTitles
//					.get(i + 1) : null;
//
//			System.out.println(current.text());
//			Elements sectionContent = doc.select(buildSectionSelector(current,
//					next));
//			System.out.println(sectionContent);
//			System.out.println();
//		}
//	}

	private static String buildTopContentSelector(Element topElement) {
		return "h1 ~ *:not(h2 ~ *,h2)";
	}

	private static String buildSectionSelector(Element current, Element next) {
		if (next != null) {
			int nextidx = next.elementSiblingIndex()-1;
			return "h2:containsOwn(" + current.text() + ") ~ *:not(h2:gt(" + nextidx
					+ ") ~ *,h2:gt(" + nextidx + "))";
		} else {
			return "h2:contains(" + current.text() + ") ~ *";
		}
	}

}
