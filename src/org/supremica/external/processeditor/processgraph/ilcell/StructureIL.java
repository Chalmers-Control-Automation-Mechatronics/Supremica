package org.supremica.external.processeditor.processgraph.ilcell;

import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.eop.EOP;

import java.util.Locale;

public class StructureIL {
	
	public static void printILStructure(){
		//printStructure(IL.class, "");
		printStructure(EOP.class, "");
		
	}
	
	public static void printStructure(Class t, String indent){
		
		Class tmpClass = null;
		
		String declarationName = "";
		String className = "";
		String declarationType = "";
		
		int i = 0;
		
		System.out.println(indent + t.toString());
		System.out.println(indent + "{");
		
		Object[] declaredFields = t.getDeclaredFields();
			
		for(Object field : declaredFields){
			String[] splited = field.toString().split(" ");
			
			declarationName = splited[2];
			className = splited[1];
			declarationType = splited[0];
				
			//System.out.println(indent + '\t' + declarationType);
			System.out.println(indent + '\t' + className);
			//System.out.println(indent + '\t' + declarationName);
				
			//Don't care to search native Java classes
			if(!className.equals("java.lang.String") &&
			   !className.equals("java.math.BigInteger"))
			{
				/*
				 * List can be anything. JAXB 2.0 makes a class, and name
				 * it from declaration name.
				 */
				if(className.equals("java.util.List"))
				{
					i = declarationName.lastIndexOf('.');
					className = declarationName.substring(i+1);
						
					//remove last package name
					declarationName = declarationName.substring(0, i);
					declarationName = declarationName.substring(0, declarationName.lastIndexOf('.')+1);
						
					//try to find class by replace letters so uppercase
					for(i = 1; i < className.length(); i++){
						className = className.substring( 0, i ).toUpperCase( Locale.ENGLISH ) + 
									className.substring( i, className.length() );
						try{
							//try to get Class
							tmpClass = Class.forName(declarationName + className);
						}catch(Exception e){
							tmpClass = null;
						}
							
						if(tmpClass != null){
							//class found
							printStructure(tmpClass,indent + '\t' + '\t');
							break;
						}
					}
				} else {
					try{
						printStructure(Class.forName(className),indent + '\t' + '\t');
					}catch(Exception e){
							;
					}
				}
			}
		}
		System.out.println(indent + "}");
	}
}
