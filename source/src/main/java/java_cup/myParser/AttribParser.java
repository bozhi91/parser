package java_cup.myParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

class bgThread  implements Runnable {
	
	String advert_id="";
	String value="";
	String name="";
	
	bgThread(String advert_id, String name, String value) {
		this.advert_id = advert_id;		
		this.name      = name;
		this.value     = value;
	}
	
	public void run(){
		AttribParser ap = new AttribParser();	
		try { 
			int id = ap.getElementByName(this.name);			
			ap.insertAttributeMeta(id,Integer.valueOf(this.advert_id),this.value);
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
}

public class AttribParser {
	private String name;
	private String value;
	private int id;
	private int meta_id;
	private String advert_id;
	
	public AttribParser(){}
	
	public AttribParser(String name) {
		this.name  = name;
	}
	
	public AttribParser(String advert_id, String tag_name, String tag_value) {
		//remove all non-numerical characters from the id
		if(tag_name.contains("id_") || tag_name.contains("_id")) {
			tag_value = tag_value.replaceAll("[^0-9.]", "");			
		}	
		if(advert_id.startsWith("00")){
			advert_id = advert_id.substring(2);
		}		
		if(tag_value.startsWith("00")){
			tag_value = tag_value.substring(2);
		}			
		if(tag_name.contains("id") && tag_value.contains("inmo")) {
			tag_value = tag_value.replace("inmo_","");
			tag_value = tag_value.replace("_","");
		}
		
		this.advert_id = advert_id;
		this.value     = tag_value;
		this.name      = tag_name;
	}

	public void setAttribValues(String advert_id, String tag_name, String tag_value) {
		this.advert_id = advert_id;
		this.value     = tag_value;
		this.name      = tag_name;
	}
	
	public void insertMetaAttribs() {
		new Thread(new bgThread(this.advert_id,this.name,this.value)).start();		
	}
	
	//insert the tag names of the XML document into the DataBase
	public void insertAttribute(String name) throws SQLException {		
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_attributes where name='"+name+"'";
	
		//Insert the attribute only once. If exists, do nothing
		if(db.isElementUnique(query)) {
			query = "insert into parser_attributes(name) values('"+name+"')";				
			db.executeInsert(query);	
		}
	}
	
	public void insertAttributeMeta(int attrib_id, int advert_id, String attrib_value) throws SQLException {
		DataBase db = DataBase.getInstance();
		String query = "insert into parser_attributes_meta(attrib_id,advert_int_id, attrib_value) values('"+attrib_id+"','"+advert_id+"','"+attrib_value+"')";				
		db.executeInsert(query);	
	}
	
	public int getElementByName(String name) throws SQLException {

		//get the ID of the 
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_attributes where name='"+name+"'";
		ResultSet rs = db.executeSelect(query);		
		
		if(rs!=null) {
			if(rs.next()) {
				return rs.getInt(1);	
			}
		}
		return 0;
	}
	
	public Map<String,String> getAttributeInternalId() throws SQLException {		
		//get the ID of the 
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_attributes";
		ResultSet rs = db.executeSelect(query);		
		Map<String,String>  hashAdvert = new HashMap <String, String>();
		
		while(rs.next()) {
			hashAdvert.put(rs.getString(2), String.valueOf(rs.getInt(1)));
		}
		return hashAdvert;
	}
	public Map<String,String> getMetaAttribId(int id) throws SQLException {		
		//get the ID of the 
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_attributes_meta where attrib_id='"+id+"'";
		ResultSet rs = db.executeSelect(query);		
		Map<String,String>  hashAdvert = new HashMap <String, String>();
		
		while(rs.next()) {
			hashAdvert.put(rs.getString(2), String.valueOf(rs.getInt(1)));
		}
		return hashAdvert;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	public Map<String,String> getAllMetaAttributes(int advert_id) throws SQLException {		
		//get the ID of the 
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_attributes_meta where advert_int_id='"+advert_id+"'";
		ResultSet rs = db.executeSelect(query);		
		Map<String,String>  hashAdvert = new HashMap <String, String>();
		
		while(rs.next()) {
			hashAdvert.put(String.valueOf(rs.getInt(2))+"__"+String.valueOf(rs.getInt(3)), String.valueOf(rs.getString(4)));//get the internal
		}
		return hashAdvert;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String getAdvertId() {
		return this.advert_id;
	}
	public int getAttribId() {
		return this.id;
	}
	public String getAttribName() {
		return this.name;
	}
	public String getAttribValue() {
		return this.value;
	}
	public int getMetaId() {
		return this.meta_id;
	}
	/////////////////////////////////////////
	public void setAdvertId(String advert_id) {
		 this.advert_id = advert_id;
	}
	public void setAttribId(int id) {
		this.id = id;	
	}
	public void setAttribName(String name) {
		this.name = name;
	}
	public void setAttribValue(String value) {
		 this.value = value;
	}
	public void setMetaId(int id) {
		this.meta_id = id;
	}
}
