package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import org.supremica.external.operationframeworkto61131.util.FileUtil;

public class HeaderNFooter {

	private  String headerFile;
	private  String footerFile;
	
	
	
	public HeaderNFooter(String headerFile, String footerFile){
		
		this.headerFile=headerFile;
		this.footerFile=footerFile;
		
	}

	public  String getHeader() {

		FileUtil file = new FileUtil(headerFile);

		return file.readFile();

	}

	public  String getFooter() {

		FileUtil file = new FileUtil(footerFile);

		return file.readFile();

	}

}
