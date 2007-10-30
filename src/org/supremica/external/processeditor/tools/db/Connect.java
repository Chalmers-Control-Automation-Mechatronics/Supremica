package org.supremica.external.processeditor.tools.db;

import java.lang.*;
import javax.swing.*;
import java.io.*;
import java.sql.*;
import java.sql.Connection.*;
import java.sql.DriverManager.*;
import javax.sql.*;
import javax.xml.stream.*; 

public class Connect {
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
		this.selectMethod = "cursor";
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
			if(con != null) {
				BaseWindow.getPrintArea().append("\nConnection Successful!");
			}
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
				if (rs != null) {
					rs.next();
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
			BaseWindow.getPrintArea().append("\nError: No active Connection: " + e.getMessage());
			closeConnection();
		}
		return projectID;
	}
	
	//	Check project ID

	public int checkProjectID(int projectID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			if(con != null){	
				ps = con.prepareStatement("select Project_ID from Projects WHERE Projects.Project_ID = ?");
				ps.setInt(1, projectID);
				try {
					rs = ps.executeQuery();
				}catch(SQLException sqle){
					BaseWindow.getPrintArea().append("\nAn SQL Exception Occured! " + sqle.getMessage());
				}
				if (rs != null) {
					rs.next();
					if (rs.getString(1).isEmpty())
						projectID = 0;
				}
				
				ps.close();
				rs.close();
				rs = null;
				closeConnection();
				
			}
			else 
				BaseWindow.getPrintArea().append("\nError: No active Connection");
		}catch(Exception e){
			BaseWindow.getPrintArea().append("\nError: No active Connection: " + e.getMessage());
			closeConnection();
		}
		return projectID;
	}

	//	Get the ROP XML-file from DB and write to a specified directory (to file)

	public void getROPXML(int projectID, String ROPNameID, String filePath) {
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
					xmlStr = rs.getString(1);	// Hela XML-filen som en sträng!!
					try {
						BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
						out.write(xmlStr);
						out.close();
						BaseWindow.getPrintArea().append("\nExtract complete");
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
	}

	//	Insert XML string into DB

	public void setROPXML(int projectID, String ROPXML) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String resultStr = "";
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
				if (rs != null) {
					rs.next();
					resultStr = rs.getString(1);
					BaseWindow.getPrintArea().append("\nROP ID: " + resultStr);
				}
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