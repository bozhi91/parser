package java_cup.myParser;

import java.beans.Statement;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.ximpleware.NavException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class Advert {
	private DataBase db;
	
	private int id;
	private int user_id;
	private int ad_ext_id;
	private String type;
	private String desc;
	private String title;

	public Advert(){}
	
	public Advert(int user_id, int ad_ext_id) {
		this.user_id   = user_id;
		this.ad_ext_id = ad_ext_id;
	}
	
	public int getAvertId(int user_id, int ad_ext_id) throws SQLException {
		int id = 0;		
		DataBase  db = DataBase.getInstance();
		ResultSet rs = null;
		String query = "select * from parser_Adverts where user_id='"+user_id+"' and ad_ext_id='"+ad_ext_id+"'";
		rs = db.executeSelect(query);		
		
		if(rs!=null){
			if(rs.next()){
				id = rs.getInt(1);
			}
		}
		return id;
	}
	
	
	public 	String getAdvertById(int user_id, int adv_id) throws SQLException {
		ArrayList<String> adverts = new ArrayList<String>();
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_Adverts where user_id='"+user_id+"' and ad_ext_id ='"+adv_id+"'";
		ResultSet rs = db.executeSelect(query);
		String ext_id = null;
		
		if(rs.next()) {			
			ext_id = String.valueOf(rs.getInt(1));
		}
		return ext_id;
	}
	
	 public Map<String,String>  getAllAdverts() throws SQLException {
		
		ArrayList<String> adverts = new ArrayList<String>();
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_Adverts";
		ResultSet rs = db.executeSelect(query);
	
		Map<String,String>  hashAdvert = new HashMap <String, String>();
		ArrayList<Map<String,String>> hashAdvertArray = new ArrayList<Map<String,String>>();
		  
		while(rs.next()){
			hashAdvert.put(String.valueOf(rs.getInt(3)), String.valueOf(rs.getInt(1)));
		}
		return hashAdvert;
	}
	 
	  public Map<String,String>  getAdvertsByUserId(String user_id) throws SQLException {			
		ArrayList<String> adverts = new ArrayList<String>();
		DataBase db  = DataBase.getInstance();
		String query = "select * from parser_Adverts where user_id='"+user_id+"'";
		ResultSet rs = db.executeSelect(query);
	
		Map<String,String>  hashAdvert = new HashMap <String, String>();
		ArrayList<Map<String,String>> hashAdvertArray = new ArrayList<Map<String,String>>();
		  
		while(rs.next()){
			hashAdvert.put(String.valueOf(rs.getLong(3)), rs.getString(1));
		}		
		return hashAdvert;
	}
	  
	  public Map<String,String>  getAdvertTitle(String user_id) throws SQLException {						
			
		    DataBase db  = DataBase.getInstance();
			Map<String,String>  hashAdvert = new HashMap <String, String>();
			
			String query = "";
			
			query = "select padv.ad_ext_id as adv_ext_id, pam.attrib_value as title\n" + 
					"from parser_Adverts padv, parser_attributes_meta pam, parser_attributes pa\n" + 
					"where padv.user_id = '"+user_id+"'\n" + 
					"and pam.advert_int_id = padv.id\n" + 
					"and pam.attrib_id = pa.id\n" + 			
					"and pa.name = \"title\"";
			
			ResultSet rs = db.executeSelect(query);
			String title = "";
			while(rs.next()){
				title = rs.getString(2);
				
				if(title.length()>80) {
					title.substring(0,80);
				}
				
				title = title.toLowerCase();
				title = title.replace(" ", "_");
				hashAdvert.put(rs.getString(1),title);
			}
			return hashAdvert;
		}
	  
	  public ArrayList<String> getAdvertImages(String user_id) throws SQLException {			
			ArrayList<String> adverts = new ArrayList<String>();
			DataBase db  = DataBase.getInstance();
			String query ="";
			
			query = "select padv.user_id, padv.ad_ext_id as advert_id, pam.attrib_value as image, pam.id\n" + 
						"from parser_Adverts padv, parser_attributes_meta pam, parser_attributes pa\n" + 
						"where padv.user_id = '"+user_id+"'\n" + 
						"and pam.advert_int_id = padv.id\n" + 
						"and pam.attrib_id = pa.id\n" + 
						"and pa.name = \"url_image\"";
			 
			ResultSet rs = db.executeSelect(query);			
			long current=0, prev=-1,counter=0; 
			
			while(rs.next()){
				counter++;
				current = rs.getLong(2);
				if(current!=prev) {
					prev=current;
					counter=0;
				}				
				adverts.add(String.valueOf(counter+"___"+rs.getString(1))+"___"+String.valueOf(rs.getLong(2))+"___"+rs.getString(3)+"___"+rs.getInt(4));				
			}
			return adverts;
		}
		 
	  public static String getAdvertText(String advertId) throws SQLException {			
			
			DataBase db  = DataBase.getInstance();
			String query ="";
			
			 query = "select  pam.*\n" + 
						"from parser_Adverts padv, parser_attributes_meta pam, parser_attributes pa\n" + 
						"where pam.advert_int_id = '"+advertId+"'\n" + 
						"and pam.advert_int_id = padv.id\n" + 
						"and pam.attrib_id = pa.id\n" + 
						"and pa.name = \"title\"";			

			ResultSet rs = db.executeSelect(query);							
			String title = "";
			while(rs.next()){		
				 title =  rs.getString(4);
			}
			return title;
		}
	
	  //Returns all adverts from the DataBase for a given user_id
	  public Map<String,String> getActiveAdverts(String user_id) throws SQLException{						
	
		DataBase db  = DataBase.getInstance();
		String query = "select  am.attrib_id, am.advert_int_id, am.attrib_value from parser_Adverts a, parser_attributes_meta am where user_id='"+user_id+"' and a.id=am.advert_int_id";
		ResultSet rs = db.executeSelect(query);
		 
		ArrayList<String>   advert     = new ArrayList<String>();
		Map<String,String>  hashAdvert = new HashMap <String, String>();
		
		while(rs.next()){		
			hashAdvert.put(rs.getString(1)+"__"+rs.getString(2),rs.getString(3) );
		}
		return hashAdvert;
	}
	 
	 public Map<String,String> getActiveAdvertsId(int user_id) throws SQLException {						
			DataBase db  = DataBase.getInstance();
			String query = "select am.id, am.attrib_id, am.advert_int_id, am.attrib_value from parser_Adverts a, parser_attributes_meta am where user_id='"+user_id+"' and a.id=am.advert_int_id";
			ResultSet rs = db.executeSelect(query);
			 
			ArrayList<String>   advert     = new ArrayList<String>();
			Map<String,String>  hashAdvert = new HashMap <String, String>();
			
			while(rs.next()){		
				hashAdvert.put( String.valueOf(rs.getLong(2))+"_"+String.valueOf(rs.getInt(3))+"_"+rs.getString(4),rs.getString(1) );
			}
			return hashAdvert;
		}
	  
	 public static void uploadImage(String id, int status){						
			DataBase db  = DataBase.getInstance();
			String query = "update parser_attributes_meta set img_uploaded='"+status+"' where id="+id;	
			db.executeUpdate(query);		
		}
	 
	 public static int isImageUploaded(String id){						
			DataBase db  = DataBase.getInstance();
			String query = "select * from parser_attributes_meta where id="+id;		
			ResultSet rs = db.executeSelect(query);
			
			try {
				if(rs!=null) {
					while(rs.next()){		
						return rs.getInt(6);
					
				}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}		
			return 0;
		}
	 
	 public static int isThumbUploaded(String id){						
			DataBase db  = DataBase.getInstance();
			String query = "select * from parser_attributes_meta where attrib_id="+id;		
			ResultSet rs = db.executeSelect(query);
			
			try {
				if(rs.next()){
					return rs.getInt(6);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}		
			return 0;
		}

	public static void updateAuxTable(int crm_id) {
		DataBase db  = DataBase.getInstance();

		String query =  "delete from parser_Adverts "
				+ "where id in( select pa.id from (SELECT * FROM parser_Adverts) AS pa,"
				+ " parser_Users pu, parser_CRMs pc where pa.user_id = pu.id and pu.crm_id = pc.id"
				+ " and pc.id = '"+crm_id+"' and pa.id not in(Select advert_id from parser_AuxAdverts) )";
		//db.executeUpdate(query);
	}
	
	public static void emptyAuxTable() {
		DataBase db  = DataBase.getInstance();
		String query = "delete from parser_AuxAdverts";
		db.executeUpdate(query);
	}
}
