package java_cup.myParser;

import java.beans.Statement;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.mysql.jdbc.PreparedStatement;
import java.sql.*;
import java_cup.internal_error;  

public class DataBase {
	 private static DataBase instance = null;
	
	 protected DataBase(){
		 //Exists only to defeat instantiation.
		  try {
			this.initDB();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	 }
	 
	 public static DataBase getInstance() {
	      if(instance == null) {
	         instance = new DataBase();
	      }
	      return instance;
	   }
	 
	  // JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
 
	   //  Database credentials
	   String USER     = "";
	   String PASS     = "";
	   String DBSERVER = "";
	   String DBNAME   = "";
	   String DBPORT   = "";
	   
	  static Connection connection = null;
	   
	 //Initialize the connection only ONCE. A singelton pattern
	public void initDB() throws SQLException {
		if(this.connection==null) {			
			try {
				this.getDBCredentials();
				this.connection = this.connection(this.USER, this.PASS, this.DBSERVER,this.DBNAME,this.DBPORT);	
			} catch (IOException | ParseException e) {e.printStackTrace();}
		}		
	}
	private Connection getConnection() {
		return this.connection;
	}
	public void getDBCredentials() throws FileNotFoundException, IOException, ParseException {		
		String json_mapper     = Constant.dbConfigFile;
		JSONParser parser  	   = new JSONParser();
        JSONObject jsonObject  = (JSONObject) parser.parse(new FileReader(json_mapper));      
        
        this.USER     = (String) jsonObject.get("user");
        this.PASS     = (String) jsonObject.get("pass");
        this.DBSERVER = (String) jsonObject.get("dbserver");
        this.DBNAME   = (String) jsonObject.get("dbname");
        this.DBPORT   = (String) jsonObject.get("dbport");
	}
	
	public boolean isElementUnique(String query) throws SQLException {
		DataBase db  = this.getInstance();
		ResultSet rs = db.executeSelect(query);		
		
		try {
			if(rs!=null){
				if(!rs.absolute(1)){
					return true;
				}
			}
		} catch (SQLException e){}	
		return false;
	}

	//returns only the agency with certainn ID
	public boolean getAgencyByEmail(String email) throws SQLException {
	    String query = "Select * from parser_Agency where email = '"+email+"'";
		ResultSet rs = this.executeSelect(query);
		
		if(rs.next()) {
			return true;
		}
		return false;		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	//Update register
	public int executeInsert( String query) {
		PreparedStatement pstmt;
		Log log = new Log();
		try {
			pstmt = (PreparedStatement) this.connection.prepareStatement(query);
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("\n==================================================================================");
			System.out.println("An exception has occurred: ");
			System.out.println("Query: "+query+"\n");
			System.out.println("********* Exception *********");
			e.printStackTrace();
			System.out.println("********* End Exception *********");
			System.out.println("==================================================================================\n");
		
			log.writeLog("\n==================================================================================\n");
			log.writeLog("An exception has occurred: "+"\n");
			log.writeLog("Query: "+query+"\n");
			log.writeLog("********* Exception *********"+"\n");
			log.writeLog("Trace: "+ e.getMessage()+"\n");
			log.writeLog("********* End Exception *********"+"\n");
			log.writeLog("==================================================================================\n");		
			
			log.exceptionsLog("\n==================================================================================\n");
			log.exceptionsLog("An exception has occurred: "+"\n");
			log.exceptionsLog("Query: "+query+"\n");
			log.exceptionsLog("********* Exception *********"+"\n");
			log.exceptionsLog("Trace: "+ e.getMessage()+"\n");
			log.exceptionsLog("********* End Exception *********"+"\n");
			log.exceptionsLog("==================================================================================\n");			
		}
		return 0;
	}
	
	public void executeMultyInsert(ArrayList<String> queries) throws SQLException {
		Connection con = this.connection;
		
		java.sql.Statement stmt = con.createStatement();	
		 for(int i=0;i<queries.size();i++){	 
			stmt.addBatch(queries.get(i));
		}
		stmt.executeBatch();
		stmt.close();				
	}
	
	public int executeUpdate( String query) {
		PreparedStatement pstmt;
		Log log = new Log();
		
		try {
			pstmt = (PreparedStatement) this.connection.prepareStatement(query);
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			log.writeLog("Error Message: "+e.getMessage()+"\n");
			log.exceptionsLog("Error Message: "+e.getMessage()+"\n");
			e.printStackTrace();
		}
		return 0;
	}
	
	//Read(Select)
	public ResultSet executeSelect( String query) {
		Connection con = this.connection;
		ResultSet rs   = null;
		Log log = new Log();
		
		try {
			PreparedStatement stmt = (PreparedStatement) con.prepareStatement(query);
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
		} catch (Exception e){
			log.writeLog("Error Message: "+e.getMessage()+"\n");
		}
		return rs;
	}
	
	//Establish conection with the database
	public Connection connection(String user, String pass, String server, String database, String port) {		
		 Connection con = null;
		 Log log = new Log();
		 String sURL    = "jdbc:mysql://"+server+":"+port+"/"+database;
		 System.out.print("Connecting to: "+sURL+"... \t");
		 log.writeLog("=== Connecting to: "+sURL+"... \t");
		 
		 try {
			con = DriverManager.getConnection(sURL,user,pass);
		    System.out.println("Connection established!");
			log.writeLog("Connection established! === \n");
		} catch (SQLException e) {
			 System.out.println("Connection has failed: "+e.getMessage());
			 log.writeLog("Connection has failed: "+e.getMessage()+"\n");
			 e.printStackTrace();
		} 
		return con;
	}
///////////////////////////////////////////////////////////////////////////////////////////////
}


