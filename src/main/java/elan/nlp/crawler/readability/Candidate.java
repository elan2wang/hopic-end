/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler.readability;

import org.jsoup.nodes.Element;

/**
 * @author elan
 * @create 2013-4-9下午3:36:02
 */
public class Candidate {
	private Element element;
	private int score;
	
	public Candidate(Element element, int score) {
		super();
		this.element = element;
		this.score = score;
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
}
