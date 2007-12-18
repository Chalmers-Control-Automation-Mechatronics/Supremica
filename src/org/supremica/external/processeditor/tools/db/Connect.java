package org.supremica.external.processeditor.tools.db;

import java.util.*;
import java.io.*;
import java.sql.*;
import javax.xml.bind.*;
import javax.xml.transform.stream.*;

import org.supremica.manufacturingTables.xsd.pr.*;
import org.supremica.manufacturingTables.xsd.vr.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.manufacturingTables.xsd.eop.*;
import org.supremica.manufacturingTables.xsd.il.*;

import org.supremica.external.processeditor.xml.Loader;


/**
 * This class is used to create connection setup objects to the
 * MS SQL Server 2005. A number of methods sending predefined
 * queries to the database are also defined
 *
 */
public class Connect {

/*
	------------------
	--< Initialize >--
	------------------
*/
	private final String PKGS_PR = "org.supremica.manufacturingTables.xsd.pr";
	private final String PKGS_VR = "org.supremica.manufacturingTables.xsd.vr";
	private final String PKGS_ROP = "org.supremica.manufacturingTables.xsd.processeditor";
	private final String PKGS_EOP = "org.supremica.manufacturingTables.xsd.eop";
	private final String PKGS_IL = "org.supremica.manufacturingTables.xsd.il";
	private java.sql.Connection con = null;
	private String url;
	private String serverName;
	private String portNumber;
	private String databaseName;
	private String userName;
	private String password;
	// Informs the driver if a server side cursor will be used,
	// which permits more than one active statement
	// on a connection.
	private String selectMethod;

/*
	--------------------
	--< Constructors >--
	--------------------
*/
	/**
	 * Creates a predefined connection setup object
	 */
	public Connect() {
		this.url = "jdbc:sqlserver://";
		this.serverName = "127.0.0.1";
		this.portNumber = "1434";
		this.databaseName = "ProductionControlDB";
		this.userName = "test123";
		this.password = "test123";
		this.selectMethod = "direct";
	}
	/**
	 * Creates a connection setup object with a number of parameters
	 *
	 * @param	url	the server URL
	 * @param	serverName	the server name
	 * @param	portNumber	the port number
	 * @param	databaseName	the database name
	 * @param	userName	the user name
	 * @param	password	the user password
	 * @param	selectMethod	the method used to retrieve result sets, can be set to either cursor or direct
	 */
	public Connect(String url, String serverName, String portNumber, String databaseName,
						String userName, String password, String selectMethod){
		this.url = url;
		this.serverName = serverName;
		this.portNumber = portNumber;
		this.databaseName = databaseName;
		this.userName = userName;
		this.password = password;
		this.selectMethod = selectMethod;
	}

/*
	---------------
	--< Methods >--
	---------------
*/
	// Make URL-string
	/**
	 * Returns the connection URL
	 * @return		the connection URL
	 */
	private String getConnectionUrl(){
		return url+serverName+":"+portNumber+";databaseName="+databaseName+";selectMethod="+selectMethod+";";
	}

	// Get connection
	/**
	 * Establishes a connection by using the DriverManager class
	 * @return		the connection object
	 */
	private java.sql.Connection getConnection(){
		try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = java.sql.DriverManager.getConnection(getConnectionUrl(),userName,password);
		}catch(Exception e){
			//e.printStackTrace();
			DBInterface.getPrintArea().append("\nError Trace in getConnection() : " + e.getMessage());
		}
		return con;
	}

	// Is connected
	/**
	 * Returns true if a connection object is defined, otherwise
	 * false
	 * @return		true if a connection object is defined
	 */
	public boolean isConnected(){
		try{
			con = this.getConnection();
			if (con != null)
				return true;
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: No active Connection" + e.getMessage());
			closeConnection();
		}
		return false;
	}

	// Close local connection
	/**
	 * Closes the current active connection.
	 */
	private void closeConnection(){
		try{
			if(con != null)
				con.close();
			con = null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// Display database properties
	/**
	 * 	Displays the driver properties, the database details
	 */
	public void displayDbProperties(){
		java.sql.DatabaseMetaData dm = null;
		java.sql.ResultSet rs = null;
		try{
			if(isConnected()){
				dm = con.getMetaData();
				DBInterface.getPrintArea().append("\nDriver Information");
				DBInterface.getPrintArea().append("\n\tDriver Name: "+ dm.getDriverName());
				DBInterface.getPrintArea().append("\n\tDriver Version: "+ dm.getDriverVersion ());
				DBInterface.getPrintArea().append("\n\nDatabase Information ");
				DBInterface.getPrintArea().append("\n\tDatabase Name: "+ dm.getDatabaseProductName());
				DBInterface.getPrintArea().append("\n\tDatabase Version: "+ dm.getDatabaseProductVersion());
				DBInterface.getPrintArea().append("\nAvalilable Catalogs ");
				rs = dm.getCatalogs();
				while(rs.next()){
					DBInterface.getPrintArea().append("\n\tcatalog: " + rs.getString(1));
				}
				DBInterface.getPrintArea().append("\n");
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: No active Connection " + e.getMessage());
			closeConnection();
		}
		dm = null;
	}

	//	Get all projects
	/**
	 * Returns a vector with all the projects in the database
	 * @return	a vector with all database projects
	 */
	public Vector<String> getAllProjects() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector<String> projects = new Vector<String>();
		try {
			if(isConnected()){
				ps = con.prepareStatement("SELECT Project_name FROM Projects");
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				while (rs.next()) {
					projects.addElement(rs.getString(1));
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return projects;
	}

	//	Get project ID
	/**
	 * Returns the database ID of a project by using the project name
	 * @param	projectName	the project name
	 * @return	the database ID of a project as INT
	 */
	public int getProjectID(String projectName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int projectID = 0;
		try {
			if(isConnected()){
				ps = con.prepareStatement("exec GetProjectID @projectName = ?");
				ps.setString(1, projectName);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				if (rs.next()) {
					projectID = rs.getInt(1);	// The project ID
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return projectID;
	}

	//****** IMPORT XML ******

	//	Import project XML as String
	/**
	 * Import a project from the database as an XML String
	 * @param	projectName	the project name
	 * @return	the project as a String object
	 */
	public String getProjectXMLAsString(String projectName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String xmlStr = "";
		try {
			if(isConnected()){
				ps = con.prepareStatement("declare @xml XML exec @xml = ExtractProjectXML @projectName = ? select @xml");
				ps.setString(1, projectName);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					xmlStr = rs.getString(1);	// The complete XML as a String object
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();

				return xmlStr;
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return xmlStr;
	}

	//****** EXPORT XML ******

	//	Export project XML as String
	/**
	 * Returns the database ID of a project by using the project name
	 * @param	projectName	the project name
	 * @return	the database ID of a project as INT
	 */
	public void setProjectXMLFromString(String xmlStr) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String resultSet = "\nNo resultSet returned!";
		try {
			if(isConnected()){
				ps = con.prepareStatement("exec InsertProjectXML @xml = ?");
				ps.setString(1, xmlStr);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					resultSet = rs.getString(1);	// The complete XML as a String object
				}
				DBInterface.getPrintArea().append("\nInserted as ID: " + resultSet);
				ps.close();
				rs.close();
				rs = null;
				closeConnection();

			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
	}

	//	Send query
	/**
	 * Sends a predefined query to the database
	 * @param	query	the predefined query as String
	 */
	public void sendQuery(String query) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = "";
		try {
			if(isConnected()){
				ps = con.prepareStatement(query);//"exec GetProjectID @projectName = ?");
				//ps.setString(1, projectName);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				while (rs.next()) {
					result = result + rs.getString(1) + "\t\t" + rs.getString(2) + "\n";
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
				DBInterface.getPrintArea().append("\n" + result);
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
	}

	// Link project
	/**
	 * Sends a predefined query to the database
	 * @param	query	the predefined query as String
	 */
	public String linkProject(int projectID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = "";
		try {
			if(isConnected()){
				ps = con.prepareStatement("exec CreateLinks @projectID = ?");
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				while (rs.next()) {
					result = result + rs.getString(2) + "\t" + rs.getString(3) + "\t" + rs.getString(4) + "\n";
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
				DBInterface.getPrintArea().append("\n" + result);
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return result;
	}

	// Get standards in use
	/**
	 * Returns an ArrayList object defining what standards are present in a specific project
	 * @param	projectID	the project database ID
	 * @return	an ArrayList object
	 */
	public ArrayList<Integer> getStandardsInUse(int projectID){
		ArrayList<Integer> standardsInUse = new ArrayList<Integer>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if(isConnected()){
				ps = con.prepareStatement("exec GetStandardsInUse @projectID = ?");
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					standardsInUse.add(rs.getInt(1));
					standardsInUse.add(rs.getInt(2));
					standardsInUse.add(rs.getInt(3));
					standardsInUse.add(rs.getInt(4));
					standardsInUse.add(rs.getInt(5));
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return standardsInUse;
	}

	//	Delete project
	/**
	 * Permanently deletes a project from the database
	 * @param	projectID	the project database ID
	 */
	public void deleteProject(int projectID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if(isConnected()){
				ps = con.prepareStatement("DELETE FROM Projects WHERE Projects.Project_ID = ? SELECT 1");
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}

				ps.close();
				rs.close();
				rs = null;
				closeConnection();

			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
	}

	//------------------------------

	//	Get all standards
	/**
	 * Returns a Vector object containing all instances of a specific standard in a specific
	 * project
	 * @param	projectID	the project database ID
	 * @param	standardIndex	the selected standard type
	 * @return	a Vector object with all the instances of the standard type
	 */
	public ArrayList<String> getAllStandards(int projectID, int standardIndex) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		try {
			if(isConnected()){
				switch (standardIndex) {
				case 0: ps = con.prepareStatement("SELECT Cell_name FROM PR_cells c INNER JOIN PR_areas a ON c.Area_ID = a.Area_ID INNER JOIN Physical_resources p ON a.Factory_ID = p.Factory_ID WHERE p.Project_ID = ?"); break;
				case 1: ps = con.prepareStatement("SELECT Cell_name FROM Virtual_resources v WHERE v.Project_ID = ?"); break;
				case 2: ps = con.prepareStatement("SELECT ROP_name_ID FROM ROPs r WHERE r.Project_ID = ?"); break;
				case 3: ps = con.prepareStatement("SELECT Operation_name_ID FROM Operations o WHERE o.Project_ID = ?"); break;
				case 4: ps = con.prepareStatement("SELECT IL_name_ID FROM ILs i WHERE i.Project_ID = ?"); break;
				}
				ps.setInt(1,projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				while (rs.next()) {
					list.add(rs.getString(1));
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return list;
	}

	//	Delete standard
	/**
	 * Deletes a specific standard in the database
	 * @param	projectID	the project database ID
	 * @param	standardIndex	the selected standard type
	 * @param	standardNameID	the selected instance of the standard type
	 */
	public void deleteStandard(int projectID, int standardIndex, String standardNameID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if(isConnected()){
				switch (standardIndex) {
				case 0: ps = con.prepareStatement("DELETE FROM Physical_resources WHERE Physical_resources.Project_ID = ? SELECT 1"); break;
				case 1: ps = con.prepareStatement("DELETE FROM Virtual_resource WHERE Virtual_resource.Project_ID = ? SELECT 1"); break;
				case 2: ps = con.prepareStatement("DELETE FROM ROPs WHERE ROPs.Project_ID = ? AND ROPs.ROP_name_ID = ? SELECT 1");
						ps.setString(2, standardNameID); break;
				case 3: ps = con.prepareStatement("DELETE FROM Operations WHERE Operations.Project_ID = ? AND Operations.Operation_name_ID = ? SELECT 1");
						ps.setString(2, standardNameID); break;
				case 4: ps = con.prepareStatement("DELETE FROM ILs WHERE ILs.Project_ID = ? AND ILs.IL_name_ID = ? SELECT 1");
						ps.setString(2, standardNameID); break;
				}
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					DBInterface.getPrintArea().append("\nID: " + standardNameID + " deleted..");
				}
				else
					DBInterface.getPrintArea().append("\nCould not delete " + standardNameID);
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
	}

	//****** IMPORT XML ******

	//	Import standard from DB as String
	/**
	 * Returns a specific standard from the database as a String object
	 * @param	projectID	the project database ID
	 * @param	standardIndex	the selected standard type
	 * @param	standardNameID	the selected instance of the standard type
	 * @return	the selected standard as an XML String
	 */
	public String getStandardXMLAsString(int projectID, int standardIndex, String standardNameID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String xmlStr = "No ResultSet returned ";
		try{
			if(isConnected()){
				switch(standardIndex) {
				case 0: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractPRXML @projectID = ? select @xml"); break;
				case 1: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractVRXML @projectID = ? select @xml"); break;
				case 2: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractROPXML @projectID = ?, @ROPNameID = ? select @xml");
						ps.setString(2, standardNameID); break;
				case 3: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractEOPXML @projectID = ?, @OPNameID = ? select @xml");
						ps.setString(2, standardNameID); break;
				case 4: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractILXML @projectID = ?, @ILNameID = ? select @xml");
						ps.setString(2, standardNameID); break;
				}
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					xmlStr = rs.getString(1);	// The complete XML as a String object
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();

				return xmlStr;
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection ");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: No active Connection: " + e.getMessage());
			closeConnection();
		}
		return null;
	}

	//	Import standard from DB as JAXB Object
	/**
	 * Returns a specific standard from the database as a JAXB object
	 * @param	projectID	the project database ID
	 * @param	standardIndex	the selected standard type
	 * @param	standardNameID	the selected instance of the standard type
	 * @return	the selected standard as a JAXB object
	 */
	public Object getStandardXMLAsObject(int projectID, int standardIndex, String standardNameID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Object o = null;
		try{
			if(isConnected()){
				switch(standardIndex) {
				case 0: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractPRXML @projectID = ? select @xml"); break;
				case 1: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractVRXML @projectID = ? select @xml"); break;
				case 2: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractROPXML @projectID = ?, @ROPNameID = ? select @xml");
						ps.setString(2, standardNameID); break;
				case 3: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractEOPXML @projectID = ?, @OPNameID = ? select @xml");
						ps.setString(2, standardNameID); break;
				case 4: ps = con.prepareStatement("declare @xml XML exec @xml = ExtractILXML @projectID = ?, @ILNameID = ? select @xml");
						ps.setString(2, standardNameID); break;
				}
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					String xmlStr = rs.getString(1);	// The complete XML file as a String object
					Loader ldr = new Loader();
					JAXBContext jc = null;
					Marshaller m = null;
					StringWriter stringWriter = new StringWriter();
					StreamResult result = new StreamResult(stringWriter);
					String content = "";
					try {
						switch(standardIndex){
						case 0: o = ldr.open(xmlStr, PKGS_PR);
								jc = JAXBContext.newInstance(PKGS_PR);
								m = jc.createMarshaller();
								m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
								m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								m.marshal(o, result);
								content = stringWriter.toString();
								DBInterface.getPrintArea().append("\n\n" + content); break;
						case 1: o = ldr.open(xmlStr, PKGS_VR);
								jc = JAXBContext.newInstance(PKGS_VR);
								m = jc.createMarshaller();
								m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
								m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								m.marshal(o, result);
								content = stringWriter.toString();
								DBInterface.getPrintArea().append("\n\n" + content); break;
						case 2: o = ldr.open(xmlStr, PKGS_ROP);
								jc = JAXBContext.newInstance(PKGS_ROP);
								m = jc.createMarshaller();
								m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
								m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								m.marshal(o, result);
								content = stringWriter.toString();
								DBInterface.getPrintArea().append("\n\n" + content); break;
						case 3: o = ldr.open(xmlStr, PKGS_EOP);
								jc = JAXBContext.newInstance(PKGS_EOP);
								m = jc.createMarshaller();
								m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
								m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								m.marshal(o, result);
								content = stringWriter.toString();
								DBInterface.getPrintArea().append("\n\n" + content); break;
						case 4: o = ldr.open(xmlStr, PKGS_IL);
								jc = JAXBContext.newInstance(PKGS_IL);
								m = jc.createMarshaller();
								m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
								m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								m.marshal(o, result);
								content = stringWriter.toString();
								DBInterface.getPrintArea().append("\n\n" + content); break;
						}
					}catch(Exception e) {
						DBInterface.getPrintArea().append("Exception: " + e.getMessage());
					}
				}
				ps.close();
				rs.close();
				rs = null;
				closeConnection();

				return o;
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection ");
		}catch(Exception e){
			DBInterface.getPrintArea().append("\nError: No active Connection: " + e.getMessage());
			closeConnection();
		}
		return null;
	}

	//****** EXPORT XML ******

	//	Export standard to DB from String
	/**
	 * Exports a selected standard to the database from a String object
	 * @param	projectID	the project database ID
	 * @param	standardIndex	the selected standard type
	 * @param	xmlStr	the XML data as a String object
	 */
	public void setStandardXMLFromString(int projectID, int standardIndex, String xmlStr) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String resultStr = "No ResultSet returned";
		String insertStr = "\nInserted as ID: ";

		try {
			if(isConnected()) {
				Loader ldr = new Loader();
				JAXBContext jc = null;
				Marshaller m = null;
				StringWriter stringWriter = new StringWriter();
				StreamResult result = new StreamResult(stringWriter);
				String content = "";
				Object o = null;
				boolean instanceFlag = false;
				String instanceType = "JAXB Class";
				try {
					switch(standardIndex){
					case 0: o = ldr.open(xmlStr, PKGS_PR);
							instanceType = "Physical resources";
							if (o instanceof Factory)
								instanceFlag = true;
							jc = JAXBContext.newInstance(PKGS_PR);
							m = jc.createMarshaller();
							m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
							m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
							m.marshal(o, result);
							content = stringWriter.toString(); break;
					case 1: o = ldr.open(xmlStr, PKGS_VR);
							instanceType = "Virtual resources";
							if (o instanceof VirtualResources)
								instanceFlag = true;
							jc = JAXBContext.newInstance(PKGS_VR);
							m = jc.createMarshaller();
							m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
							m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
							m.marshal(o, result);
							content = stringWriter.toString(); break;
					case 2: o = ldr.open(xmlStr, PKGS_ROP);
							instanceType = "ROP";
							if (o instanceof ROP)
								instanceFlag = true;
							jc = JAXBContext.newInstance(PKGS_ROP);
							m = jc.createMarshaller();
							m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
							m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
							m.marshal(o, result);
							content = stringWriter.toString(); break;
					case 3: o = ldr.open(xmlStr, PKGS_EOP);
							instanceType = "Operation";
							if (o instanceof Operation)
								instanceFlag = true;
							jc = JAXBContext.newInstance(PKGS_EOP);
							m = jc.createMarshaller();
							m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
							m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
							m.marshal(o, result);
							content = stringWriter.toString(); break;
					case 4: o = ldr.open(xmlStr, PKGS_IL);
							instanceType = "IL";
							if (o instanceof IL)
								instanceFlag = true;
							jc = JAXBContext.newInstance(PKGS_IL);
							m = jc.createMarshaller();
							m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
							m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
							m.marshal(o, result);
							content = stringWriter.toString(); break;
					}
				}catch(Exception e) {
					DBInterface.getPrintArea().append("\nError: " + e.getMessage() + "\nWrong encoding? Not validated?");
				}
				if (instanceFlag) {
					switch (standardIndex) {
					case 0: ps = con.prepareStatement("exec InsertPRXML @projectID = ?, @xml = ?");break;
					case 1: ps = con.prepareStatement("exec InsertVRXML @projectID = ?, @xml = ?");break;
					case 2: ps = con.prepareStatement("exec InsertROPXML @projectID = ?, @xml = ?");break;
					case 3: ps = con.prepareStatement("exec InsertEOPXML @projectID = ?, @xml = ?");break;
					case 4: ps = con.prepareStatement("exec InsertILXML @projectID = ?, @xml = ?");break;
					}
					ps.setInt(1, projectID);
					ps.setString(2, content);
					try {
						rs = ps.executeQuery();
					}catch(SQLException sqle) {
						DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
						closeConnection();
						insertStr = "";
					}
					if (rs != null) {
						rs.next();
						resultStr = rs.getString(1);	// The complete XML as a String object
					}
					DBInterface.getPrintArea().append(insertStr + resultStr);
				}
				else
					DBInterface.getPrintArea().append("\nNot an instance of " + instanceType + "!");

				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection ");
		}catch(Exception e) {
			DBInterface.getPrintArea().append("\nError: " + e.getMessage());
		}
	}

	//	Export standard to DB from JAXB Object
	/**
	 * Exports a selected standard to the database from a JAXB object
	 * @param	projectID	the project database ID
	 * @param	standardIndex	the selected standard type
	 * @param	o	the XML data as a JAXB object
	 */
	public void setStandardXMLFromObject(int projectID, int standardIndex, Object o) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String xmlStr = "";
		String resultStr = "No ResultSet returned";
		String insertStr = "\nInserted as ID: ";
		try {
			if(isConnected()) {
				JAXBContext jc = null;
				switch(standardIndex){
				case 0: jc = JAXBContext.newInstance(PKGS_PR); break;
				case 1: jc = JAXBContext.newInstance(PKGS_VR); break;
				case 2: jc = JAXBContext.newInstance(PKGS_ROP); break;
				case 3: jc = JAXBContext.newInstance(PKGS_EOP); break;
				case 4: jc = JAXBContext.newInstance(PKGS_IL); break;
				}
				Marshaller m = jc.createMarshaller();
				m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				StringWriter stringWriter = new StringWriter();
				StreamResult result = new StreamResult(stringWriter);
				m.marshal(o, result);
				xmlStr = stringWriter.toString();

				switch(standardIndex){
				case 0: ps = con.prepareStatement("exec InsertPRXML @projectID = ?, @xml = ?"); break;
				case 1: ps = con.prepareStatement("exec InsertVRXML @projectID = ?, @xml = ?"); break;
				case 2: ps = con.prepareStatement("exec InsertROPXML @projectID = ?, @xml = ?"); break;
				case 3: ps = con.prepareStatement("exec InsertEOPXML @projectID = ?, @xml = ?"); break;
				case 4: ps = con.prepareStatement("exec InsertILXML @projectID = ?, @xml = ?"); break;
				}
				ps.setInt(1, projectID);
				ps.setString(2, xmlStr);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle) {
					DBInterface.getPrintArea().append("\nAn SQL Exception Occurred! " + sqle.getMessage());
					closeConnection();
					insertStr = "";
				}
				while(rs.next()) {
					resultStr = rs.getString(1);
				}
				DBInterface.getPrintArea().append(insertStr + resultStr);
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else
				DBInterface.getPrintArea().append("\nError: No active Connection ");
		}catch(Exception e) {
			DBInterface.getPrintArea().append("\nError: A resource must be selected in SOC. " + e.getMessage());
		}
	}
}