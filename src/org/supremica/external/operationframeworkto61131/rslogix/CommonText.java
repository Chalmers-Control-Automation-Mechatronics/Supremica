package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.lang.StringBuffer;

import org.supremica.external.operationframeworkto61131.util.FileUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;


public class CommonText {

	public static LogUtil log = LogUtil.getInstance();

	private final static String NEW_LINE_WINDOWS = "\r\n";
	private final static String NEW_LINE_UNIX = "\n";
	public final static String TAB = "\t";
	public final static String NEW_LINE = NEW_LINE_WINDOWS;

	public final static String EQUAL = " := ";
	public final static String COLON = ": ";
	public final static String SEMICOLON = ";";
	public final static String COMMA = ",";
	public final static String SPACE = " ";
	public final static String QUOTATION = "\"";
	public final static String LEFT_ROUND_BRACKET = "(";
	public final static String RIGHT_ROUND_BRACKET = ")";
	public final static String LEFT_SQUARE_BRACKET = "[";
	public final static String RIGHT_SQUARE_BRACKET = "]";
	public final static String YES = "Yes";
	public final static String NO = "No";

	public static String getTabs(int nOfTabs) {

		StringBuffer ret = new StringBuffer();

		for (int i = 0; i < nOfTabs; i++) {

			ret.append(TAB);

		}

		return ret.toString();

	}

	public static void main(String args[]) {

		FileUtil file = new FileUtil(
				"C:\\Documents and Settings\\HAHA\\Desktop\\test.txt");

		StringBuffer buf = new StringBuffer();

		buf.append(CommonText.getTabs(3)).append("Start with 3 tabs").append(
				CommonText.NEW_LINE).append(CommonText.getTabs(3)).append(
				"Another line with 3 tabs");

		file.writeFile(buf.toString(), true);

	}

	public String getPropertyPair(String propertyName, String value, int nTabs) {

		String tabs = getTabs(nTabs);

		StringBuffer buf = new StringBuffer();

		buf.append(tabs).append(propertyName).append(EQUAL).append(value)
				.append(COMMA).append(NEW_LINE);
		
		return buf.toString();

	}
}
