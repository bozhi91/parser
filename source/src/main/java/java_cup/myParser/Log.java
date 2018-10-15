package java_cup.myParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class Log {

	private int id;
	private String name;
	private String guide_xml_path;

	private static String filename  = null;
	private static String masterLog = null;
	
	//Initialize this using Singelton pattern
	public Log(){		
		if(this.filename==null) {			 
			File f = new File(Constant.logFolder); // create a folder for the images
			if(!f.exists()) {
				f.mkdir();
			}
			
			//The filename is: jar_dd_mm_yyyy
			Date date = new Date();
	    	String logname = Constant.logger+date.getDate()
	    			+"-"+(Integer.valueOf(date.getMonth())+1)
	    			+"-"+(date.getYear()+1900)+".txt";
	    	
	    	this.filename  = Constant.logFolder+logname;
		}			
	}
	public Log(String crm_name){	
		if(this.masterLog==null) {			 
			File f = new File(Constant.logFolder); // create a folder for the images			
			if(!f.exists()){
				f.mkdir();
			}
	
			//The filename is: jar_dd_mm_yyyy
			Date date = new Date();
	    	String logname = Constant.logger+"MasterLog_"+crm_name+"_"+date.getDate()+"-"+date.getMonth()+"-"+(date.getYear()+1900)+".txt";
	    	this.masterLog  = Constant.logFolder+logname;
	    	
	    	File file = new File(this.masterLog);
		   	if(file.exists()){
		   		file.delete();
		   	}
		}
	}

	public void writeLog(String message){
		  try{
	    	File file = new File(this.filename);
	    	if(!file.exists()){
	    	   file.createNewFile();
	    	}
	    	
	    	Date date = new Date();
	    	String time = "Log from(hh:mm:ss): "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+" -> ";
	    	
	    	FileWriter fw     = new FileWriter(file,true);
	    	BufferedWriter bw = new BufferedWriter(fw);
	    	bw.write("\n"+time+message+"\n");		  
	    	bw.close();	    	
	      }catch(IOException ioe){}		  	
	}
	
	public void exceptionsLog(String message){
		
		String exceptFile = this.filename;
		exceptFile = exceptFile.replace("jar", "exception");

		System.out.println("Log "+exceptFile);
		
		try{
	    	File file = new File(exceptFile);
	    	if(!file.exists()){
	    	   file.createNewFile();
	    	}
	    	
	    	Date date = new Date();
	    	String time = "Log from(hh:mm:ss): "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+" -> ";
	    	
	    	FileWriter fw     = new FileWriter(file,true);
	    	BufferedWriter bw = new BufferedWriter(fw);
	    	bw.write("\n"+time+message+"\n");		  
	    	bw.close();	    	
	      }catch(IOException ioe){}  	
	}
	
	public void writeMasterLog(String message){
	  try{
	   	File file = new File(this.masterLog);
	   	if(!file.exists()){
	   	   file.createNewFile();
	   	}  		   
	    	
	   	FileWriter fw     = new FileWriter(file,true);
	   	BufferedWriter bw = new BufferedWriter(fw);

	 	bw.write(message+"\n");
	   	
	   	bw.close();	    	
	  }catch(IOException ioe){}
	}	 
}
