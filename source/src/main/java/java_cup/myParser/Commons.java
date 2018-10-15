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
		/*String server = "ftp.ghestia.cat";
	    int port      = 21;
	    String user   = "homesya";
	    String pass   = "WyiAt6n42xPernXmVZBv";*/
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
}
