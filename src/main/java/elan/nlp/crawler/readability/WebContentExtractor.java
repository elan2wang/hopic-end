/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler.readability;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author elan
 * @create 2013-4-9下午1:21:15
 * 
 * This content extractor is implemented referring to Readability-0.5.1
 */
public class WebContentExtractor {
	
	private static final String unlikelyCandidatesRe = "combx|comment|disqus|foot|header|menu|meta|nav|rss|shoutbox|sidebar|sponsor";
	private static final String okMaybeItsACandidateRe = "and|article|body|column|main";
	private static final String positiveRe = "article|body|content|entry|hentry|page|pagination|post|text";
	private static final String negativeRe = "combx|comment|contact|foot|footer|footnote|link|media|meta|promo|related|scroll|shoutbox|sponsor|tags|widget";
	private static final String divTopElementsRe = "<(a|blockquote|dl|div|img|ol|p|pre|table|ul)";
	
	private static Pattern unlikelyCandidatesP = Pattern.compile(unlikelyCandidatesRe, Pattern.CASE_INSENSITIVE);
	private static Pattern okMaybeItsACandidateP = Pattern.compile(okMaybeItsACandidateRe, Pattern.CASE_INSENSITIVE);
	private static Pattern positiveP = Pattern.compile(positiveRe, Pattern.CASE_INSENSITIVE);
	private static Pattern negativeP = Pattern.compile(negativeRe, Pattern.CASE_INSENSITIVE);
	private static Pattern divTopElementsP = Pattern.compile(divTopElementsRe, Pattern.CASE_INSENSITIVE);
	
	/**
	 * remove JS scripts, CSS, form and header elements 
	 */
	public static void cleanDocument(Document doc){
		/* remove all header elements */
		Elements headers = doc.head().children();
		for(Element header : headers) header.remove();
		
		/* remove scripts */
		Elements scripts = doc.getElementsByTag("script");
		for(Element script : scripts) script.remove();
		
		/* remove style */
		Elements styles = doc.getElementsByTag("style");
		for(Element style : styles) style.remove();
		
		/* remove style in elements */
		cleanStyle(doc.body().children());
	}
	
	public static String getArticleTitle(Document doc){
		return doc.title();
	}
	
	public static Charset getCharset(Document doc){
		String content = doc.attr("content");
		String charset = "UTF-8";
		if(content.contains("charset")){
			String[] strs = content.split("=");
			for(int i=0; i<strs.length; i++){
				if(strs[i].equals("charset")){
					charset = strs[i+1];
					break;
				}
			}
		}
		return Charset.forName(charset);
	}
	
	public static String grapArticle(Document doc){
		/*
		 * First, node preparing. Trash nodes that look cruddy (like ones with class
		 * name "comment", etc), and turn DIV tag into P tag where they have been used
		 * inappropriately (as in, where they contain no other block level elements)
		 */
		Elements all = doc.getAllElements();
		for (Element e : all){
			/* remove all unlikely candidate */
			String classAndId = e.className() + e.id();
			Matcher m1 = unlikelyCandidatesP.matcher(classAndId);
			Matcher m2 = okMaybeItsACandidateP.matcher(classAndId);
			if(m1.find() && !m2.find() && !e.tagName().equals("body")){
				//logger.debug("remove class : "+e.className());
				e.remove();
				continue;
			}
			
			/* turn all div's that don't have children block level elements to p's  */
			if(e.tagName().equalsIgnoreCase("div")){
				if(!divTopElementsP.matcher(e.html()).find()){
					//logger.debug("turn tag div whose class name is \"" + e.className() + "\" to p");
					e.tagName("p");
				} else {
					/* EXPERIMENTAL */
				}
			}
			
		}
		
		/* 
		 * Loop through all paragraphs, and assign a score to them based on how content-y
		 * they look. Then add their score to their parent node.
		 *
		 * A score is determined by things like number of commas, class names, etc. 
		 * Maybe eventually link density.
		 */
		Elements allParagraphs = doc.getElementsByTag("p");
		CandidateList candidates = new CandidateList();
		for (Element e : allParagraphs){
			Element parent = e.parent();
			Element grandParent = parent.parent();
			String content = e.text();
			
			/* If this paragraph is less than 25 characters, don't even count it. */
			if(content.length() < 25) continue;
			
			/*
			 *  check whether this element's parent and grand parent are in candidate list,
			 *  if not, create them with 0 score, and add them to candidate list
			 */
			Candidate c_parent = candidates.get(parent);
			Candidate c_grandParent = candidates.get(grandParent);
			if(c_parent == null){
				c_parent = new Candidate(parent, 0);
				initializeCandidate(c_parent);
				candidates.add(c_parent);
			}
			if(c_grandParent == null){
				c_grandParent = new Candidate(grandParent, 0);
				initializeCandidate(c_grandParent);
				candidates.add(c_grandParent);
			}
			
			int score = 0;
			
			/* Add a point for the paragraph itself as a base */
			score ++;
			
			/* Add points for any commas within this paragraph */
			score += e.text().split(",").length;
			
			/* For every 100 characters in this paragraph, add another point. Up to 3 points. */
			int tmp = e.text().length()/100;
			score += (tmp > 3 ? 3 : tmp);
			
			/* Add the score to the parent. The grandparent gets half. */
			c_parent.setScore(c_parent.getScore() + score);
			c_grandParent.setScore(c_grandParent.getScore() + score/2);
		}
		/*
		 * After we've calculated scores, loop through all of the possible candidate nodes we found
		 * and find the one with the highest score.
		 */
		Candidate topCandidate = null;
		for(int i=0; i < candidates.size(); i++){
			/*
			 * Scale the final candidates score based on link density. Good content should have a
			 * relatively small link density (5% or less) and be mostly unaffected by this operation.
			 */
			Candidate c = candidates.get(i);
			c.setScore((int) (c.getScore()*(1 - getLinkDensity(c.getElement()))));
			
			if(topCandidate == null || c.getScore() > topCandidate.getScore()) topCandidate = c;
		}
		
		/*
		 * If we still have no top candidate, just use the body as a last resort.
		 * We also have to copy the body node so it is something we can modify.
		 */
		if (topCandidate == null || topCandidate.getElement().className().equalsIgnoreCase("body"))
		{
			Element e = doc.createElement("div");
			e.html(doc.body().html());
			doc.body().html("");
			doc.body().appendChild(e);
			
			topCandidate = new Candidate(e, 0);
			initializeCandidate(topCandidate);
		}

		/*
		 * Now that we have the top candidate, look through its siblings for content that might also be related
		 * Things like preambles, content split by ads that we removed, etc.
		 */
		Element articleContent = doc.createElement("div");
		Elements siblingNodes = topCandidate.getElement().parent().children();
		int siblingScoreThreshold = (int)(10 < topCandidate.getScore()*0.2 ? topCandidate.getScore()*0.2 : 10);
		for(Element e : siblingNodes){
			boolean append = false;
			
			if(e == topCandidate.getElement()){
				append = true;
			}
				
			if(candidates.get(e) != null && candidates.get(e).getScore() >= siblingScoreThreshold){
				append = true;
			}
				
			if(e.tagName().equals("p")){
				int textLength = e.text().length();
				if(textLength > 0){
					float linkDensity = getLinkDensity(e);
					if(textLength > 80 && linkDensity < 0.25){
						append = true;
					}
					else if(linkDensity == 0 && Pattern.compile(".( |$)").matcher(e.text()).find()){
						append = true;
					}
				}
				
			}
			if(append){
				articleContent.appendChild(e);
			}
		}
		return articleContent.text();
	}
		
	private static void cleanStyle(Elements nodes){
		Iterator<Element> ite = nodes.iterator();
		while(ite.hasNext()){
			Element e = ite.next();
			e.removeAttr("style");
			cleanStyle(e.children());
		}
	}
	
	private static void initializeCandidate(Candidate c){
		Pattern p1 = Pattern.compile("DIV", Pattern.CASE_INSENSITIVE);
		Pattern p2 = Pattern.compile("PRE|TD|BLOCKQUOTE", Pattern.CASE_INSENSITIVE);
		Pattern p3 = Pattern.compile("ADDRESS|OL|UL|DL|DD|DT|FORM|L", Pattern.CASE_INSENSITIVE);
		Pattern p4 = Pattern.compile("H1|H2|H3|H4|H5|H6|TH", Pattern.CASE_INSENSITIVE);
		 
		c.setScore(0);
		String classname = c.getElement().tagName();
		if(p1.matcher(classname).find()){
			//logger.debug("TAG: DIV");
			c.setScore(c.getScore() + 5);
		}
		else if(p2.matcher(classname).find()){
			//logger.debug("TAG: PRE|TD|BLOCKQUOTE");
			c.setScore(c.getScore() + 3); 
		}
		else if(p3.matcher(classname).find()){
			//logger.debug("TAG: ADDRESS|OL|UL|DL|DD|DT|FORM|LI");
			c.setScore(c.getScore() - 3);
		}
		else if(p4.matcher(classname).find()){
			//logger.debug("TAG: H1|H2|H3|H4|H5|H6|TH");
			c.setScore(c.getScore() - 5); 
		}
		c.setScore(c.getScore() + getClassWeight(c.getElement()));
	}

	private static int getClassWeight(Element e){
		int weight = 0;
		
		/* look for special clasName */
		if(!e.className().equals("")){
			if (positiveP.matcher(e.className()).find()) weight += 25;
			if (negativeP.matcher(e.className()).find()) weight -= 25;	
		}
		/* look for special ID */
		if(!e.id().equals("")){
			if (positiveP.matcher(e.id()).find()) weight += 25;
			if (negativeP.matcher(e.id()).find()) weight -= 25;
		}
		
		return weight;
	}
	
	/* get the percentage of link text in whole text */
	private static float getLinkDensity(Element e){
		Elements allA = e.getElementsByTag("a");
		int textLength = e.text().length();
		int linkLength = 0;
		for(Element a : allA){
			linkLength += a.text().length();
		}
		return linkLength/textLength;
	}

}
