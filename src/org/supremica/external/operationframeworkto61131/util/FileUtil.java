package org.supremica.external.operationframeworkto61131.util;
/**
 * @author LC
 *
 */
import java.io.*;

import org.supremica.external.operationframeworkto61131.util.log.LogUtil;



public class FileUtil {

	private String fileName;
	private BufferedReader bufReader = null;
	private BufferedWriter bufWriter = null;
	private static LogUtil log = LogUtil.getInstance();
	private final static String NEW_LINE = "\r\n";

	public static String fixPathEndSign(String path) {

		if (path.contains("\\") && !path.endsWith("\\")) {

			path = path + "\\";
		} else if (path.contains("/") && !path.endsWith("/")) {

			path = path + "/";
		}

		return path;
	}

	public FileUtil(String fileName) {
		this.fileName = fileName;

	}

	/*
	 * overwrite the existing file from the beginning.
	 */
	public String readFile() {

		if (isBufferedReaderReady()) {
			StringBuffer stringBuffer = new StringBuffer();
			try {
				while (bufReader.ready()) {
					stringBuffer.append(readLine()).append(NEW_LINE);
				}

				close();
			} catch (Exception e) {
				close();
			}

			return stringBuffer.toString();

		} else {
			return null;
		}
	}

	public String readLine() {

		if (isBufferedReaderReady()) {

			try {
				if (bufReader.ready()) {
					return bufReader.readLine();
				} else {
					return null;
				}

			} catch (Exception e) {
				log.error("Fail to read line from file:" + fileName);

				return null;
			}
		} else {
			return null;
		}

	}

	public void writeFile(String info, Boolean append) {

		getBufferWriter(this.fileName, append);

		try {
			bufWriter.write(info);
			close();
		} catch (Exception e) {
			log.error(e.toString() + " Fail to write file:" + fileName);

			close();
		}

	}

	private Boolean isBufferedReaderReady() {
		if (bufReader == null) {
			try {
				File inputfile = new File(fileName);

				if (isValid(inputfile, fileName)) {

					bufReader = new BufferedReader(new FileReader(fileName));
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				log.error(e.toString() + ": Can't open file " + fileName);

				close();
				return false;
			}

		} else {
			return true;
		}

	}

	/*
	 * overwrite the existing file from the beginning.
	 */

	public void writeLine(String info, Boolean append) {
		getBufferWriter(this.fileName, append);
		try {

			bufWriter.write(info);
			bufWriter.newLine();
			bufWriter.flush();
			// close();

		} catch (Exception e) {
			// e.printStackTrace();
			log.error("Fail to write line to file:" + fileName);

			close();
		}

	}

	private void getBufferWriter(String fileName, Boolean append) {

		if (bufWriter == null) {

			try {
				bufWriter = new BufferedWriter(new FileWriter(fileName, append));
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.toString() + ": Can't open file " + fileName);

				close();
			}

		}
	}

	public void close() {

		try {
			if (bufReader != null) {
				bufReader.close();

				bufReader = null;
			}

		} catch (Exception e) {
			log.error("Failed to close file:" + fileName);
		}

		try {
			if (bufWriter != null) {
				bufWriter.close();
				bufWriter = null;
			}

		} catch (Exception e) {
			log.error("Failed to close file:" + fileName);
		}

	}

	/*
	 * Check the validation of the input file
	 */
	public static Boolean isValid(File inputfile, String fileNameInfo) {		
		
		if (inputfile == null) {

			log.error("Failed to load file:" + fileNameInfo);
			return false;
		}

		if (!inputfile.exists()) {

			log.error("File does not exist:" + fileNameInfo);

			return false;
		}

		if (inputfile.isDirectory()) {

			log
					.error("Target is a directory, a file is needed:"
							+ fileNameInfo);

			return false;

		}

		return true;
	}

	public static void main(String args[]) {

		String file = "C:\\haha.java";

		String file2 = "C:\\haha2.txt";

		Boolean append = false;
		FileUtil fileUtil = new FileUtil(file);
		FileUtil fileUtil2 = new FileUtil(file2);
		System.out.println("start");

		while (true) {
			String str = fileUtil.readLine();
			if (str != null) {
				// System.out.println(str);
				fileUtil2.writeLine(str, append);
			} else {

				break;
			}

		}

		fileUtil.close();
		fileUtil2.close();

		FileUtil fileUtil3 = new FileUtil("C:\\123.txt");
		fileUtil3.writeLine("adsfaf" + "\t" + "ddd", append);
		fileUtil3.close();

	}

}
