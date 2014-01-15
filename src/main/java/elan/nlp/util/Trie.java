/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author elan
 * @create 2013-4-27上午10:21:45
 */
public class Trie {
	private static final Logger logger = LoggerFactory.getLogger(Trie.class);
	private final int MAX_NUM = 26;
	private Node root;
	
	enum NodeType { COMPLETED, UNCOMPLETED }
	
	class Node {
		private Node[] child;
		private NodeType type;
		private int v;
		
		public Node(int v){
			child = new Node[MAX_NUM];
			type = NodeType.UNCOMPLETED;
			this.v = v;
		}

		public Node[] getChild() {
			return child;
		}

		public void setChild(Node[] child) {
			this.child = child;
		}

		public NodeType getType() {
			return type;
		}
		
		public void setType(NodeType type) {
			this.type = type;
		}
		
		public int getV() {
			return v;
		}

		public void setV(int v) {
			this.v = v;
		}
	}
	
	public void initialize() {
		root = new Node(0);
	}
	
	public void buildTrie(Set<String> words) {
		initialize();
		for(String word : words) insert(word);
	}
	
	public void insert(String word) {
		Node currentNode = root;
		char[] wordArray = word.toCharArray();
		
		for (int i = 0; i < wordArray.length; i++) {
			if(currentNode.getChild()[charToIndex(wordArray[i])] == null) {
				currentNode.getChild()[charToIndex(wordArray[i])] = new Node(currentNode.getV() + 1);
			}
			currentNode = currentNode.getChild()[charToIndex(wordArray[i])];
		}
		currentNode.setType(NodeType.COMPLETED);
	}
	
	public boolean contains(final char[] word) {
		Node currentNode = root;
		int i, len = word.length;
		for (i = 0; i < len; i++) {
			if(currentNode.getChild()[charToIndex(word[i])] == null) {
				break;
			}
			currentNode = currentNode.getChild()[charToIndex(word[i])];
		}
		
		return (i == len) && (currentNode.getType() == NodeType.COMPLETED);
	}
	
	public boolean contains(final char[] word, int start, int length) {
		Node currentNode = root;
		int i;
		for (i = start; i < length; i++) {
			if(currentNode.getChild()[charToIndex(word[i])] == null) {
				break;
			}
			currentNode = currentNode.getChild()[charToIndex(word[i])];
		}
		
		return (i == length) && (currentNode.getType() == NodeType.COMPLETED);
	}
	
	public boolean contains(String word) {
		char[] wordArray = word.toCharArray();
		return contains(wordArray);
	}
	
	private int charToIndex(char ch) {
		return ch - 'a';
	}
	
	public static void main(String[] args) throws IOException {
		String[] words = {"actually", "after", "afterwards", "before", "the", "and", "so"};
		HashSet<String> wordSet = new HashSet<String>();
		wordSet.addAll(Arrays.asList(words));
		
		Trie trie = new Trie();
		trie.buildTrie(wordSet);
		
		logger.debug("and exist :" + trie.contains("and"));
		logger.debug("but exist :" + trie.contains("but"));
		logger.debug("a exist :" + trie.contains("a"));
		logger.debug("apple exist :" + trie.contains("apple"));
		
	}
}
