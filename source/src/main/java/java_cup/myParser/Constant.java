package java_cup.myParser;

public class Constant {
	
	/*public static final String user = "root";
	public static final String dbConfigFile = "./utils/db_connect.json"; 
	public static final String mapper 		= "./utils/mapper.json";  	
	public static final String logger 		= "./utils/Log_";
	public static final String logFolder    = "./utils/logs/";			
	public static final String imagesFolder	= "./utils/pictures/";  		
	public static final String watermark	= "./utils/watermark.jpg"; 
	public static final String xmlFolder	= "./utils/xmls/";
	*/
	
	public static final String dbConfigFile = "/home/bozhi/Downloads/xmlParser/xmlParser/utils/db_connect.json";
	public static final String mapper 		= "/home/bozhi/Downloads/xmlParser/xmlParser/utils/mapper.json";
	public static final String logger 		= "jar_";
	public static final String logFolder    = "/home/bozhi/Desktop/Logs/";	
	public static final String imagesFolder	= "/home/bozhi/Desktop/pictures/";
	public static final String watermark	= "/home/bozhi/Downloads/xmlParser/xmlParser/utils/watermark.jpg";	
	public static final String xmlFolder	= "/home/bozhi/Desktop/xmls/";
	
	
	//===== Image properties =====
	public static final int image_full_width  = 1200;
	public static final int image_full_height = 900;
	
	public static final int image_medium_width  = 1024;
	public static final int image_medium_height = 768;
	
	public static final int image_small_width  = 320;
	public static final int image_small_height = 240;
	public static final String image_format    = "jpeg";
	
	//===== S3 Amazon properties =====
	public static final String S3bucketname = "homesya";
	public static final String secretKey    = "iXRO4BCoaHlOgGXW4Y1KTf+BRlt3l2rr+xkq75vw";
	public static final String keyName      = "AKIAJQJTEIRZ7R4M4KSA";	
	public static final String CDN_SERVER   = "https://cdn.homesya.com/";
	
	/*public static final String S3bucketname = "s3bucket";
	public static final String secretKey    = "807f1d77f0f7920ae608f9a3430c7dd11ffad71e";
	public static final String keyName      = "0fea4ab0ca7411e88cb3312109b66a69";	
	public static final String CDN_SERVER   = "https://cdn.homesya.com/";
	*/
	//===== FTP Constants =====
	//public static final String remote_ftp_file = "/AGENCIAS.xml";
	
	public static final String id_prefix = "";
	public Constant() {}
}
