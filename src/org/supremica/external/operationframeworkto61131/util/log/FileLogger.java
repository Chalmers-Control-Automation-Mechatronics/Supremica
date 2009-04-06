package org.supremica.external.operationframeworkto61131.util.log;
/**
 * @author LC
 *
 */
import java.util.Date;

import org.supremica.external.operationframeworkto61131.util.FileUtil;




public class FileLogger implements Logger {

	private FileUtil fileUtil;

	public FileLogger(String logFileName) {

		fileUtil = new FileUtil(logFileName);

	}

	public void log(String message) {

		fileUtil.writeLine(new Date().toString(), false);
		fileUtil.writeLine(message, false);
		fileUtil.writeLine("", false);

	}

	public static void main(String args[]) {

		String file = "C:\\haha123.txt";

		FileLogger fileLogger = new FileLogger(file);

		fileLogger.log("jhahahah");

		fileLogger.log("public static void main(String args[]) {");
	}
}
