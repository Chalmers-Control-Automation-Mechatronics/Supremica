package org.supremica.external.operationframeworkto61131.util;
/**
 * @author LC
 *
 */
import org.plcopen.xml.tc6.ObjectFactory;

public class StringUtil {

	public static Boolean isEmpty(String param) {

		if (param == null) {
			return true;
		} else if (param.length() < 1) {
			return true;
		} else {
			return false;
		}

	}

	public static org.plcopen.xml.tc6.FormattedText getFormattedTextFromString(
			String str) {

		ObjectFactory plcopenObjectFactory = new ObjectFactory();

		org.plcopen.xml.tc6.FormattedText formattedText = plcopenObjectFactory
				.createFormattedText();
		if (!StringUtil.isEmpty(str)) {

			formattedText.setAny(new String(str));

		}

		return formattedText;
	}
	
	
	public static String removeSpace(String str){
		
		
		return str.trim().replace(" ", "");
		
	}
	
	public static String replaceSpaceWithUnderscore(String str){
		
		
		return str.trim().replace(" ", "_");
	}
	
	public static void main(String[] args) {

		System.out.println(StringUtil.removeSpace(" I'm here to see the movie. d"));
	}
}
