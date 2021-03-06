package java_cup.myParser;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.List;
import java.beans.Statement;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;

import com.amazonaws.services.cloudfront.model.Paths;
import com.ximpleware.AutoPilot;
import com.ximpleware.IndexReadException;
import com.ximpleware.NavException;
import com.ximpleware.PilotException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java_cup.internal_error;

public class Marketplace {
	private static String CRM_name = null;

	public Marketplace(){}
	
	public Marketplace(String CRM, String agency,boolean checkDeleted,boolean update, int agency_id, int advert_parse_id){
	   	this.CRM 	= CRM;
	   	CRM_name 	= CRM;
	   	this.agency = agency;	  
	   	this.update = update;
	   	this.checkDeleted    = checkDeleted;	   	
	   	this.agency_id 	     = agency_id;
	   	this.advert_parse_id = advert_parse_id;
	}
			
	public static String CRM    = "";
	String agency = "";
	public static boolean update;
	public static boolean checkDeleted;
	public int agency_id = 0;
	public static int advert_parse_id = 0;
	
	static Map<String, String> agenciesList   = new HashMap<String, String>();
	static Map<String, String> newAttributres = new HashMap<String, String>();
	
	public static int numAdverts  = 0;
	public static int numSites    = 0;
	public static float totalTime = 0;
	public static long numImages  = 0;
	public static int numThreads  = -1;
	public static int storedAdverts  = 0;
	public static int storedSites    = 0;
	public static int parsed_adverts = 0;
	
	public  int successAdverts  = 0;
	public  int failedAdverts   = 0;
	public  boolean advertFault = false;
	
	public  int failedImages = 0;
	public  int failedTags   = 0;
	
	public void parseMarketplaces() throws XPathParseException, XPathEvalException, NavException, IOException, SQLException, ParseException{
		try {			
			//Delete the xml files first so we can download them and work with a fresh/updated copy
			Runtime.getRuntime().exec("rm -rf "+Constant.xmlFolder+"*.xml");
			
			Advert.emptyAuxTable();
			parseCRM(this.CRM,this.agency);
		}catch(Exception e){
			
			String errorMessage = "Exception: "
					+"<"+e.toString()+">"			
					+". File: "+e.getStackTrace()[0].getFileName()
					+". Classname: "+e.getStackTrace()[0].getClassName()
					+". Method: "+e.getStackTrace()[0].getMethodName()
					+". Line: "+e.getStackTrace()[0].getLineNumber();
			
			Log log   = new Log();
			log.writeLog(errorMessage+" Stack trace: "+e.getStackTrace());
			
			/*System.out.println(errorMessage+" "+System.err.toString());			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			*/
			
			String text = "=== Critical Failure === The parsing process was terminated! Check the logfile: logname.txt for more information.";		 
			Commons.sendNotification(text+" "+errorMessage);		    
		}	
	    //Monitoring the threads running on background
	    //new Thread(new threadDispatcher()).start();
	}

	//This parser is valid ONLY for XML of molista stored in app.molista.com/XML/filename.xml
	public boolean parseCRM(String CRM_name,String agencia)
			throws XPathParseException, XPathEvalException, NavException, IOException, ParseException, SQLException {
		
		Log log   = new Log();
		int count = 0;
		
		//Select the mapper to parse the XML
	    Mapper map = new Mapper(CRM_name,"");
	    Map<String,String> CRM         = map.getCRMMapper();	
	    ArrayList<String> unmappedTags = new ArrayList<String>();
	    ArrayList<String> tagList = new ArrayList<String>();	    
	    ArrayList<User> agencies  = new ArrayList<User>();
	    
	    //Load the table parser_Users in the memory
	    ArrayList<String> dbUsers = new ArrayList<String>();
	    dbUsers = new User().getDBUSers();
	    
	    //Create new CRM in the database
		CRM crm = new CRM(CRM);
		crm.createNewCRM();
		int crm_id     = crm.getCrmByName(CRM_name).getId();	
		String fileURL = CRM.get("xml_guide_path");//get the url to the xml file with the agencies
		
		System.out.println("\n === Parsing CRM Guide: "+fileURL+" ===");
		log.writeLog("\n === Parsing CRM Guide: "+fileURL+" ===\n");
		//==============================================================================================================
		
		//========================== DOWNLOAD THE XML FILE WITH THE AGENCIES ==========================
		final VTDGen vg = new VTDGen();
		boolean status  = false;
		
		//Detect if the XML file is stored locally or remotely.	
		if(fileURL.contains("http")){//the XML file is stored on HTTP/HTTPS server
			//status = vg.parseHttpUrl(fileURL, true);			
			String filename = Commons.downloadFile(fileURL);			
			if(filename!=null){//Do this if the file was downloaded
				status = vg.parseFile(filename, true);
			}				
		}
		else if(fileURL.contains("ftp")){//the XML file is stored on FTP server			
			fileURL = fileURL.substring(6, fileURL.length());
			status  = Commons.downloadFTPFile(
					fileURL,"./"+fileURL,
					CRM.get("server"),
					Integer.valueOf(CRM.get("port")),
					CRM.get("user"),
					CRM.get("pass"));
			
			fileURL="./"+fileURL;			
			if(status){//Do this if the file was downloaded				
				status = vg.parseFile(fileURL, true);
			}			
		}	
		else {//the XML file is stored in the local machine
			status = vg.parseFile(fileURL, true);			   
		}				
		//Check if the given XML file is accessible. If it's not, quit the function.
	    if(!status) {
	    	System.out.println("#Error: The agencies file <"+fileURL+"> is not accessible for the CRM <"+this.CRM+">");
	    	log.writeLog("#Error: The file <"+fileURL+"> is not accessible!!\n");
	    	return false;
	    }
		//========================== DOWNLOAD THE XML FILE WITH THE AGENCIES ==========================			  
	    final VTDNav vn    = vg.getNav();
	    final AutoPilot ap = new AutoPilot(vn);
	 
    	//read the child nodes of the agency
	    User user        = null;
    	String tag_name  = "";
    	String tag_value = "";  
    	String site_id   = "";
    	boolean id_found = false, insert=false;
    	
    	//Iterate over all tags from the XML
	    ap.selectElement("*");
	    while(ap.iterate()){		    
	    	tag_name = vn.toString(vn.getCurrentIndex());
		    int t    = vn.getText();			 
		    
		    if(t!=-1){
		    	tag_value = vn.toNormalizedString(t);		    							 
		    	//If the XML tag is not present in the mapper, skip the code below and execute the loop from the begining.
		    	if(CRM.get(tag_name)==null){
		    		if(unmappedTags.indexOf(tag_name)==-1){//check if the tagname is already stored in the arraylist. We need to store id only once.
		    		   unmappedTags.add(tag_name);
		    		   System.out.println("The tagname <"+tag_name+"> from the file <"+fileURL+">is not mapped.");
		    		}		    	
		    		continue;		    	
		    	}		    	
		    	//Get the ID of the Agency and remove all non-numerical characters
		      	if(CRM.get(tag_name).equals("id")){	
		      		site_id = tag_value;		  
		      		site_id = site_id.replaceAll("[^0-9.]", "");

				   while(site_id.startsWith("0")){
					   site_id = site_id.substring(1, site_id.length());
				   }		
				   //========== FILTER BY AGENCY ID ==========
			       if(agency_id!=0){ //if we want to filter by agency id		      		
			    	   if(Integer.valueOf(site_id)==agency_id){
			    		   id_found = true;
			    		   insert   = true;						 						      
					   }
					   else insert = false;
			      	}
			      	else{
			      		id_found = true;
			      		insert   = true;
			      	}	
		      	  //========== FILTER BY AGENCY ID ==========
		      		numSites++;
		       	}		  
		      	//Check if the agency we're currently parsing is already present in the database.
		    	if(dbUsers.indexOf(Constant.id_prefix+crm_id+String.valueOf(site_id))!=-1){		      		
		      		//id_found=false;
		      	}
		      	if(id_found){		      		
		      		tagList.add(tag_name+"___"+tag_value);
		      	}		        
		        //Store the tagnames for the agency in the database in table: parser_Attributes
		        AttribParser a = new AttribParser();
		        a.insertAttribute(CRM.get(tag_name));
		     }//End if()
		
		    //When all attributes from one agency are redden, store them in the database as a new user
		    if(vn.getCurrentDepth()==1 && site_id!="" && id_found && insert){
		    	tagList.add("end___");
		    	user = new User(tagList,crm_id,CRM);//store the user in the database		    	
		    	agencies.add(user); //Store the user object in a hashmap so we can parse the adverts for a particular agency
		    }				  
	    }//end while()
	    //We're saving the last agency of the XML since we're leaving the while() loop before doing so.
	    if(id_found && insert){
		    tagList.add("end___");
	  		user = new User(tagList,crm_id,CRM);	
	  		agencies.add(user);
	    }  		
  		//Insert all users at once
  		if(user!=null) {
  		   user.executeInsert();
  		}
  
  		String feeds = CRM.get("xml_feeds");  		  		
	    for(int i=0; i<agencies.size();i++){
	    	//IF the CRM doesn't have a link/URL to the adverts.xml, that means there is only ONE XML(with adverts) for ALL agencies
	    	//They're porbably mixed(adverts and agencies) in the same XML
	    	if(CRM.get("xml_path").equals("none")){
	    		feeds = CRM.get("xml_feeds");
	    	}
	    	else{
	    		feeds = agencies.get(i).getXmlFeedsPath();
	    	}	    	
	    	if(agency_id!=0){ //filter by agencyID	    	    			    	
				if(agencies.get(i).getId().equals(Constant.id_prefix+crm_id+agency_id)){				
					id_found = true;					
					System.out.println("\n===> Parsing agency with external id:<"+agency_id+">. Agency name:<"+agencies.get(i).getName()+">\n");
					//new Thread(new  parseAdvertsBG(feeds, this.agency,agencies.get(i).getName(), crm_id, agencies.get(i).getId() )).start();
					parseXmlAdverts(feeds, this.agency,agencies.get(i).getName(), crm_id, agencies.get(i).getId());										
				}				
	    	}else{
	    		//System.out.println(agencies.get(i).getId()+" name: "+agencies.get(i).getName()+" feeds: "+feeds);
	    		id_found = true;
	    		System.out.println("\n===> Parsing agency with external id:<"+agencies.get(i).getId_ext()+">. Agency name:<"+agencies.get(i).getName()+">\n");
	    		//new Thread(new  parseAdvertsBG(feeds, this.agency,agencies.get(i).getName(), crm_id, agencies.get(i).getId() )).start();
	    		parseXmlAdverts(feeds, this.agency,agencies.get(i).getName(), crm_id, agencies.get(i).getId());
	    	}
	    }
  		    	    
	    //IF the agency has a separated XML file with the deleted adverts, then check the file and store those adverts in the database
	    deleteInactiveAgencies(CRM,crm_id);
			
	    if(!id_found && agency_id!=0){
        	System.out.println("\n\t === Agency with id <"+agency_id+"> was not found! ===");
        	log.writeLog("\n\t === Agency with id <"+agency_id+"> was not found! ===");
        }	
	    
	    //user.deleteUsers(crm_id);	  
	    System.out.println("\n === Parsing for the CRM Guide: <"+fileURL+"> has completed ===");
		log.writeLog("\n === Parsing for the CRM Guide: <"+fileURL+"> has completed ===n");		
		return true;
	 }
	
	public boolean parseXmlAdverts(String filename, String mapper, String site_name, int crm_id,String site_id)
		throws XPathParseException, XPathEvalException, NavException, IOException, ParseException, SQLException {				 
	
		long time_1   = System.currentTimeMillis();
		Log log       = new Log();
		Log masterLog = new Log(this.CRM);
		
		//Select the mapper to parse the adverts
	    Mapper map = new Mapper("",mapper);	  
	    Map<String,String> site_mapper = map.getCRMMapper();
	   
	    //Get the mapper of the CRM we're currently parsing
	    Mapper crm_map = new Mapper(CRM_name,"");
	    Map<String, String> CRM = crm_map.getCRMMapper();		  
	    
		System.out.println("\t\t Parsing feeds from: <"+filename+"> \n");	
		log.writeLog("\t\t Parsing feeds from: <"+filename+"> \n");
		
		//check if the mapper is available.
	    if(map.getCRMMapper()==null){
	    	System.out.println("============ Mapper : <"+mapper+"> was not found ============\n");
	    	log.writeLog("============ Mapper : <"+mapper+"> was not found ============\n");
	    	return false; //If the mapper is not found, exit	  
	    }
	  
	  //If the remote xml document is not accessible, exit
	    boolean status   = false;	    
	    final VTDGen vg  = new VTDGen();	   
	    String localFile = Constant.xmlFolder+filename.substring(filename.lastIndexOf('/') + 1);
	    boolean integrated = false;//This defines if the agencies and the feeds goes in the same XML file
	    
	    //This will check if the ID_Agency is integrated in the XML with the adverts
	    integrated = isAgencyIntegrated(mapper);
	  
	    //If the agency_id is not integrated in the xml feeds, delete the feeds.xml
	    if(!integrated){
		    Runtime.getRuntime().exec("rm -rf "+localFile);	
	    }
	    
	    //Detect if the XML file is stored locally or remotely.	
		if(filename.contains("http")){//the XML file is stored on HTTP/HTTPS server
			//status = vg.parseHttpUrl(filename, true);					
				
		    File file = new File(localFile);			
		    if(!file.exists()){
				System.out.println(" === Downloading file: <"+filename+"> ===");				
				filename = Commons.downloadFile(filename);	
			}
			else{
				filename = filename.substring(filename.lastIndexOf('/') + 1);	
				filename = Constant.xmlFolder+filename;
			}			
			if(filename!=null){//Do this if the file was downloaded
				status = vg.parseFile(filename, true);
			}		
		}
		//The XML file is stored on FTP server. In that case, download the xml file
		else if(filename.contains("ftp")){			
			filename = filename.substring(6, filename.length());
			status   = Commons.downloadFTPFile(
					filename,"./"+filename,
					CRM.get("server"),
					Integer.valueOf(CRM.get("port")),
					CRM.get("user"),
					CRM.get("pass"));						
			filename = Constant.xmlFolder+filename;			
			status   = vg.parseFile(filename, true);    
		}	
		else{//the XML file is stored in the local machine
			status = vg.parseFile(filename, true);			   
		}
	
	    //Check if the given XML file is accessible
	    if(!status){
	    	System.out.println("#Error: The xml-adverts file <"+filename+"> is not accessible!! ");
	    	log.writeLog("#Error: The xml-adverts file <"+filename+"> is not accessible!! \n");
	    	return false;
	    }	    
		
	    final VTDNav vn    = vg.getNav();	
	    final AutoPilot ap = new AutoPilot(vn);
	    
	    String advert_ext_id = null;
	    ArrayList<String> categoryList    = new ArrayList<String>();	  
	    ArrayList<String> unmappedAttribs = new ArrayList<String>();	  
	    ArrayList<String> adverts         = new ArrayList<String>();
	    ArrayList<String> agencies        = new ArrayList<String>();	   
	    ArrayList<String> attributes      = new ArrayList<String>();	   	 
	    ArrayList<AttribParser> attribMetaArray    = new ArrayList<AttribParser>();	   
	    Map<String,ArrayList<String>> agencies_map = new HashMap <String, ArrayList<String>>();
	    ////////////////////////////////////////////////////////////////////////////////////////	   
	    boolean insert = false, id_found = false;
	    boolean insert_age = false, id_found_age = false;
	    
	    ///THIS LINE IS USED FOR DEBUGING    
	    	if(advert_parse_id!=0) {
		      	System.out.println("\n\t ===> Parsing advert wiht id: "+advert_parse_id+" <===\n");
		    }
    	///THIS LINE IS USED FOR DEBUGING
	    
	    this.failedImages = 0;
	    this.failedTags   = 0;	
	    Date date = new Date();	 	 
	    masterLog.writeMasterLog( "Log from(hh:mm:ss): "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+"");
	    masterLog.writeMasterLog("Parsing Agency with external id: "+Constant.id_prefix+crm_id+site_id);
	    masterLog.writeMasterLog("XML FILE: "+filename+"\n");

	    String tag_name   	 = "";	   
	    String advert_id_tmp = "";
	      
        ap.selectElement("*");               
        while(ap.iterate()){
        	if(vn.getText()!=-1){
		    	tag_name  = vn.toString(vn.getCurrentIndex());		    			 
		      	String tag_value = vn.toNormalizedString(vn.getText());
		    	String tmp    = vn.toString(vn.getCurrentIndex());  
		    	String parent = null;
		    	String mapped_tag = "";	
		    	String agency_id  = "";	
		    	AttribParser attrib;		    				   

		    	//Get the ID of the advert. We also normalize the id. Remove all non-numerical characters and so on.
		    	//======================= PROCESS THE ID OF THE ADVERT =======================================
		    	if(site_mapper.get(tag_name)!=null && site_mapper.get(tag_name).equals("id")){		    		
		    		advert_id_tmp = tag_value;
		    		advert_ext_id = tag_value;//Get the advertId from the XML	
		    		advert_ext_id = advert_ext_id.replaceAll("[^0-9.]", "");
		    			
		    		while(advert_ext_id.startsWith("0")){
		    			advert_ext_id = advert_ext_id.substring(1);
		    		}			    				    				    		
		    		if(advertFault==true) this.failedAdverts++;
		    		else this.successAdverts++; 
		    		this.advertFault = false;	
		    				    				    		
		    		//=========== FOR DEBUGGING =================
			    	//=========== FILTER BY ADVERT ID ===========   		    		
		    		if(advert_parse_id!=0 && advert_ext_id!=null){
				    	if(advert_ext_id.equals(String.valueOf(advert_parse_id))){
				    		insert_age   = true;
							id_found_age = true;
						}	
						else{insert_age=false;}
					}
					else{
						if(!integrated){									
							insert_age=true;id_found_age=true;
						}						
					}	
			    	//=========== FOR DEBUGGING =================
			    }
		    	//======================= PROCESS THE ID OF THE ADVERT =======================================
		    	
		    	//======================= PROCESS THE ID OF THE AGENCY =======================================
		    	//we enter this condition if and only if the advert/feed holds the AgencyId. In our case - Ghestia
		    	if(site_mapper.get(tag_name)!=null && site_mapper.get(tag_name).equals("id_agency")){		    	
		    		String value = tag_value;		    		

		    		value = value.replace("inmo_", "");		    		
		    		while(value.startsWith("0")){
		    			value = value.substring(1);
		    		}
		    		//Get the agency_id from the adverts.xml
		    		if(site_id.equals(Constant.id_prefix+String.valueOf(crm_id)+value)){
		    			advert_ext_id = advert_id_tmp;
		    			if(advert_ext_id.contains("_")){
				    		String[] tokens = advert_ext_id.split("_");
				    		advert_ext_id   = tokens[2];
		    			}		    			
		    			insert_age   = true;
						id_found_age = true;
		    		}
		    		else{insert_age=false;}
		    	}
		    	//======================= PROCESS THE ID OF THE AGENCY =======================================
		    	
		    	//========== GET THE PARENT TAG IF WE HAVE NESTED TAGS ========== 
		    	if(!vn.toElement(VTDNav.NEXT_SIBLING,tag_name)){
		    		tmp   = vn.toString(vn.getCurrentIndex()); //store the current node	
					vn.toElement(vn.PARENT); 				   //switch to the parent node	
					parent = vn.toString(vn.getCurrentIndex());//get the parent node name	
					vn.toElement(VTDNav.FC,tmp);
					
					if(!parent.equals(site_mapper.get("root"))){
			    		tag_name = parent+"/"+tag_name;
			    	}
		    	}			    	
		    	//========== GET THE PARENT TAG IF WE HAVE NESTED TAGS ==========
		    	
		    	//Search the current tag in the mapper
			    if(site_mapper.get(tag_name)!=null){			    	
				    //If we have nested tags, iterate over all siblings and store them in the array list.
				    if(advert_ext_id!=null){
				    	do{	    	
					    	mapped_tag = site_mapper.get(tag_name);
					    	//Normalize the tag_value if the tagname is <id>. If we have. id_123_456, we remove the '_'
					    	if(site_mapper.get(tag_name).equals("id") && tag_value.contains("_")){
					    		String[] tokens  = tag_value.split("_");			
					    		tag_value = tokens[2];	
					    		advert_ext_id = tag_value;		
					    	}
				    		//Get the bathrooms, bathrooms, and other special attributes. Do this only for ghestia.
					    	//Index a specific property features
					    	if(site_mapper.get(tag_name)!=null && site_mapper.get(tag_name).equals("property_feature") && integrated){	
					    		//Special charset encoding. For spanish symbols like: ñ,ç á,é, etc.. DOUGH!! :/					    		
					    		tag_value = java.text.Normalizer.normalize(tag_value, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
					    		
					    		//if the tag_value(in that case the feature) is present in the mapper, store it in the database
					    		if(site_mapper.get(tag_value)!=null ){
					    			//mapped_tag = "feature_"+site_mapper.get(tag_value);
					    			mapped_tag = site_mapper.get(tag_value);
					    		}					    								   
					    		vn.toElement(VTDNav.NEXT_SIBLING);					 
					    		tag_value = vn.toNormalizedString(vn.getText());					    		
					    		//mapped_tag+=":"+tag_value;-> store this in attrib_meta
					    	}
					    	attrib  = new AttribParser(advert_ext_id, mapped_tag, tag_value);					    						    
					    	attribMetaArray.add(attrib);				    				    		
					    	tag_value  = vn.toNormalizedString(vn.getText());				    		
					    }while(vn.toElement(VTDNav.NEXT_SIBLING, tag_name));
				    }
				    
			     	//Store the category and subcategory id of the property. 
				    //==================== GET THE PROPERTY CATEGORY ====================
			     	if(site_mapper.get(tag_name).equals("category")){
			     		String attribute = tag_value; //Dúplex->duplex, Piso->piso, Casa->casa, etc...
			     		
			    		//Normalize the attribute name. Remove the special symbols like:(ñ,ç,à,ì,è, etc...). We also remove the empty spaces and ather shitty symbols.						
			     		attribute = java.text.Normalizer.normalize(attribute, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
			     		attribute = attribute.toLowerCase();
			     		attribute = attribute.replace(" ", "");	
						
						String subcat = CRM.get(attribute+"_sub");
						String cat    = CRM.get(attribute+"_cat");	
				
						//If the category is present in the XML but not in our mapper, display a error message.
						if(cat==null || subcat==null){						
							 if(categoryList.indexOf(attribute)==-1){	
								categoryList.add(attribute);	 
								//System.out.println("----> The attribute <"+attribute+"> is not mapped in the categories list.");								
								masterLog.writeMasterLog(" The attribute <"+attribute+"> is not mapped in the categories list.");	
								this.advertFault=true;
								this.failedTags++;
							 }
						}
						else{
							//Otherwise, store the category tag in the database.
							attrib 	   = new AttribParser(advert_ext_id, "category_id", cat);
							attrib.setAdvertId(site_id+advert_ext_id);
					     	attribMetaArray.add(attrib);	
					     	
					     	attrib 	   = new AttribParser(advert_ext_id, "subcategory_id", subcat);
					     	attrib.setAdvertId(site_id+advert_ext_id);
					     	attribMetaArray.add(attrib);
						}						
			     	}//end category if();
			     	//==================== GET THE PROPERTY CATEGORY ====================
			     	
			    	//Store each XML label only once in the array with attributes				    
					 if(attributes.indexOf(mapped_tag)==-1 && (mapped_tag!=null || !mapped_tag.equals("null")) && !this.checkDeleted){	
					    attributes.add(mapped_tag);						    					 
					 }    								
					 
					//Selective storage. If we're filtering by advertID, we store only the advert with this id.  
					if(advert_ext_id!=null && mapped_tag!=null){
					   	//Create array list of adverts
					   	if(adverts.indexOf(advert_ext_id)==-1){//store the advert only once in our arraylist					 	   
					   		if(insert_age && id_found_age){
					   		 	adverts.add(advert_ext_id);
					   			insert_age = false;
					   			if(this.checkDeleted){continue;}//if we only want to update the deleted adverts, skip the code below					   			
					   			//agencies_map.replace(site_id, null, adverts);
					   		}					   			
					   	}		      	
						//Store the attribute in the database and check if the attribute is mapped or not 
					  	//store each XML label only once in this array						    
					  	if(attributes.indexOf(mapped_tag)==-1 && (mapped_tag!=null || !mapped_tag.equals("null"))){
					  	   attributes.add(mapped_tag);						   	 
						}		    	
					}
					else if(advert_ext_id==null && mapped_tag==null){	
						System.out.println("\nError: *** Bad tagname <"+tag_name+"> in the mapper ***\n");
						log.writeLog("Error: *** Bad tagname <"+tag_name+"> in the mapper ***\n");
					}						
				//================================================================================================
			    }			   
			    else{	    
			    	if(newAttributres.get(tag_name)==null){				    		
			    	 	if(unmappedAttribs.indexOf(tag_name)==-1 ) {
			    	 		unmappedAttribs.add(tag_name);
			    	 		this.advertFault=true;
			    	 		//System.out.println("<<< The element: ("+tag_name+") from file:("+filename+") is not mapped. >>>");
			    	 		masterLog.writeMasterLog("<<< The element: ("+tag_name+") from file:("+filename+") is not mapped. >>>");
			    	 		this.failedTags++;
			    	 	}
			    	 	log.writeLog("<<< The element: ("+tag_name+") from file:("+filename+") is not mapped. >>>");
			    		newAttributres.put(tag_name,tag_name);			    		
			    	}		    
			   }
			   //================================================================================================							   
		    }//end if        	
        }	 //end while             
    	
        User user = new User(); 
        user = user.getUserByName(crm_id, site_name);
        
       /* if(this.checkDeleted)
        	writeAdvertsDB(attributes,adverts,attribMetaArray,user);
        */
        
        //======================Display a list of the unmapped attributes========================
        String regex 	   = "([A-Z][a-z]+)";
        String replacement = "$1_";        
        if(unmappedAttribs.size()>0){
        	System.out.println("\n === Insert the following attributes in the attributes list of the CRM of your mapper.json. \n");
        	for(int i=0;i<unmappedAttribs.size();i++) {
        		String attrib = unmappedAttribs.get(i);
        		//Convert the attribute to: from CamelCase to camel_case
        		attrib = attrib.replaceAll(regex, replacement);      
        		
        		if(attrib.endsWith("_"))
        			attrib = attrib.substring(0, attrib.length() - 1);
        		
        		attrib = attrib.toLowerCase();        	        
        		//System.out.println("\""+unmappedAttribs.get(i)+"\":\""+attrib+"\",");
        	}        	
        	unmappedAttribs.clear();
        	System.out.println();
        }        	
        if(categoryList.size()>0){
        	/*System.out.println("\n === Insert the following attributes in the category list of the CRM of your mapper.json. \n");
        	for(int i=0;i<categoryList.size();i++) {
        		System.out.println("\""+categoryList.get(i)+"_cat\":"+"\"\",");
        		System.out.println("\""+categoryList.get(i)+"_sub\":"+"\"\",");
        	}        		
        	categoryList.clear();
        	System.out.println();*/
        }
        //======================Display a list of the unmapped attributes========================
        
        //Add some extra attributes
        attributes.add("url_image_thumb");
        attributes.add("subcategory_id");
        attributes.add("category_id");
        ///////////////////////////////////////////////////////////////////////////////////////////        

        //Try to run this one in background       
        if(insert_age || id_found_age || insert || id_found){	
        	System.out.println(" The number of adverts to insert is: ("+adverts.size()+") from the file: "+filename+". Agency Id: "+site_id+"\n");
        	log.writeLog("The number of adverts to insert is: ("+adverts.size()+") from the file: "+filename+". Agency Id: "+site_id+"\n");     
        	
        	writeAdvertsDB(attributes,adverts,attribMetaArray,user);
         
        	System.out.println("=== Parsing finished for file: "+filename+", mapper: "+mapper+" ===");		    
     		log.writeLog("=== Parsing finished for file: "+filename+", mapper: "+mapper+" ===\n");     		
        }
        else if(id_found==false || adverts.size()==0){	
    	    System.out.println("\n\t *** Advert with id <"+advert_parse_id+"> not found! ***");
    		log.writeLog("*** Advert with id <\"+advert_parse_id+\"> not found!");
        }    
  
        masterLog.writeMasterLog("\n\n ::: Process completed for this agency ::: ");
        masterLog.writeMasterLog(" Failed(unmapped) tags: "+ this.failedTags);
        masterLog.writeMasterLog(" Failed(to download) images: "+ this.failedImages);
        
        masterLog.writeMasterLog(" Sucessfully inserted adverts: "+ this.successAdverts);
        masterLog.writeMasterLog(" Failed adverts: "+this.failedAdverts);
        masterLog.writeMasterLog(" Total adverts: "+(this.failedAdverts+this.successAdverts));
        masterLog.writeMasterLog("==============================================================\n\n");
        
        this.successAdverts = 0;
		this.failedAdverts  = 0;
        
	    long time_2 = System.currentTimeMillis();		
		totalTime=time_2-time_1;
	    return true;
	}

	//Process and store the advert in the database
	public boolean writeAdvertsDB(
			ArrayList<String> attributes,  
			ArrayList<String> adverts,  
			ArrayList<AttribParser> attribMetaArray,
			User user) throws NumberFormatException, SQLException {
		
		//Insert adverts			
		ArrayList<String> queries = new ArrayList<String>();
		Map<String,String> storedAdverts = null;	
		DataBase db  = DataBase.getInstance();
		Advert a     = new Advert();
		int counter  = 0;
		String query = "";	  
		String user_id = user.getId();	
		
		//Write the Adverts to the Database	
		//=======================================================================================================================================================
		//Check if the advert is created. If it's not, create it.					
			query = "insert into parser_Adverts(id,user_id,ad_ext_id) values";
			String queryaux = "insert into parser_AuxAdverts(advert_id) values";			
			db.executeUpdate("delete from parser_AuxAdverts");
			
			//Get all adverts for a given user. We store the table parser_Adverts in the memory.
			//Later we'll check if the advert with a certain ID is already stored in the Database.		
			storedAdverts = a.getAdvertsByUserId(user_id);					
			//Generate the query in the memory(RAM) so we can store all the adverts in the table parser_Adverts at once.
			int total = adverts.size();	
			for(int i=0;i<total;i++){
				System.out.print("\r");
				System.out.print(" Inserting "+(i+1)+" out of:"+adverts.size()+" adverts.....");				
				String adv_ext = null;			
				adv_ext = storedAdverts.get(adverts.get(i)); 				
		    	long adv_ext_id = Long.valueOf(adverts.get(i));
		    	queryaux+="('"+user.getId_ext()+adv_ext_id+"'),";
		    	
		    	//If the advert is not present in the database, insert it.
		    	if(adv_ext==null && String.valueOf(user_id+adv_ext_id).length()<18){	
		    		counter++;		    	
		    		query+= "('"+user_id+adv_ext_id+"','"+user_id+"','"+adv_ext_id+"'),";
		    		numAdverts++;
		    	} 
		    	else{	
		    		//System.out.println("Bad advert id: "+user_id+adv_ext_id);
		    		//Log log = new Log();
		    		//log.writeLog("Invalid advert id: "+user_id+adv_ext_id);
		    		//storedAdverts++;
		    	}
		    }
			System.out.println(" [ Done ] ");
			//we use query_aux to detect deleted adverts
			//Insert the advert in the aux table: parser_AuxAdverts
			if(queryaux.endsWith(",")){   
				queryaux = queryaux.substring(0, queryaux.length() - 1) + ';';	
				db.executeInsert(queryaux);
			}		
			//If there are any queries generated, execute them.
			if(counter>0){	
				if(query.endsWith(",")){
					query = query.substring(0, query.length() - 1) + ';';	
					db.executeInsert(query);
				}
			}			
			//if we're checking only for the deleted adverts, we dont need to index the attributes or upload the images.
			if(this.checkDeleted)
				return false;
			
			//Update the Values of the advert_id_ext with advert_id_intrnal in the attributes_meta_table
			storedAdverts = a.getAdvertsByUserId(user_id);			
			for(int i=0;i<attribMetaArray.size();i++){	
				String advert_id = attribMetaArray.get(i).getAdvertId();		
				String int_id    = storedAdverts.get(advert_id);//get the external advert id				
				attribMetaArray.get(i).setAdvertId(int_id);//replace it with the internal ID. The one stored in the database							
			}  	        		        
//=======================================================================================================================================================
		
	    //Write the Attributes to the Database
//=======================================================================================================================================================
			//Check if the attribute is created. If not, insert it in the Database
			 AttribParser ap = new AttribParser();		 
			 Map<String,String>attributesArray = ap.getAttributeInternalId();	
			 Map<String,String>meta_id = null;
			 
			 query = "insert into parser_attributes(name) values";			 
			 //Generate the query in the memory(RAM)
			 counter = 0;
			 String attrib_name = "";
			 int img=0;
			 for(int i=0;i<attributes.size();i++){
				//check if a given attribute is stored in the database(table: parser_Attributes). If it's not, store it!			
				 if(attributesArray.get(attributes.get(i))==null){
					query += "('"+attributes.get(i)+"'),";			
					counter++;
				 }
			 }					 			
			 //If there are any queries generated, execute them.		
			 if(counter>0){	
				if(query.endsWith(",")){                           			
					query = query.substring(0, query.length() - 1) + ';';						
				}	
				db.executeInsert(query);
			 }	
	
			//Update the Values of the attrib_id_ext with attrib_id_intrnal	in the table attributes_meta	
			attributesArray = ap.getAttributeInternalId();
			for(int i=0;i<attribMetaArray.size();i++){				
				String int_id = attributesArray.get(attribMetaArray.get(i).getAttribName());												
				if(int_id!=null) {
					attribMetaArray.get(i).setAttribId(Integer.valueOf(int_id));//update the attrib_id(in the table parser_attributes_meta)							
				}
			}						
//=======================================================================================================================================================	
			
		//Write the Meta Attributes	to the DataBase
//=======================================================================================================================================================
		//Check if the advert is already inserted. If it is, compare the field 'attrib_value' from the Database with the one
		// in the XML. If are equals, do nothing. Otherwise, update the field	
		Map<String,String> meta_attribs   = new HashMap<String, String>();	
		Map<String,String> active_adverts = new HashMap<String,String>();
		
		//get all adverts for the current Agency
		active_adverts = a.getActiveAdverts(user_id);	
		query   = "insert into parser_attributes_meta(attrib_id,advert_int_id, attrib_value) values";
		counter = 0;							
		
		
		//TODO: Try to run this loop in a new thread. So we can upload the images of different adverts at the same time.
		System.out.println("===== Uploading images =====");
		int num_rows = 0;		
		for(int i=0;i<attribMetaArray.size();i++){
			String key = attribMetaArray.get(i).getAttribId()+"__"+attribMetaArray.get(i).getAdvertId();							
			//Do this if the attribute is not stored in the attributes_meta table
			if(active_adverts.get(key)==null){												
				counter++;
				String attribValue = attribMetaArray.get(i).getAttribValue();
			
				if(!attribValue.contains("\\'"))
					attribValue = attribValue.replaceAll("'", "''");
				
				if(attribMetaArray.get(i).getAdvertId()!=null){	
					num_rows++;
					query+="('"+
							attribMetaArray.get(i).getAttribId()+"','"+
							attribMetaArray.get(i).getAdvertId()+"','"+
							attribValue+"'),";			
				}
				if(num_rows==50){//num of rows we want to insert at once in the database
					num_rows = 0;
					if(query.endsWith(",")){   
						query = query.substring(0, query.length() - 1) + ';';	
						db.executeInsert(query);
					}	
					query = "insert into parser_attributes_meta(attrib_id,advert_int_id, attrib_value) values";
				}					
			}
			else if(update){
				 //If the attribute already exists, check for differences between the one in the database and the one from teh XML
				 //If they are different, update the one from the Database.				
				if(!active_adverts.get(key).equals(attribMetaArray.get(i).getAttribValue())){
					//if the attribute is not an image or feature, then update it.	
					if( !attribMetaArray.get(i).getAttribName().equals("url_image") && 
						!attribMetaArray.get(i).getAttribName().equals("property_feature") &&
						!attribMetaArray.get(i).getAttribName().equals("feature_value") &&
						!attribMetaArray.get(i).getAttribName().equals("photo_numimagen")						
					){
						String[] parts   = key.split("__");
						String attrib_id = parts[0]; 
						String advert_id = parts[1];							
						String attribValue = attribMetaArray.get(i).getAttribValue();
						
						if(!attribValue.contains("\\'"))
							attribValue = attribValue.replaceAll("'", "''");
						
						String update = "update parser_attributes_meta set attrib_value = '"+attribValue+"' where attrib_id = '"+attrib_id+"' and advert_int_id ='"+advert_id+"'";															
						db.executeUpdate(update);
					}													
				}
			}
		}  
		
		//Do the insert of the advet meta attributes
		if(counter>0){	
			if(query.endsWith(",")){   
				query = query.substring(0, query.length() - 1) + ';';	
				db.executeInsert(query);
			}	
		}	
//=======================================================================================================================================================		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//========= Download the images for each advert =========
			Advert adv = new Advert();
			ArrayList<String> web_site_adverts;	
			ArrayList<String> pendingImages = new ArrayList<String>();
			String image = "";

			//set the tumb image for each advert
			Map<String,String> hashAdvert = adv.getAdvertTitle(user_id);
			web_site_adverts = adv.getAdvertImages(user_id);
		
			PropertyImage p = new PropertyImage();		
			hashAdvert      = adv.getAdvertTitle(user_id);
			
			for(int i=0;i<web_site_adverts.size();i++){	
				String[] tokens  = web_site_adverts.get(i).split("___");				
				try{	
					String adv_title = "";
					adv_title = hashAdvert.get(tokens[2]);
					if(adv_title==null)adv_title = "untitled";
					
					System.out.print("\r");
					image = p.downloadImage(web_site_adverts.get(i)+"___"+adv_title+"___"+web_site_adverts.size()+"___"+(i+1));	
					
					if(image.contains("_pending")) {
						tokens  = image.split("_");
						image = tokens[0];
						this.advertFault = true;
						this.failedImages++;
					}						
					if(image!=null) {
						if(i==0) {							
							tokens  = web_site_adverts.get(i).split("___");
							String adv_ext_id = tokens[2];						
							int uplThumb = Advert.isThumbUploaded(attributesArray.get("url_image_thumb"));						
							if(uplThumb==0) {
								query   = "insert into parser_attributes_meta(attrib_id,advert_int_id, attrib_value,img_uploaded) values";
								query+="('"+attributesArray.get("url_image_thumb")+"','"+user_id+adv_ext_id+"','"+user_id+"/"+adv_ext_id+"/tm_"+image+"','1')";//insert the tumbnail		
								db.executeInsert(query);								
							}														
						}							
						//pendingImages.add(image);	
					}									
				}catch (IOException e){
					e.printStackTrace();
				}	
			}
			System.out.println();
			//p.downloadPendingImages(pendingImages);
		//========= Download the images for each advert =========	
		return true;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Get the inactive aggencies from a XML file, store their ids in the database in the table parser_AuxAdverts
	private boolean deleteInactiveAgencies(Map<String,String> CRM,int crm_id) throws PilotException, NavException {
		String fileURL  = CRM.get("xml_inactive_feeds");
		boolean status  = false;
		String tag_name  = "";
    	String tag_value = "";  
    	String site_id   = "", advert_id="";
    	DataBase db  = DataBase.getInstance();
		final VTDGen vg = new VTDGen();	
		
		//If the CRM doesn't have a 
		if(fileURL==null) {
			return false;
		}
		//========================== DOWNLOAD THE XML FILE WITH THE AGENCIES ==========================
		System.out.println("\nChecking for deleted adverts...");
		//Detect if the XML file is stored locally or remotely.	
		if(fileURL.contains("http")){//the XML file is stored on HTTP/HTTPS server
			status = vg.parseHttpUrl(fileURL, true);	
		}
		else if(fileURL.contains("ftp")){//the XML file is stored on FTP server			
			fileURL = fileURL.substring(6, fileURL.length());
			status  = Commons.downloadFTPFile(
					fileURL,"./"+fileURL,
					CRM.get("server"),
					Integer.valueOf(CRM.get("port")),
					CRM.get("user"),
					CRM.get("pass"));
			
			fileURL="./"+fileURL;
			if(status)//do this if the file was downloaded
				status = vg.parseFile(fileURL, true);
		}	
		else {//the XML file is stored in the local machine
			status = vg.parseFile(fileURL, true);			   
		}	
		
		//Check if the given XML file is accessible. If it's not, quit the function.
	    if(!status) {
	    	System.out.println("#Error: The agencies file <"+fileURL+"> is not accessible for the CRM <"+this.CRM+">");	    	
	    	return false;
	    }
		//========================== DOWNLOAD THE XML FILE WITH THE AGENCIES ==========================
			  
	    final VTDNav vn    = vg.getNav();
	    final AutoPilot ap = new AutoPilot(vn);
    	String query = "Insert into parser_DeletedAdverts values";
	    int count_regs=0;
    	
	    //Iterate over all tags from the XML
	    ap.selectElement("*");
		while(ap.iterate()){
			tag_name = vn.toString(vn.getCurrentIndex());
		    int t    = vn.getText();	
		    if(t!=-1) {
		    	tag_value = vn.toNormalizedString(t);			    			  		    			    	
		    	if(tag_value.contains("_")){
		    		//tag_name = tag_name.replace("_", replacement);			    		
		    		String[] tokens  = tag_value.split("_");		    		
		    		site_id   = tokens[1];
		    		advert_id = tokens[2];
		    		query+="("+site_id+advert_id+"),";
		    		count_regs++;
		    	}
		    }					    
		}
		//Clear the table before storing the new IDs
		db.executeUpdate("delete from parser_DeletedAdverts"); 
		
		//Store the advertsId in the database so we can delete them later
		if(query.endsWith(",") && count_regs>0){   
			query = query.substring(0, query.length() - 1) + ';';	
			db.executeInsert(query);			
			System.out.printf("  We have: %d deleted adverts.\n",count_regs);
		}				
		return true;		 
	}	
	
	private boolean isAgencyIntegrated(String filename){		
		if(filename.equals("ghestia") || filename.equals("1001portales")){
		    return true;
		}
		return false;
	}
	
	/*
	   final VTDNav vn    = vg.getNav();
	    final AutoPilot ap = new AutoPilot(vn);
    	String query = "Insert into parser_DeletedAdverts values";
	    int count_regs=0;
    	
	    //Iterate over all tags from the XML
	    ap.selectElement("*");
		while(ap.iterate()){
			tag_name = vn.toString(vn.getCurrentIndex());
		    int t    = vn.getText();	
		    if(t!=-1) {
		    	tag_value = vn.toNormalizedString(t);			    			  		    			    			    
		    	if(tag_name.contains("agency_id")){
		    		return true;
		    	}
		    }					    
		}*/
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class parseAdvertsBG implements Runnable {	
	String fileurl,mapper,site_name,site_id;
	int user_int_id,crm_id,totalRegisters;

	public static int numThreads = 0;
	
	//parseXmlAdverts(fileurl,mapper,user_int_id);
	public parseAdvertsBG(String fileurl, String mapper, String site_name, int crm_id,String site_id) {
		this.fileurl   = fileurl;
		this.mapper    = mapper;
		this.site_name = site_name;
		this.crm_id    = crm_id;	
		this.site_id = site_id;
	}
	
	public void run(){
		Marketplace mp = new Marketplace();	
		if(mp.numThreads==-1)mp.numThreads=0;
		try{
			mp.numThreads++;			
			if(mp.update) {
				Advert.updateAuxTable(crm_id);				
			}			
			mp.parseXmlAdverts(this.fileurl, this.mapper, this.site_name,this.crm_id,this.site_id);			
			mp.numThreads--;
		} 
		catch (XPathParseException e){
			e.printStackTrace();
		} catch (XPathEvalException e){
			e.printStackTrace();
		} catch (NavException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		} catch (ParseException e){
			e.printStackTrace();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
}


///////////////////////////////////////////////////////////////////////////////////////
/*
//This calss will notify us when the parser is done
class threadDispatcher  implements Runnable {	
	
	public threadDispatcher(){
	    Thread t = Thread.currentThread();
	    t.setName("Dispatcher");
	    t.setPriority(1);
	}
	
	public void run(){
		Marketplace mp  = new Marketplace();		
		
		 Thread t = Thread.currentThread();
		    t.setName("Dispatcher");		    
		    t.setPriority(1);
		    System.out.println("current thread: " + t);
		    
		    int count = 0;
		    
		    do{	  
			    int active = Thread.activeCount();
			    System.out.println("currently active threads: " + active);
			    Thread all[] = new Thread[active];
			    Thread.enumerate(all);			    
			    count=0;			    
			    for (int i = 0; i < active; i++) {
			      //System.out.println(all[i].getName());
			    	if(all[i].getName().contains("Thread")){	
			    		System.out.println("Still alive :P");
					    count++;
			    	}			    	
			    }				  
		    }while(count!=0);
		
		System.out.println("\n ========= Parsing completed ========= \n");
		System.out.println("Total parsed adverts: "+mp.numAdverts);
		System.out.println("Total stored adverts: "+mp.storedAdverts);
		System.out.println("Total sites/users parsed: "+mp.numSites);
		System.out.println("Total users stored: "+mp.storedSites);
		System.out.println("Total time: "+mp.totalTime/1000+"s");
		System.out.println("===================================================");
	}
}
///////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////
 
  */


