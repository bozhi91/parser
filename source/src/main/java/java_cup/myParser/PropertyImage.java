package java_cup.myParser;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

class imageResize  implements Runnable{
		BufferedImage source;
		String local   = "";
		int x,y;
		String remoteFile = "";
		
	imageResize(BufferedImage source,String local,String remoteFile, int width, int height){
		     this.source = source;
		     this.local = local;
		     this.x = width;
		     this.y = height;
		     this.remoteFile = remoteFile;		
	}
	@Override
	public void run() {
		PropertyImage p = new PropertyImage();
		boolean resized = false;
		
		try {
			resized = p.resizeFile(source,local,x,y);
			if(resized) {
				p.pushS3Object(local, remoteFile);			
			}					
			p.deleteFile(local);
		} catch (IOException e){
			e.printStackTrace();
			//Log log = new Log();
			//log.exceptionsLog(e.toString());			
		}	
	}
}

class PropertyImage {
	ArrayList<String> fileUrl;
	File f = null;
	private static String filePath = Constant.imagesFolder;

	String user_id   = "";
	String advert_id = "";
	String order  	 = "";
	String bucketName;
	String secretKey  = "";
    String keyName    = "";
    
	PropertyImage(){
	   this.f = new File(this.filePath); // create a folder for the images
	   this.f.mkdir();
	   this.bucketName = Constant.S3bucketname;
	   this.secretKey  = Constant.secretKey;
	   this.keyName    = Constant.keyName;
	}

	public void downloadPendingImages(ArrayList<String> pendingImages){				
			if(pendingImages.size()>0){	
				//System.out.println("=== There are some pending images to download ===");
				for(int i=0;i<pendingImages.size();i++) {				
					try {
						downloadImage(pendingImages.get(i));
					} catch (IOException e) {e.printStackTrace();}
				}
			}		
	}	
	
	public String downloadImage(String url) throws IOException{				
		String filename   	  = "";	//extract the filename from the url
		String title     	  = ""; //the advert title.
		String extension  	  = "";	//file extension: jpg, jpeg, png, etc...
		String remoteFilePath = ""; //the remote file name 
		int pos = 0;		
		String pendingImage = null;	
		String aux_url = url;
		String id;
		
		//Split the arguments
		String[] tokens  = url.split("___");
		tokens  = url.split("___");
		this.order     = tokens[0];
		this.user_id   = tokens[1]; 
		this.advert_id = tokens[2];	
		url 		   = tokens[3];
		id   		   = tokens[4];
		title 	       = tokens[5];	
		remoteFilePath = "partners_new/"+this.user_id+"/"+this.advert_id+"/";
					
		//Get the url of the image
		URL website = new URL(url);	
		
		if(url.contains(".molista")){ 
			url.replace(".molista", "");			
		}
		
		//Extract the filename from the url. We convert the name: http://server.com/path/image.jpg to image.jpg
		filename = url.substring(url.lastIndexOf('/') + 1);		
		
		//Get file extension
		pos = filename.lastIndexOf('.');
		if(pos>0){extension = filename.substring(pos+1);}	
		
		//Normalize the image name. Remove unicode characters like: ñ,ç, á,í,ó, etc. Remove also non-alphanumerical characters except: '_'.
		title = title.replaceAll("[^A-Za-z0-9_]", "");
		String imageName  = order+"_"+title+"."+extension;
		imageName = imageName.replace("..", ".");
		imageName = java.text.Normalizer.normalize(imageName, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		int uploadedImage = Advert.isImageUploaded(id);

		try{		
			if(uploadedImage!=1){				
				float progress = ((Float.valueOf(tokens[7]))/Float.valueOf(tokens[6]))*100;
				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(2);				
				
				System.out.print("Uploading "+(tokens[7])+" out of "
						+tokens[6]
						+" images("+df.format(progress)
						+"%). For the advert("+this.advert_id+"), agency("+this.user_id+") ");
			
				/*	
				float progress = (Float.valueOf(tokens[7])/Float.valueOf(tokens[6]))*100;				
				System.out.printf("Uploading %s out of %s images(%.2f%). For the advert(%s) of the agency(%s)",
						tokens[7],tokens[6],progress,this.advert_id,this.user_id);*/
				
				//==== Download the original image =====
				   ReadableByteChannel rbc = Channels.newChannel(website.openStream());	
			   	   FileOutputStream fos    = new FileOutputStream(filePath+"o_"+imageName);		   	   
				   fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				//==== Download the original image =====
							
				 //Store the original image in the memory and delete it.
				 File input          = new File(filePath+"o_"+imageName);
				 BufferedImage image = ImageIO.read(input);				
				
				 //===== Upload the original image to S3(Amazon) server =====
				 if(image!=null)
					 pushS3Object(filePath+"o_"+imageName,remoteFilePath+"o_"+imageName);			
				 
				//Resize the images and upload them to the S3 server. After that, delete the image from the localhost	
				new Thread(new imageResize(image,filePath+"f_"+imageName,remoteFilePath+"f_"+imageName,Constant.image_full_width,Constant.image_full_height)).start();	
				new Thread(new imageResize(image,filePath+"m_"+imageName,remoteFilePath+"m_"+imageName,Constant.image_medium_width,Constant.image_medium_height)).start();	
				new Thread(new imageResize(image,filePath+"tm_"+imageName,remoteFilePath+"tm_"+imageName,Constant.image_small_width,Constant.image_small_height)).start();	  						
				 
				//If everything goes well, mark the image(in the Database) as uploaded
				Advert.uploadImage(id,1);			
			}
		}
		catch(IOException e) {
			Log log = new Log();
			log.writeLog("#Error: The image <"+url+"> was not downloaded!\n\t #Reason: "+e+"\n\n");
			log.exceptionsLog("#Error: The image <"+url+"> was not downloaded! Advert_id: "+this.advert_id
					+". Agency_id:"+this.user_id+"\n\t #Reason: "+e+"\n\n");
			System.out.println("#Error: The image <"+url+"> was not downloaded!\n\t #Reason: "+e+"\n\n");
			
			//Mark the image as NOT uploaded
			//pendingImage = aux_url;		
			imageName=aux_url+"_pending";
			Advert.uploadImage(id,0);
		}
		return imageName;	
	}
	
	//Resize the image
	public boolean resizeFile(BufferedImage image,String fileName_out, int width, int height) throws IOException{	
		String watermarkImageFile = Constant.watermark;
		//=====================================================================		  	
		  if(image==null) {
			  System.out.println("Image not readable: "+fileName_out);
			  return false;
		  }
			  			  
		   int originalWidth  = image.getWidth();
		   int originalHeight = image.getHeight();
			  
	       if(width < height){
	        	height = Math.round(width * ((float)originalHeight/originalWidth));
	        }
	        else if(height < width){
	        	width = Math.round(height * ((float)originalWidth/originalHeight));
	        }
	    //=====================================================================
		
	    Image tmp             = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.SCALE_DEFAULT);

        Graphics2D g2d        = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        
        //===== Add watermark to the image =====
           if(!fileName_out.contains("tm")){
        	 BufferedImage watermarkImage = ImageIO.read(new File(watermarkImageFile));
             int topLeftX = (resized.getWidth() - watermarkImage.getWidth())-50;
             int topLeftY = (resized.getHeight() - watermarkImage.getHeight())-50;
             
             AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
             g2d.setComposite(alphaChannel);      
             g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);
          }          
          g2d.dispose();
       //===== Add watermark to the image =====
          
       //Write the file to the disk
	   File output = new File(fileName_out);
	   ImageIO.write(resized, Constant.image_format, output);	   
	   return true;
    }
   
	//===== Upload file to the S3 Server =====
	public void pushS3Object(String localFileName,String remoteFileName) {		
		AmazonS3 s3client = initS3Client();
		Log log = new Log();
		
		log.writeLog("\t=== Info: File uploaded to S3 as: "+remoteFileName+" ===\n");		
	   // System.out.print("\t=== Info: Uploading file to S3 as: "+remoteFileName+" ===");
	    
	  //Upload files to Amazon S3
	    s3client.putObject(new PutObjectRequest(bucketName,remoteFileName, new File(localFileName)).withCannedAcl(CannedAccessControlList.PublicRead));
	    ((AmazonS3Client) s3client).getResourceUrl
	    (bucketName, remoteFileName);
	    
	    //Delete the original file after the upload
	    this.deleteFile(localFileName);	    
	    //System.out.println(" [ Done ] ");
	}
	
	public void deleteFile(String filename) {		
		File file = new File(filename);
		file.delete();
	}
	
	public AmazonS3 initS3Client() {		
	    AWSCredentials credentials = new BasicAWSCredentials(this.keyName, this.secretKey);	   
		AmazonS3 s3client = new AmazonS3Client(credentials);		
		return s3client;
	}
	
	public void listFiles() {		
		AmazonS3 s3client = initS3Client();		
		 //List all objects
        System.out.println("Listing objects");
        ObjectListing listing = s3client.listObjects(new ListObjectsRequest().withBucketName(bucketName));
      
        for(S3ObjectSummary objectSummary : listing.getObjectSummaries()){        
        	if(objectSummary.getKey().contains("partners_new")){
        		  System.out.println(" -> " + objectSummary.getKey() + "  " +
                          "(size = " + objectSummary.getSize()/1024 + " KB)");
        	}        
        }     
	}
	 // List all the buckets
	public void listAllBuckets(AmazonS3 s3client) {	  
	   /* List<Bucket> buckets = s3client.listBuckets();
	    for (Bucket next : buckets) {
	      System.out.println(next.getName());
	    }*/
	}		
}



