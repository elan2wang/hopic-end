/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler.readability;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author elan
 * @create 2013-4-9下午3:56:17
 */
public class CandidateList{
	private static final Logger logger = LoggerFactory.getLogger(CandidateList.class);
	private List<Candidate> candidates = new ArrayList<Candidate>();
	private int length = 0;
	
	public void add(Candidate c){
		if(!candidates.contains(c)){
			candidates.add(c);
			length++;
		}
	}
	
	public void add(Element e, int score){
		if(this.containElement(e)){
			logger.debug("Already exist candidate whose element is the same, " +
					"its score will change to new score");
			Candidate c = this.get(e);
			c.setScore(score);
		} else {
			candidates.add(new Candidate(e, score));
			length++;
		}
	}
	
	public Candidate topCandidate(){
		int maxScore = 0;
		int topIndex = -1;
		for(int i=0; i < candidates.size(); i++){
			if(candidates.get(i).getScore() > maxScore){
				maxScore = candidates.get(i).getScore();
				topIndex = i;
			}
		}
		return candidates.get(topIndex);
	}
	
	public boolean containElement(Element e){
		for(Candidate c : candidates){
			if(c.getElement() == e) return true;
		}
		return false;
	}
	
	public Candidate get(Element e){
		for(Candidate c : candidates){
			if(c.getElement() == e) return c;
		}
		return null;
	}

	public int size(){
		return this.length;
	}
	
	public Candidate get(int index){
		return candidates.get(index);
	}
}
