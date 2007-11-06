package org.supremica.external.processeditor.tools.db;

import java.util.*;
import java.io.*;
import java.sql.*;
import javax.xml.bind.*;
import javax.xml.transform.stream.*;
import org.supremica.external.processeditor.xml.*;


public class Connect {
	private final String PKGS = "org.supremica.manufacturingTables.xsd.processeditor";
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
	--< Constructors >--
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
	--< Methods >--
*/
	
	// Make URL-string

	private String getConnectionUrl(){
		return url+serverName+":"+portNumber+";databaseName="+databaseName+";selectMethod="+selectMethod+";";
	}

	// Establish connection by using the DriverManager class	
	
	private java.sql.Connection getConnection(){
		try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
			con = java.sql.DriverManager.getConnection(getConnectionUrl(),userName,password);
		}catch(Exception e){
			e.printStackTrace();
			BaseWindow.getPrintArea().append("\nError Trace in getConnection() : " + e.getMessage());
		}
		return con;
	}
	
	// Close local connection
	
	private void closeConnection(){
		try{
			if(con != null)
				con.close();
			con = null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//	Display the driver properties, database details 
	
	public void displayDbProperties(){
		java.sql.DatabaseMetaData dm = null;
		java.sql.ResultSet rs = null;
		try{
			con = this.getConnection();
			if(con != null){
				dm = con.getMetaData();
				BaseWindow.getPrintArea().append("\nDriver Information");
				BaseWindow.getPrintArea().append("\n\tDriver Name: "+ dm.getDriverName());
				BaseWindow.getPrintArea().append("\n\tDriver Version: "+ dm.getDriverVersion ());
				BaseWindow.getPrintArea().append("\n\nDatabase Information ");
				BaseWindow.getPrintArea().append("\n\tDatabase Name: "+ dm.getDatabaseProductName());
				BaseWindow.getPrintArea().append("\n\tDatabase Version: "+ dm.getDatabaseProductVersion());
				BaseWindow.getPrintArea().append("\nAvalilable Catalogs ");
				rs = dm.getCatalogs();
				while(rs.next()){
					BaseWindow.getPrintArea().append("\n\tcatalog: " + rs.getString(1));
				}
				BaseWindow.getPrintArea().append("\n");
				rs.close();
				rs = null;
				closeConnection();
			}
			else 
				BaseWindow.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: No active Connection" + e.getMessage());
			closeConnection();
		}
		dm = null;
	}
	
	//	Get all projects
	public Vector getAllProjects() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector projects = new Vector();
		try {
			con = this.getConnection();
			if(con != null){	
				ps = con.prepareStatement("SELECT Project_name FROM Projects");
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
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
				BaseWindow.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return projects;
	}
	
	//	Get project ID

	public int getProjectID(String projectName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int projectID = 0;
		try {
			con = this.getConnection();
			if(con != null){	
				ps = con.prepareStatement("exec GetProjectID @projectName = ?");
				ps.setString(1, projectName);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
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
				BaseWindow.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return projectID;
	}
	
	//	Delete project

	public void deleteProject(int projectID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			if(con != null){	
				ps = con.prepareStatement("DELETE FROM Projects WHERE Projects.Project_ID = ? SELECT 1");
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
				}
				
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
				
			}
			else 
				BaseWindow.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
	}
	
	//	Get all ROPs

	public Vector getAllROPs(int projectID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Vector ROPs = new Vector();
		try {
			con = this.getConnection();
			if(con != null){	
				ps = con.prepareStatement("SELECT ROP_name_ID, ROP_type, Comment FROM ROPs WHERE ROPs.Project_ID = ?");
				ps.setInt(1,projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
				}
				while (rs.next()) {
					ROPs.addElement(rs.getString(1));
				}
				
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else 
				BaseWindow.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
		return ROPs;
	}
	
	//	Delete ROP

	public void deleteROP(int projectID, String ROPNameID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			if(con != null){	
				ps = con.prepareStatement("DELETE FROM ROPs WHERE ROPs.Project_ID = ? AND ROPs.ROP_name_ID = ? SELECT 1");
				ps.setInt(1, projectID);
				ps.setString(2, ROPNameID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
				}
				
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
				
			}
			else 
				BaseWindow.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: " + e.getMessage());
			closeConnection();
		}
	}
	
	//	Get the ROP XML-file from DB and write to a specified directory (to file)

	public Object getROPXML(int projectID, String ROPNameID, String filePath) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String xmlStr = "";
		try{
			con = this.getConnection();
			if(con != null){
				ps = con.prepareStatement("declare @xml xml exec @xml = ExtractROPXML @projectID = ?, @ropNameID = ? select @xml");
				ps.setInt(1, projectID);
				ps.setString(2, ROPNameID);
				
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					xmlStr = rs.getString(1);	// Hela XML-filen som en sträng
					try {
						//BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
						//out.write(xmlStr);
						//out.close();
						Loader ldr = new Loader();
						Object o = ldr.open(xmlStr);

						JAXBContext jc = JAXBContext.newInstance(PKGS);
						Marshaller m = jc.createMarshaller();
						
						StringWriter stringWriter = new StringWriter();
						StreamResult result = new StreamResult( stringWriter );
						m.marshal( o, result );
						String content = stringWriter.toString();
						
						System.out.println(content);
						BaseWindow.getPrintArea().append("\nExtract complete (System.out.println())");
						
						return o;
						
					}catch (Exception e) {
						if (e.getMessage() == null) {
							BaseWindow.getPrintArea().append("\nError writing to file. The string is empty. Wrong projectID or ROPname?");
						}
						else {
							BaseWindow.getPrintArea().append("\nError writing to file: " + e.getMessage());
						}
						closeConnection();	
					}
				}
				
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else 
				BaseWindow.getPrintArea().append("\nError: No active Connection ");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: No active Connection: " + e.getMessage());
			closeConnection();
		}
		
		return null;
	}

	//	Insert XML string into DB

	public void setROPXML(int projectID, String ROPXML) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String resultStr = "No ResultSet returned";
		try {
			con = this.getConnection();
			if(con != null) {
				ps = con.prepareStatement("exec InsertROPXML @projectID = ?, @xml = ?");
				ps.setInt(1, projectID);
				ps.setString(2, ROPXML);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle) {
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
					closeConnection();
				}
				while(rs.next()) {
					resultStr = rs.getString(1);
				}
				BaseWindow.getPrintArea().append("\nROP ID: " + resultStr);

				ps.close();
				rs.close();
				rs = null;
				closeConnection();
			}
			else 
				BaseWindow.getPrintArea().append("\nError: No active Connection ");
		}catch(Exception e) {
			BaseWindow.getPrintArea().append("\nError: " + e.getMessage());
		}
	}
}