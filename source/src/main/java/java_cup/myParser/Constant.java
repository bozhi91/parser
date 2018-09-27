package java_cup.myParser;

public class Constant {

	
/*	public static final String user = "root";
	public static final String dbConfigFile = "./db_connect.json"; 
	public static final String mapper 		= "./mapper.json";  	
	public static final String logger 		= "./Log_";
	public static final String logFolder    = "./logs/";			
	public static final String imagesFolder	= "./pictures/";  		
	public static final String watermark	= "./watermark.jpg"; 
*/
	
	public static final String dbConfigFile = "/home/bozhi/Downloads/xmlParser/db_connect.json";
	public static final String mapper 		= "/home/bozhi/Downloads/xmlParser/mapper.json";
	public static final String logger 		= "jar_";
	public static final String logFolder    = "/home/bozhi/Desktop/Logs/";	
	public static final String imagesFolder	= "/home/bozhi/Desktop/pictures/";
	public static final String watermark	= "/home/bozhi/Downloads/xmlParser/watermark.jpg";	
	
	
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
	
	public Constant() {}
}
