package org.supremica.external.operationframeworkto61131.builder;

import org.plcopen.xml.tc6.*;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;



import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * ProjectInformationBuilder.java handles the information about the whole
 * program at the beginning of the PLCopen XML file. Only the time is generated
 * in this version. Other information is keep as they were from the input
 * PLXopen xml file.
 * 
 * 
 * Created: Mar 31, 2009 6:10:28 PM
 * 
 * @author LC
 * @version 1.0
 */
public class ProjectInformationBuilder {
	private static LogUtil log = LogUtil.getInstance();

	//		
	// <project xmlns="http://www.plcopen.org/xml/tc6.xsd">
	// <fileHeader productVersion="1.0" productName="FIX1"
	// companyURL="www.chalmers.se" companyName="CTH"
	// creationDateTime="2008-03-19T20:31:26"/>
	// <contentHeader language="en-US" author="CTH SS2" organization="SS2"
	// version="1.0" name="Cell Example ">
	// <coordinateInfo>
	// <fbd>
	// <scaling y="0" x="0"/>
	// </fbd>
	// <ld>
	// <scaling y="0" x="0"/>
	// </ld>
	// <sfc>
	// <scaling y="0" x="0"/>
	// </sfc>
	// </coordinateInfo>
	// </contentHeader>

	public static void buildDescription(org.plcopen.xml.tc6.Project project) {

		addDateAndTime(project);
		addAuthorInformation(project);

		// FIXME add project information( author, organization...)

	}

	// The current time when the function is called will be added to
	// creationDateTime in fileHeader and modificationDateTime in contentHeader
	private static void addDateAndTime(org.plcopen.xml.tc6.Project project) {

		Project.FileHeader fileHeader = project.getFileHeader();
		try {
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss");
			// odd way?
			XMLGregorianCalendar date = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(
							dateTimeFormat.format(Calendar.getInstance()
									.getTime()));

			fileHeader.setCreationDateTime(date);
		} catch (Exception e) {

			log.error("Failed to add time information to project file header.");

		}

	}

	private static void addAuthorInformation(org.plcopen.xml.tc6.Project project) {

	}

}
