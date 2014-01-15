/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * @author elan
 * @create 2013-4-27上午10:02:56
 */
public class WordsLoader {
	
	/**
	 * @param wordfile File contains the word list
	 * @return A HashSet with the wordfile's words
	 * @throws IOException
	 */
	public static HashSet<String> getWordSet(File wordfile){
		HashSet<String> result = new HashSet<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(wordfile));
			String word = null;
			while((word = reader.readLine()) != null) {
				result.add(word.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * @param wordfile File contains the word list
	 * @param comment The comment string to ignore
	 * @return A HashSet with the wordfile's words
	 * @throws IOException
	 */
	public static HashSet<String> getWordSet(File wordfile, String comment) {
		HashSet<String> result = new HashSet<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(wordfile));
			String word = null;
			while((word = reader.readLine()) != null) {
				if(!word.startsWith(comment))
					result.add(word.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
