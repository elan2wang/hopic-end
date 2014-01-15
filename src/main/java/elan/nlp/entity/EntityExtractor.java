package elan.nlp.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import elan.nlp.crawler.http.Response;
import elan.nlp.util.ConfigUtil;

public class EntityExtractor {

	private AlchemyAPI alchemyAPI;
	
	// key:entity, value:total count
	private HashMap<String, Integer> entities_count;
	// key:entity, value:passage count
	private HashMap<String, Integer> entities_exist;
	
	public EntityExtractor() {
		entities_count = new HashMap<String, Integer>();
		entities_exist = new HashMap<String, Integer>();
		alchemyAPI = new AlchemyAPI(ConfigUtil.getValue("ALCHEMY_API"), ConfigUtil.getValue("ALCHEMY_API_KEY"));
   	}

	public void batchProcess(String srcDir, String outputFile, String topFile) {
		File outfile = new File(outputFile);
		if(outfile.exists()) {
			outfile.delete();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(outputFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		File dir = new File(srcDir);
		File[] files = dir.listFiles();
		Integer num = 1;
		for (File file : files) {
			// read text from file
			String filename = file.getName();
			if (filename.startsWith(".") || !filename.endsWith(".txt")) continue;
			String text = readTextFromFile(file);
					
			// send Alchemy API request
			Response response = alchemyAPI.GetRankedNamedEntities(text);
			if (response == null) {
				try {
					fw.append("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				continue;
			}
			
			// get entities from result
			HashMap<String, Integer> entities = (HashMap<String, Integer>) getEntities(response);
			appendToOutfile(fw, entities);
			System.out.print(".");
			if (num++ % 20 == 0) System.out.print("\n");
		}
		
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		getHotEntities(topFile);
	}
	
	private String readTextFromFile(File file) {
		StringBuilder text = new StringBuilder();
		try {
			String line = null;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while( (line = reader.readLine()) != null) {
				text.append(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return text.toString();
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
					
					if (entities_count.containsKey(entity_name)) {
						entities_count.put(entity_name, entities_count.get(entity_name)+entity_count);
						entities_exist.put(entity_name, entities_exist.get(entity_name)+1);
					} else {
						entities_count.put(entity_name, entity_count);
						entities_exist.put(entity_name, 1);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return entityMap;
	}

	private void appendToOutfile(FileWriter fw, Map<String, Integer> entities) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> entity : entities.entrySet()) {
			sb.append(entity.getKey()+":"+entity.getValue()+"\t");
		}
		if (sb.length() > 0) sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		try {
			fw.append(sb.toString());
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void getHotEntities(String path) {
		
		List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(entities_exist.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
            //降序排序
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
            
        });
        
		File outfile = new File(path);
		if(outfile.exists()) {
			outfile.delete();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(path);
			for(Entry<String, Integer> entry: entities_exist.entrySet()) {
				fw.append(entry.getKey()+ "\t" +entry.getValue() + "\n");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) {
		EntityExtractor ee = new EntityExtractor();
		ee.batchProcess("News/fulltext1", "News/entity/bbc_entities.txt", "News/entity/bbc_top_entities.txt");
	}
}
