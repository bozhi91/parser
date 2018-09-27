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
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.omg.CORBA.portable.OutputStream;

public class Feed {

	private String reference;
	private String id;
	private String agencyName;
	private ArrayList<String> imagesArray;
	private String type;
	private String title;
	private String description;
	
	public Feed(String id, String reference, String type, String title,String description, String agencyName,  ArrayList<String> imagesArray) {
		this.reference = reference;
		this.id        = id;
		this.agencyName = agencyName;
		this.imagesArray = imagesArray;
		this.type = type;
		this.title = title;
		this.description = description;
	}
	
	
/*	public  String getAgency() {
		 JSONObject agency = new JSONObject();
		 	agency.put("id",this.id);
		 	agency.put("email",this.email);
		 	agency.put("phone",this.phone);
			agency.put("name",this.name);
		 	agency.put("country",this.country);
		 	
		 	return agency.toString();
	}*/
	
	//returns true if the Agency is created. False otherwise
	public boolean createFeed() throws IOException, SQLException {
		
		DataBase db = new DataBase();
		db.initDB();
		
		String insert = "insert into Agency values('1','"+this.agencyName+"','email@server.com','+34 555 123 456','Homesya','España')";
		
		//db.insertAgency(insert);
		
/*
		JSONObject feed      = new JSONObject();
		JSONArray imagesList = new JSONArray();
		 
		for(int i = 0;i<this.imagesArray.size();i++) {
			imagesList.add(imagesArray.get(i));
		}
		feed.put("imagesArray",imagesList);
		
		feed.put("id",this.id);
		feed.put("ref",this.reference);
		feed.put("agencyName",this.agencyName);
		feed.put("type",this.type);
		feed.put("title",this.title);
		feed.put("description",this.description);*/
		
		//Send HTTP Request....
	//	sendPOST("https://www.casinuevo.com/webservice/ws_form.php?methodo=insertAd",feed.toString()); 
		return true;
	}
	
	private static void sendPOST(String site_url, String data) throws IOException {
		
		System.out.println(data);
		
		  HttpURLConnection con = null;
		  String urlParameters = data;
		  byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
		
		
		 
		  System.out.println(data);	
		        
		        try {
		            URL myurl = new URL(site_url);
		            con = (HttpURLConnection) myurl.openConnection();

		            con.setDoOutput(true);
		            con.setRequestMethod("GET");
		            con.setRequestProperty("User-Agent", "XML awesome parser.");
		            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
		             
		                wr.write(postData);
		            }

		           StringBuilder content;

		            try (BufferedReader in = new BufferedReader(
		                    new InputStreamReader(con.getInputStream()))){

		                String line;
		                content = new StringBuilder();

		                while ((line = in.readLine()) != null) {
		                    content.append(line);
		                    content.append(System.lineSeparator());
		                }
		                System.out.println(content);
		            }
		          
		        } finally {
		            con.disconnect();
		       }
		  }
}
