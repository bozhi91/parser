package java_cup.myParser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class Commons {
	//private static FTPClient ftpClient = null;
	public static boolean createFTPConnection(String server, int port, String user, String pass){
		 
		int FTP_CONNECTION_TIMEOUT = 3000; 		 
		 //TODO....
	     return true;
	}
	
	public static boolean downloadFTPFile(String remoteFile, String destFile, String server, int port, String user, String pass){		 
		
		int FTP_CONNECTION_TIMEOUT = 3000; 		 
	    FTPClient ftpClient = new FTPClient();	     
	    Log log = new Log();	      
	   
	    System.out.println("\nConnecting to ftp server: "+server);
	    log.writeLog("\nConnecting to ftp server: "+server);
	      
	      try {
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);
		    ftpClient.enterLocalPassiveMode();
		    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		    ftpClient.setConnectTimeout(FTP_CONNECTION_TIMEOUT);		   
		   
		   //String remoteFile = Constant.remote_ftp_file;
		    System.out.print("  Downloading FTP file: "+remoteFile);
            File downloadFile1 = new File(destFile);
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile, outputStream1);
            outputStream1.close();
 
            if(!success){
            	System.out.println("\nFile <"+remoteFile+"> was not downloaded. File was not found on the server.");
            	log.writeLog("\nFile <"+remoteFile+"> was not downloaded. File was not found on the server.");
            	return false;
            }     
            
		} catch(IOException e){
		  System.out.println("\n FTP Error: " + e.getMessage());
		  log.writeLog("\n FTP Error: " + e.getMessage());
		  return false;
		}
	      System.out.println("\t[ DONE ]");
	      return true;
	}
	
	public static void sendNotification(String message) throws IOException{
		String url  = "https://api.telegram.org/bot517091407:AAEeNCLcVGyjyExKaJ_oNhOjx-ZG4DqZDoI/sendMessage";
		//String text = "Critical Failure. The parsing process was terminated! Check the logfile: logname.txt for more information.";
		message = message.replace(" ","%20");

		System.out.println("=== A message from the XML Parser ===");
	    Process p = Runtime.getRuntime().exec("curl -s -X POST "+url+" -d chat_id=227254225 -d text="+message);
	}
	
	//Download file from HTTP server
	public static String downloadFile(String fileUrl) throws IOException{			
		
		String filename = null;
		try {			
			filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
			URL website = new URL(fileUrl);	
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		   	FileOutputStream fos    = new FileOutputStream(Constant.xmlFolder+filename);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);	
		}catch(IOException e){}
		
		return Constant.xmlFolder+filename;
	}
}
