package elan.nlp.entity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import elan.nlp.crawler.http.Response;
import elan.nlp.util.ConfigUtil;
import elan.nlp.util.FileUtil;

public class EntityExtractor {

	private AlchemyAPI alchemyAPI;

	public EntityExtractor() {
		alchemyAPI = new AlchemyAPI(ConfigUtil.getValue("ALCHEMY_API"), ConfigUtil.getValue("ALCHEMY_API_KEY"));
	}

	public void batchProcess(String src, String dstPrefix) throws IOException {
		String all = dstPrefix + "all.txt";
		String err = dstPrefix + "err.txt";

		FileWriter all_fw = FileUtil.open(all);
		FileWriter err_fw = FileUtil.open(err);

		
		BufferedReader reader = new BufferedReader(new FileReader(src));
		String line = null;
		int num = 1;
		while ((line = reader.readLine()) != null) {
			String url = line.split("\t")[2];
			// send Alchemy API request
			Response response = alchemyAPI.getRankedNamedEntities(url);
			if (response == null) {
				FileUtil.append(err_fw, line);
				FileUtil.append(all_fw, "\n");
				System.out.print("W");
				continue;
			}

			// get entities from result
			HashMap<String, Integer> entities = (HashMap<String, Integer>) getEntities(response);
			StringBuffer sb = new StringBuffer();
			for (Entry<String, Integer> entry : entities.entrySet()) {
				sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\t");
			}
			sb.append("\n");
			FileUtil.append(all_fw, sb.toString());
			System.out.print("|");
			if ((num++)%20 == 0) System.out.println();
		}
		reader.close();
		FileUtil.close(all_fw);
		FileUtil.close(err_fw);
	}

	private Map<String, Integer> getEntities(Response response) {
		HashMap<String, Integer> entityMap = new HashMap<String, Integer>();
		try {
			JSONArray entities = new JSONObject(response.asString()).getJSONArray("entities");
			for (int i=0; i<entities.length(); i++) {
				JSONObject entity = entities.getJSONObject(i);
				String type = entity.getString("type");
				if (type.equalsIgnoreCase("person") || 
						type.equalsIgnoreCase("country") ||
						type.equalsIgnoreCase("company")) {
					String entity_name = entity.getString("text");
					Integer entity_count = Integer.parseInt(entity.getString("count"));

					entityMap.put(entity_name, entity_count);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return entityMap;
	}

	public static void main(String args[]) throws IOException {
		EntityExtractor ee = new EntityExtractor();
		ee.batchProcess("News/todolist/Guardian_Edward_Snowden.dat", "News/entity/bbc2_");
	}
}
