package java_cup.myParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.omg.CORBA.portable.OutputStream;

public class User {

	private static String query = "insert into parser_Users (id,crm_id, user_ext_id, name, email, tel, webpage,feed_url,dni,provincia,localidad,tipo_via,nombre_via,numero_via,cp,country) values ";
			
	private String id_ext;	
	private String id;
	private int crm_id;
	private int user_ext_id;
	private String xml_ads_path="not available";
	private String name="not available";
	private String country="not available";
	private String email="not available";
	private String phone="not available";
	private String webpage = "not available";
	private String xml_feeds_path="";
	
	private String provincia="not available",
			localidad="not available",
			tipo_via="not available",
			nombre_via="not available",
			numero_via="not available",
			cp="not available",dni="not available";
	
	public User(){}
	
	static int j = 0;
	
	public User(ArrayList<String> tagList, int crm_id, Map<String,String> CRM){		
		String[] tokens;
		String tag_name  = "";
		String tag_value = "";		
		this.crm_id = crm_id;		
		
		if(CRM.get("xml_path").equals("none")){
			this.xml_feeds_path = CRM.get("xml_feeds");
  		}
 
		for(int i=j;i<tagList.size();i++){
			 j++;
			 //Split the attributs: tag name, tag value
			 tokens   = tagList.get(i).split("___");				
			 tag_name = tokens[0];
			 
			 if(tokens.length>1){
				 tag_value = tokens[1]; 
			 }
		
			 if(!tag_value.contains("\\'")){
				 tag_value = tag_value.replaceAll("'", "''");
			 }

			 //Normalize the id of the property. The id only allows integer numbers.
			 if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("id")){
				tag_value = tag_value.replace("inmo_", "");
			/*	if(tag_value.startsWith(""))
				   tag_value.substring(2, tag_value.length());
				*/	
				this.user_ext_id = Integer.valueOf(tag_value);	
				this.id =Constant.id_prefix+this.crm_id+this.user_ext_id;					
		    }
			else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("name")){
	      		name = tag_value;
	       	}		//Get the name of the Agency
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("country")){
	    		country = tag_value;
	       	}		      
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("email")){		    		
	      		email = tag_value;
	       	}	
	      	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("phone")){
	      		phone = tag_value;
	       	}			      	
	      	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("xml_path")){
	      		xml_feeds_path = tag_value;		
	      		System.out.println(xml_feeds_path);
	       	}  
	    	//For each webpage, we launch a new scan, looking for adverts
	      	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("web_page")){ 	  
	      		webpage = tag_value;
	      	}
			//////////////////////////////////////////////////////////////////////////////////
			 
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("dni")){ 	  
	      		dni = tag_value;
	      	}	
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("provincia")){ 	  
	    		provincia = tag_value;
	      	}	
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("localidad")){ 	  
	    		localidad = tag_value;
	      	}	
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("tipo_via")){ 	  
	    		tipo_via = tag_value;
	      	}	
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("nombre_via")){ 	  
	    		nombre_via = tag_value;
	      	}	
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("numero_via")){ 	  
	    		numero_via = tag_value;
	      	}	
	    	else if(CRM.get(tag_name)!=null && CRM.get(tag_name).equals("cp")){ 	  
	    		cp = tag_value;
	      	}						
			 //System.out.println("<<"+tagList.get(i));
			//Insert the user into the DataBase.
			if(tagList.get(i).equals("end___")) {		
				this.insertUser();
			}				
		 }
	}
	
	public User(int crm_id, int user_ext_id,String name,
			    String xml_ads_path, String email, String phone,
			    String webpage,String xml_feeds_path) {
		
		this.crm_id = crm_id;
		this.user_ext_id  = user_ext_id;
		this.xml_ads_path = xml_ads_path;
		this.name  = name;
		this.email = email;
		this.phone = phone;
		this.webpage = webpage;
		this.xml_feeds_path = xml_feeds_path;
	}
	
	
	public String getId(){
		return this.id;
	}

	public String getXmlFeedsPath(){
		return this.xml_feeds_path;
	}

	public String getName(){
		return this.name;
	}

	public int getId_ext(){
		return this.user_ext_id;
	}
	
	public void deleteUsers(int crm_id){	
		DataBase  db = DataBase.getInstance();
		ResultSet rs = null;
		
		//Update the table parser_Users
		String query = "delete from `parser_Users` where id not in(select id from parser_AuxUsers) and crm_id = '"+crm_id+"'";
		db.executeUpdate(query); 
		
		//Empty the table parserAuxUsers
		query = "delete from parser_AuxUsers";
		db.executeUpdate(query); 
	}
	public void updateUsers(String user_id) throws SQLException{	
		DataBase  db = DataBase.getInstance();
		ResultSet rs = null;
		String update = "update parser_Users set ";
		boolean mismatch = false;		
		
		User user = getUserById(user_id);		
		Log log   = new Log();
		
		if(!this.name.equals(user.name)){	
			update+=" name = '"+this.name+"',";
			mismatch = true;			
		}
		if(!this.email.equals(user.email)){	
			update+=" email = '"+this.email+"',";
			mismatch = true;		
		}
		if(!this.phone.equals(user.phone)){	
			update+=" tel = '"+this.phone+"',";
			mismatch = true;
		}		
		
		if(mismatch){			
			if(update.endsWith(",")){   
				update = update.substring(0, update.length() - 1);		
			}	
			update+=" where id = "+user_id+";";		
			db.executeUpdate(update);
			
			System.out.println("Update user with id: "+user_id);
			log.writeLog("Log Info: Update user with id:"+user_id+"\n");
			log.writeLog("Query: "+update+"\n");
		}
	}
	
	public User getUserById(String user_id) throws SQLException{
		DataBase  db = DataBase.getInstance();
		String query = "select * from parser_Users where id='"+user_id+"'";
		ResultSet rs = db.executeSelect(query);
		int id = 0;
		User user = new User();
		
		if(rs.next()) {
			user.id = rs.getString(1);
			user.crm_id = rs.getInt(2);
			user.user_ext_id = rs.getInt(3);
			user.name = rs.getString(4);
			user.email = rs.getString(5);
			
			user.phone = rs.getString(6);
			user.webpage = rs.getString(7);
			user.xml_feeds_path =  rs.getString(8);
		}		
		return user;
	}
	
	public int getUserId(int crm_id, int user_ext_id) throws SQLException{
		DataBase  db = DataBase.getInstance();
		String query = "select * from parser_Users where crm_id = '"+crm_id+"' and user_ext_id='"+user_ext_id+"'";
		ResultSet rs = db.executeSelect(query);
		
		int id = 0;
		if(rs.next()) {
			id = rs.getInt(1);
		}		
		return id;
	}
	
	public User getUserByName(int crm_id, String name) throws SQLException{
		DataBase  db = DataBase.getInstance();
		String query = "select * from parser_Users where crm_id = '"+crm_id+"' and name='"+name+"'";
		ResultSet rs = db.executeSelect(query);
		
		String id = "";
		if(rs!=null) {
			if(rs.next()) {
				this.id 	= rs.getString(1);
				this.crm_id = rs.getInt(2);				
				this.user_ext_id = rs.getInt(3);
			}	
		}			
		return this;
	}
	
	public boolean insertUser(){
		DataBase  db = DataBase.getInstance();
		ResultSet rs = null;
		
		//Insert the users in the temporary table
		//String query = "insert into parser_AuxUsers (id) values ('9999"+this.crm_id+this.user_ext_id+"')";
		//db.executeInsert(query); 
		
		//if the user exists already, do not create it.
		 if(this.userExists()) {
			 try {
				this.updateUsers(Constant.id_prefix+this.crm_id+this.user_ext_id);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			 return false;
		 }
			
		this.query+= "('"+Constant.id_prefix+this.crm_id+this.user_ext_id+"','"
						  +this.crm_id+"', '"
						  +this.user_ext_id+"', "
						  +"'"+this.name+"', '"
						  +this.email+"','"
						  +this.phone+"','"
						  +this.webpage+"','"
						  +this.xml_feeds_path+"','"
						  +dni+"','"+provincia+"','"
						  +localidad+"'"
						  +",'"+tipo_via+"'"
						  +",'"+nombre_via+"'"
						  +",'"+numero_via+"'"
						  +",'"+cp+"'"
						  +",'"+country+"'"+"),";
				
		System.out.println("\t === INFO: Inserting a new user <"+this.name+"> ===\n");		 
		Log log = new Log();		 
		log.writeLog("\t=== INFO: Inserting a new user <"+this.name+"> ===\n\n");
			
		return true;
	}
	
	public void executeInsert() {			
		if(query.endsWith(",")){   
			query = query.substring(0, query.length() - 1) + ';';			
		}
		if(!query.endsWith("values ")){   
			//System.out.println(this.query);
			DataBase  db = DataBase.getInstance();
			db.executeInsert(query);
		}		
	}
	
	public ArrayList<User> getAllUsers() throws SQLException{		
		DataBase  db = DataBase.getInstance();
		String query = "select * from parser_Users";
		ResultSet rs = db.executeSelect(query);
		ArrayList<User> Users = new ArrayList<User>();
		User user; 
		
		while(rs.next()) {
			user = new User(1,rs.getInt(1),"","","","","","");
			System.out.println("-->"+rs.getInt(1));
		}	
		return Users;
	}
	
	public ArrayList<String> getDBUSers() throws SQLException{		
		DataBase  db = DataBase.getInstance();
		String query = "select * from parser_Users";
		ResultSet rs = db.executeSelect(query);
		
		ArrayList<String> users = new ArrayList<String>();
		User user; 
		
		while(rs.next()){			
			users.add(rs.getString(1));
		}	
		return users;
	}
	
	
	public boolean userExists(){
		DataBase  db = DataBase.getInstance();
		ResultSet rs = null;
		String query = "select * from parser_Users where user_ext_id = '"+this.user_ext_id+"' and crm_id='"+this.crm_id+"'";
		rs = db.executeSelect(query);
		
		try {
			if(rs!=null){
				if(rs.absolute(1)){
					this.id = rs.getString(1);
					return true;
				}
			}
		} catch (SQLException e1){}	
		return false;
	}
}
