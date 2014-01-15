/**
 * 
 * Copyright 2013.  All rights reserved. 
 * 
 */
package elan.nlp.util.json;

/**
 * @author wangjian
 * @create 2013年7月30日 下午8:02:34
 * @update TODO
 * 
 * 
 */
enum JsonScope {
    EMPTY_ARRAY,
    
    NONEMPTY_ARRAY,
    
    EMPTY_OBJECT,
    
    DANGLING_NAME,
    
    NONEMPTY_OBJECT,
    
    EMPTY_DOCUMENT,
    
    NONEMPTY_DOCUMENT,
    
    CLOSED
}
