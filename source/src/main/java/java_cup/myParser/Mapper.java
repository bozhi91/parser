package java_cup.myParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mapper {

	private String endPointUrl;
	private String email;
	private String phone;
	private int id;
	
	private String marketplace;
	private String crm;
	
	//TODO...
	public Mapper(String crm, String marketplace) {
		this.marketplace = marketplace;
		this.crm = crm;
	}
	public Mapper(){}
	
	public Map<String,String> getCRMMapper() 
			throws FileNotFoundException, IOException, ParseException {
		
		String json_mapper = Constant.mapper;//"/home/bozhi/Downloads/mapper.json";
		JSONParser parser  = new JSONParser();
        JSONObject jsonObject     = (JSONObject) parser.parse(new FileReader(json_mapper));  
        JSONObject jsonObject_tmp = jsonObject;
        Map<String,String> CRM    = new HashMap <String, String>();
        ArrayList<Map<String,String>> CRMS = new ArrayList<Map<String,String>>();   
        ArrayList<String> crm_guides = new ArrayList<String>();
        boolean siteFound = false;
        
        JSONArray  crmsList = null;
        String name = "";
    	
        if(crm.length()!=0) {
    		crmsList = (JSONArray) jsonObject.get("CRMs");
    		name     = crm;
    	}
    	else {
    		crmsList = (JSONArray) jsonObject.get("Marketplaces");
    		name     = marketplace;
    	}
             
        //Load CRMs data
        CRM = new HashMap <String, String>();
        for(int i=0;i<crmsList.size();i++){
        	jsonObject = (JSONObject) crmsList.get(i);      
        	jsonObject_tmp=jsonObject;
        	if(jsonObject.get("site_name").equals(name)){
        			siteFound = true;
        			
        			//get the ftp attributes from the CRM mapper
        			jsonObject_tmp = (JSONObject)jsonObject.get("ftp_attribs");
        			if(jsonObject_tmp!=null) {
        				for(Object key : jsonObject_tmp.keySet()){
    	            		CRM.put(key.toString(),(String) jsonObject_tmp.get(key));
    	            	}	
        			}
        			
        			//get the category list from the CRM section in the mapper
        			jsonObject_tmp = (JSONObject)jsonObject.get("categories");
        			if(jsonObject_tmp!=null) {
        				for(Object key : jsonObject_tmp.keySet()){
    	            		CRM.put(key.toString(),(String) jsonObject_tmp.get(key));
    	            	}	
        			}
        			
        			//Get the list of CRM XML guide 	            
        			//Load CRM data
	        		CRM.put("site_name",(String) jsonObject.get("site_name"));	            
	            	CRM.put("root",(String) jsonObject.get("root"));
	            	CRM.put("xml_guide_path",(String) jsonObject.get("xml_agencies"));
	            	CRM.put("xml_feeds",(String) jsonObject.get("xml_feeds"));
	            	CRM.put("xml_inactive_feeds",(String) jsonObject.get("xml_inactive_feeds"));
	            
	            	// === Read a JSON Array ===
	            	/*JSONArray guides = (JSONArray) jsonObject.get("xml_guides");	
	            	 for(int j=0;j<guides.size();j++){
	            		 JSONObject guide  = (JSONObject) guides.get(j);	            		
	            		 crm_guides.add((String) guide.get("guide"));	            		
	            		 CRM.put("xml_guides",crm_guides);
	            	 }*/	            	
	            	 
	            	//Get all values of the object attributes
	            	jsonObject = (JSONObject)jsonObject.get("attributes");
	            	for(Object key : jsonObject.keySet()) {	            		
	            		CRM.put(key.toString(),(String) jsonObject.get(key));		            		
	            	}	    
        		} 
        	}
        if(siteFound)return CRM;
        else return null;
	}
}
