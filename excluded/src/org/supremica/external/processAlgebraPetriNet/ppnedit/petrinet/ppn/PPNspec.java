package org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn;

import java.lang.*;
import java.util.LinkedList;

/**
*	class to specify specific operations for PNN
*	expressions
*
*	Internal operations needs for regexp to work
*
*
*
*/
public class PPNspec{
    
	/* Human operators*/
	public static final String[] HUMAN_OPERATIONS = new String[]{"->","+","*","&","--","=","<",">"};
    
	
	/* Internal operators*/
    public static final String SEQUENCE = "_SEQUENCE_";
    public static final String ALTERNATIVE = "_ALTERNATIVE_";
    public static final String ARBITARY_ORDER = "_ARBITARY_ORDER_";
    public static final String SYNCHRONIZE = "_SYNCHRONIZE_";
    public static final String PARALLEL = "_PARALLEL_";
    public static final String EQUAL = "=";
	
	
    public static final String FIRST_EVENT = "_FIRST_EVENT_";
    public static final String LAST_EVENT = "_LAST_EVENT_";
    
    /* Parenthesis */
    /* soft */
	public static final char START = '(';
    public static final char END = ')';
	
	/* brackets */
	public static final char BSTART = '[';
    public static final char BEND = ']';
	
	/* "gul wings" */
	public static final char GSTART = '{';
    public static final char GEND = '}';
    
	
	/* Internal operators*/
    public static final String[] INTERNAL_OPERATIONS = new String[]{SEQUENCE,ALTERNATIVE,
                                              		ARBITARY_ORDER,SYNCHRONIZE,
                                              		PARALLEL,EQUAL,FIRST_EVENT,
                                              		LAST_EVENT};
}

