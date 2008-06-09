package org.supremica.external.avocades.xml;

public class SpecificationSynthesXML {
	
	/*-----------------------------------------------------------------------*/
	/*
	/* XML - Strings
	/* 
	/*-----------------------------------------------------------------------*/
	
	//XML-tags from original input format
	public static final String ILSEOPS            = "ILsEOPs";
	public static final String SIMULTANEITY       = "simultaneity";
	public static final String PROCESS            = "Process";
	public static final String OPERATION          = "Operation";
	
	public static final String STATE              = "State";
	public static final String EVENT              = "Event";
	public static final String RESTRICTION        = "Restriction";
	public static final String AND                = "And";
	public static final String OR                 = "Or";
	
	public static final String EVENT_IL           = "Event_interlocking";
	public static final String OPERATION_IL       = "Robot_interlocking";
	
	public static final String INT_STATE          = "Internal_state";
	public static final String EXT_STATE          = "External_state";
	
	public static final String OP_NOT_ONGOING     = "Op_not_ongoing";
	public static final String OP_NOT_STARTED     = "Op_not_started";
	
	//XML definitions used as attributes
	public static final String ID                 = "id";
	public static final String TYPE               = "type";
	public static final String NAME               = "name";
	
	
	//This array contains strings who can be treated as empty
	public static final String[] NOT_VALID_STRINGS = new String[]{ "-", "" };
	
	/*-----------------------------------------------------------------------*/
	/*
	/* Basic functions
	/* 
	/*-----------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * This function returns true if string dosen't 
	 * exist in NOT_VALID_STRINGS
	 * 
	 * @param string
	 * @return
	 */
	protected static boolean isValidString( String string ){
		
		//Sanity check
		if(string == null || string.length() == 0){
			return false;
		}
		
		//Test string against all strings in NOT_VALID_STRINGS
		for(int i=0; i < NOT_VALID_STRINGS.length; i++ ){
			if( NOT_VALID_STRINGS[i].equals( string ) ){
				return false;
			}
		}
		
		return true;
	}
	
}
