package de.uni_freiburg.informatik.ultimate.html2json;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonPageDescriptor {

	private String mTitle;
	private LinkedHashMap<Object, Object> mMap;

	public JsonPageDescriptor(String title, String topLevelDescription,
			Map<String, String> sectionTitle2sectionContent,
			String interfaceDescription) {
		title = convertToId(title);
		mTitle = title;

		mMap = new LinkedHashMap<>();

		mMap.put("id", title);
		mMap.put("description", topLevelDescription);
		mMap.put("user_info", "");
		mMap.put("animate", "true");
		mMap.put("html", "true");

		ArrayList<Object> sectionsList = new ArrayList<>();
		if (interfaceDescription != null) {
			// add a section for the webinterface at the beginning
			LinkedHashMap<Object, Object> currentMap = new LinkedHashMap<Object, Object>();

			currentMap.put("title", "Web Interface");
			currentMap.put("type", "interface");
			currentMap.put("html", "true");
			currentMap.put("content", interfaceDescription);
			currentMap.put("link_deco", "true");
			currentMap.put("button", "Open interface");

			sectionsList.add(currentMap);
		}

		// add user-defined sections
		for (Entry<String, String> entry : sectionTitle2sectionContent
				.entrySet()) {
			LinkedHashMap<Object, Object> currentMap = new LinkedHashMap<Object, Object>();

			currentMap.put("title", entry.getKey());
			currentMap.put("type", "text");
			currentMap.put("html", "true");
			currentMap.put("content", entry.getValue());
			currentMap.put("link_deco", "true");
			sectionsList.add(currentMap);
		}
		mMap.put("sections", sectionsList);

	}

	private String convertToId(String id) {
		return id.toLowerCase().replace(" ", "_").replace("ä", "ae")
				.replace("ö", "oe").replace("ü", "ue");
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		return gson.toJson(mMap);
	}

	public void writeToFile(String path) throws IOException {
		File baseDir = new File(path);
		File actualFile = new File(baseDir, convertToId(mTitle) + ".json");

		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(actualFile), "UTF-8"));
		try {
			out.write(this.toString());
			out.flush();
		} finally {
			out.close();
		}
	}

}
