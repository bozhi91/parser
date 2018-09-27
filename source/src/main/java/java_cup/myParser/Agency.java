package java_cup.myParser;

import java.io.IOException;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ximpleware.NavException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/*
class DBConnectionBG  implements Runnable{
	String query;
	DBConnectionBG(String query) throws XPathParseException, XPathEvalException, NavException, IOException {
		this.query = query;
	}
	public void run() {
		Agency agency = new Agency();
		agency.insertAgency(this.query);
	}
}*/

public class Agency {
	private String endPointUrl;
	private String email;
	private String phone;
	private String id;
	private String name;
	private String country;
	private DataBase db;
	
	public Agency(){}
	
	public Agency(String endPointUrl, String id, String email, String phone,String name, String country) {
		this.endPointUrl = endPointUrl;
		this.id    = id;
		this.email = email;
		this.phone = phone;
		this.name    = name;
		this.country = country;
	}
	
	public String getId(){
		return this.id;
	}
	
	//returns true if the Agency exists. False otherwise
	/*public boolean checkAgency() throws XPathParseException, XPathEvalException, NavException, IOException {
	
		String query = "insert into Agency values('"+this.id+"','"+this.endPointUrl+"','"+this.email+"','"+this.phone+"','"+this.name+"','"+this.country+"')";
		
		new Thread(new DBConnectionBG(query)).start();
		
		JSONObject agency = new JSONObject();
		
		agency.put("id",this.id);
		agency.put("email",this.email);
		agency.put("phone",this.phone);
		agency.put("name",this.name);
		agency.put("country",this.country);
		
	//	System.out.println(agency);
		 
		return true;
	}*/
	
	public String getAgency(){
		return "";
	}
	
	//returns true if the Agency is created. False otherwise
	/*public boolean insertAgency(String query) {
		DataBase db = new DataBase();
		
		try{
			if(!db.getAgencyByEmail(this.email)) {
				db.insertAgency(query);	
			}
	
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return true;
	}
	*/
}
