package java_cup.myParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.json.simple.parser.ParseException;
import java_cup.internal_error;

public class mainParser {
	
	static boolean update  = true;
	static boolean run = false; 
	
/*	static int agency_id   = 96; //The external Id of the agency fom the XML
	static int advert_id   = 2060;//5615;  //Parse and store ONLY the advert with this ID	

	public static String CRM    = "molista_kyero";
	public static String mapper = "kyero";
	*/
	
 	static int agency_id   = 37;  //50;   //2; //The external Id of the agency fom the XML
	static int advert_id   = 1444; //5896; //5890;  //Parse and store ONLY the advert with this ID	

	public static String CRM    = "habitaclia";
	public static String mapper = "habitaclia";
	
	public static String agenciesFile = null;
	public static String feedsFile	  = null; 
	
	public static void main(String argv[]) 
	  throws internal_error, java.io.IOException, java.lang.Exception {
		
		DataBase db    = new DataBase();		
		Marketplace mp = new Marketplace(CRM,mapper,update,agency_id,advert_id);
		mp.parseMarketplaces();
		 
			
		//Get the input arguments from the console. 
		/*splitArgs(argv);
		if(run){
			if(validateInput()){		
				DataBase db    = new DataBase();		
				Marketplace mp = new Marketplace(CRM,mapper,update, agency_id,advert_id);
				mp.parseMarketplaces();								
			}
			else{
				System.out.println("#ERROR: Wrong input. Check the manual of the application: ./parser.jar -h");
				//printHelp();
			}
		}*/
	 }
	
	private static boolean validateInput() throws FileNotFoundException, IOException, ParseException {
		
		String crm_name    = CRM;
		String mapper_name = mapper;
		
		if(crm_name==null){
			 System.out.println("You must specify a CRM name");
			 return false;
		}
		if(mapper_name==null){
			 System.out.println("You must specify a mapper name");
			 return false;
		}
		else {
			 Mapper crm    = new Mapper(crm_name,"");
			 Mapper agency = new Mapper("",mapper_name);
			 
			 Map<String,String> crm_map    = crm.getCRMMapper();
			 Map<String,String> agency_map = agency.getCRMMapper();
			 
			 if(crm_map==null ) {
				 System.out.println("CRM name <"+crm_name+"> is incorrect");
				 return false;
			 }	
			 if(agency_map==null) {
				 System.out.println("The mapper name <"+mapper_name+"> is incorrect");
				 return false;
			 }	
		}
		 return true;
	}
	
	private  static void splitArgs(String argv[]) {
		
		boolean wrongInput = false;
		
		run = true;
		if(argv.length==0) {
			run = false;
			printHelp();
		}
		
		for(int i=0;i<argv.length;i++){						
			if(argv[i].equals("-u")) {
				update = true;
			}
			else if(argv[i].equals("-h")) {
				printHelp();
				run = false;
			}
			
			else if(argv[i].equals("-a")) {
				 if(++i>=argv.length  || argv[i].startsWith("-")) {
					 System.out.println("\n Error: Missing agencies filename.");
					 wrongInput = true;
				 }					
				 else  System.out.println("\n Filename: "+argv[i--]);
			}
			
			//Get the mapper name
			else if(argv[i].equals("-m")) {												
				 if(++i>=argv.length  || argv[i].startsWith("-")) {
					 System.out.println("\n Error: Missing mapper name!");
					 wrongInput = true;
				 }					
				 else mapper=argv[i--];
			}
			//Get the CRM
			else if(argv[i].equals("-c") ) {								
				 if(++i>=argv.length  || argv[i].startsWith("-")) {
					 System.out.println("\n Error: Missing CRM name");
					 wrongInput = true;
				 }
				 else CRM = argv[i--];
			}
			//Get the advert external id
			else if(argv[i].equals("-uid") ) {								
				 if(++i>=argv.length  || argv[i].startsWith("-")) {
					 System.out.println("\n Error: Missing agency id.");
					 wrongInput = true;
				 }
				 else agency_id = Integer.parseInt(argv[i--]);
			}
			//Get the advert external id
			else if(argv[i].equals("-aid")) {								
				 if(++i>=argv.length  || argv[i].startsWith("-")) {
					 System.out.println("\n Error: Missing advert id.");
					 wrongInput = true;
				 }
				/* else if(agency_id==0) {
					 System.out.println("\n Error: Missing agency id.");
					 wrongInput = true;
				 }*/
				 else advert_id = Integer.parseInt(argv[i--]);
			}
			
			else{
				//System.out.println("Unrecognized option \"" + argv[i] + "\"");
			 }
		}
		
		if(wrongInput) printHelp();
	}
	  
	private static void printHelp(){	
		
		System.out.println("==================================================================================================");
		System.out.println("***** USAGE *****:.\n ./parser.jar -c <crm_name> -m <mapper> -n [num of registers to parse] -u -uid [external agency id] -aid [external advert id].\n");				
	
        System.out.println(" ===== Params ===== ");    
        System.out.println("\t -h   Displays a help message(This one :D ).");
		System.out.println("\t -c <crm name>   The name of the crm.");
		System.out.println("\t -m <mapper>     The name of the mapper.");
		System.out.println("\t -n <integer positive number> The number of the users and adverts you want to parse from the XML.");
		System.out.println("\t -u This will update the existing registers(users or adverts)");
		System.out.println("\t -uid <ineger number> The external id of the agency (The one specified in the XML).");		
		System.out.println("\t -aid <ineger number> The external id of the advert (The one specified in the XML). "
				+ "This parameter is combined with the one above. If you use this one, is mandatory to set the agency id as well.\n");
		
		System.out.println(" Example1: parser.jar -c molista_homesya -m homesya "
				+ "-> This command will parse, insert or update all the feeds from the CRM 'homesya' using the parser 'homesya'.\n");		
		
		System.out.println(" Example2: parser.jar -c molista_homesya -m homesya -n 2 -u "
				+ "-> This command will parse and insert only 2 feeds from the CRM 'homesya' using the parser 'homesya'."
				+ " This also will update the existing feeds.\n");
		
		System.out.println(" Example3: parser.jar -c molista_homesya -m homesya -uid 1 -aid 1 -> "
				+ "This will parse only the advert with id=1 from the agency_id=1. ");
		
		System.out.println("==================================================================================================");
		
		/*
		System.out.println("\n\t  =================== DEBUG MODE =================== \n" + 
				" - Cargar todos los System.out.println(\"===================================================================================\"); de un crm: parser.jar molista_homesya homesya 0 1\n" + 
				" \n" + 
				" - Cargar los primeros X anuncios de un crm:  parser.jar molista_homesya homesya N 1. 'N' es el numero de anuncios a cargar. 1 es un flag que indica que queremos actualizar los registros existentes.\n" + 
				" \n" + 
				" - Cargar todos los anuncios de una agencia de un crm: parser.jar molista_homesya homesya 0 1 agency_id\n" + 
				" \n" + 
				" - Cargar un anuncio de una agencia de un crm: parser.jar molista_homesya homesya 1 1 agency_id advert_id");			
		 */
	}
}

//Test Shit
/*	Advert adv = new Advert();
	ArrayList<String> web_site_adverts;	
	ArrayList<String> pendingImages = new ArrayList<String>();
	String image = "";

	web_site_adverts = adv.getAdvertImages(1191);
	PropertyImage p  = new PropertyImage();		
	Map<String,String> hashAdvert = adv.getAdvertTitle(1191);		
	String[] tokens  = web_site_adverts.get(0).split("-");
	p.downloadImage(web_site_adverts.get(0)+"-"+hashAdvert.get(tokens[2]));					
	*/
