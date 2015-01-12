package elan.nlp.entity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import elan.nlp.util.FileUtil;

public class EntityCleaner {
	
	private static final int MIN_COUNTS = 20;
	
	private Map<String, Integer> entities_count;
	private Map<String, Integer> entities_index;
	
	private String src;
	private String dst;
	private String dict;
	
	public EntityCleaner(String src, String dst, String dict) {
		this.entities_count = new HashMap<String, Integer>();
		this.entities_index = new HashMap<String, Integer>();
		
		this.src = src;
		this.dst = dst;
		this.dict = dict;
	}
	
	public void calc() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(src));
			String line = null;
			while ((line = reader.readLine()) != null) {
				for (String entry : line.split("\t")) {
					if (entry.indexOf(':') == -1) continue;
					String [] pair = entry.split(":");
					if (entities_count.get(pair[0]) != null) {
						Integer value = entities_count.get(pair[0]) + Integer.parseInt(pair[1]);
						entities_count.put(pair[0], value);
					} else {
						entities_count.put(pair[0], Integer.parseInt(pair[1]));
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void index() {
		for (Entry<String, Integer> entry : entities_count.entrySet()) {
			if (entry.getValue() >= MIN_COUNTS) {
				entities_index.put(entry.getKey(), entities_index.size()+1);
			}
		}
	}
	
	public void filter() {
		try {
			FileWriter fw = FileUtil.open(dst);
			BufferedReader reader = new BufferedReader(new FileReader(src));
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringBuffer sb = new StringBuffer();
				for (String entry : line.split("\t")) {
					if (entry.indexOf(':') == -1) continue;
					String [] pair = entry.split(":");
					if (entities_index.containsKey(pair[0])) {
						sb.append(entities_index.get(pair[0])).append(":")
						.append(pair[1]).append("\t");
					}
				}
				sb.append("\n");
				FileUtil.append(fw, sb.toString());
			}
			reader.close();
			FileUtil.close(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void dict() {
		FileWriter fw = FileUtil.open(dict);
		for (Entry<String, Integer> entry : entities_index.entrySet()) {
			StringBuffer sb = new StringBuffer();
			sb.append(entry.getKey()).append("\t").append(entry.getValue()).append("\n");
			FileUtil.append(fw, sb.toString());
		}
		FileUtil.close(fw);
	}
	
	public static void main(String[] args) {
		EntityCleaner ec = new EntityCleaner("News/entity/bbc2_all.txt", "News/entity/entity_all.txt", "News/entity/entity_dict.txt");
		ec.calc();
		ec.index();
		ec.filter();
		ec.dict();
	}
}
