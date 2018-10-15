package java_cup.myParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class CRM {

	private int id;
	private String name;
	private String xml_guide_path;

	public CRM(Map<String,String> CRM) {	
		this.name = CRM.get("site_name");
		this.xml_guide_path = CRM.get("xml_guide_path");
	}
	
	public CRM(){}
	
	public CRM getCrmByName(String name) throws SQLException {
		DataBase  db = DataBase.getInstance();
		String query = "select * from parser_CRMs where name = '"+name+"'";
		ResultSet rs = db.executeSelect(query);
		
		if(rs!=null){
			if(rs.next()){
				this.id = rs.getInt(1);
				this.name = rs.getString(2);
				this.xml_guide_path = rs.getString(3);
			}		
		}	
		return this;
	}
	
	public boolean createNewCRM() throws SQLException {
		DataBase  db = DataBase.getInstance();
		ResultSet rs = null;
		
		//if the user exists already, do not create it.
		 if(this.crmExists()) {
			 return false;
		 }
		
		String query = "insert into parser_CRMs (name, xml_agency_path)"
				+ "values ('"+this.name+"', '"+this.xml_guide_path+"')";
				
		db.executeInsert(query);				
		return true;
	}
	
	public boolean crmExists() {
		DataBase  db = DataBase.getInstance();
		ResultSet rs = null;
		String query = "select * from parser_CRMs where name = '"+this.name+"'";
		rs = db.executeSelect(query);
		
		try {
			if(rs!=null){
				if(rs.absolute(1)){
					return true;
				}
			}
			
		} catch(SQLException e1){}
		return false;
	}
	
	public int getId() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public String getXmlPath() {
		return this.xml_guide_path;
	}
	
	
}
