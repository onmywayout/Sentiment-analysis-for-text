package tesis.informationCollector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tesis.PersistentStorage.DAO;

import com.mongodb.BasicDBObject;




/**
 * Offers useful services for other classes
 * @author Nicolas
 *
 */
public class Util
{


	/**
	 * Storage location for errors
	 */
	static String StoragePath = System.getProperty("user.home")+  java.io.File.separator  + "reportSOFIA.txt";


	/**
	 * Quita imagenes y tags del texto, solo conserva el cuerpo de la noticia
	 * 
	 * @param contenidoItem
	 * @return
	 */
	public static String cleanRSS(String contenidoItem) 
	{

		String respuesta = "";
		String cuerpo = "";
		String descr = ""; //Para cuando hay los dos tags en la noticia
		String cont = "";

		Pattern description = Pattern.compile("<description>(.*)</description", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher descriptionMatches = description.matcher(contenidoItem);

		Pattern content = Pattern.compile("<content.*?>(.*)<\\/content", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher contentMatches = content.matcher(contenidoItem);

		if(descriptionMatches.find())
		{descr  += (descriptionMatches.group(1));}//
		if (contentMatches.find())
		{cont  += contentMatches.group(1);	}//

		cuerpo = descr.length()>cont.length()? descr:cont;
		if(cuerpo.startsWith("<"))
		{cuerpo.replaceFirst("<", "");		}

		//removes every div and similars

		cuerpo = cuerpo.replaceAll("(<.*?>)", " ");

		
		//removes links
		cuerpo = cuerpo.replaceAll("((((https?):((//)|(\\\\))+)|(www.))+[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]*)", " ");

		//replace all non UTF-8 characters so the analyzer wont print error messages
		cuerpo = cuerpo.replaceAll("[^\\x20-\\x7e]", "");
		//replaces non representative characters which slow down the analyzer
		cuerpo = cuerpo.replaceAll("\\$|&|\\[|\\]|'|:|\\?|/|\\^|\"", "");
		
		//removes whitespaces
		cuerpo = cuerpo.replace("\n", "");



		cuerpo = cuerpo.replace("\n", "");

		respuesta = cuerpo.replace("p&gt;", "").replace("/p&gt;", "")
				.replace("![CDATA[", "").replace("]]", "").replace("&lt;", "")
				.replace("div class=quot;field field-type-text field-field-teaserquot;    div class=quot;field-itemsquot;            div class=quot;field-item oddquot;", "").replace("</STRONG>", "")
				.replace("/imggt;", "").replace("width=\"1\"", "")
				.replace("href=\"\"", "").replace("gt;", "")
				.replace("img src=\"\"", "").replace("border=\"0\"", "")
				.replace("&", "").replace("acute;", "")
				.replace("div class=\"feedflare\"", "").replace("/img/a", "")
				.replace("/div height=\"1\"", "").replace("img alt=", "")
				.replace("nbsp;", "").replace("tilde;", "");

		return respuesta.trim();
	}


	/**
	 * Cleans the tweet info for eassier processing
	 * Besides it replaces common emoticons with words to ease the classifier work
	 * @param tweet text
	 * @param Language English EN or Spanish SP
	 * @return text free of line returns, hashtags, mentions and links
	 */
	public static String cleanTweet(String texto, String Language) 
	{
		String response = texto;

		//removes mentions
		response = response.replaceAll("@(.*?)\\s", " ");
		//removes hashtags
		response = response.replaceAll("#(.*?)\\s", " ");
		//removes links
		response = response.replaceAll("((((https?):((//)|(\\\\))+)|(www.))+[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]*)", " ");

		//removes whitespaces
		response = response.replace("\n", "");

		String positiveWord = "";
		String negativeWord = "";

		if(Language.contains("EN"))
		{
			positiveWord = "happy";
			negativeWord = "sad";
		}
		else
		{
			positiveWord = "feliz";
			negativeWord = "triste";
		}


		response = response.replaceAll("(:\\)|\\(:|:D|♥|💕|😊|👏|👌|😂|😖|😁|😃|💃|😍|<3|:-\\))", " " + positiveWord + " ");

		response = response.replaceAll("(:s|:\\(|\\):|😴|😩|😗|😞|😔|😒|:-\\(|:'\\()", " " + negativeWord + " ");

		//replace all non UTF-8 characters so the analyzer wont print error messages
		response = response.replaceAll("[^\\x20-\\x7e]", "");
		//replaces non representative characters which slow down the analyzer
		response = response.replaceAll("\\$|&|\\[|\\]|'|:|\\?|/|\\^|\"", "");


		return response.trim();


	}

	/**
	 * Logs a message to the log file
	 * @param messagge
	 * @param origin
	 */
	public static void log(String message, String origin)
	{
		@SuppressWarnings("deprecation")
		String date = new Date().toGMTString();


		try 
		{
			Logger log = Logger.getLogger("DEFAULT");
			FileHandler fh;
			fh = new FileHandler(StoragePath,true);
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  

			log.info("\n------- \n¡INFO!:"+origin+"\n"+date+"\n");

			log.log(Level.INFO, message);
		}
		catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}


	}
	/**
	 * Prints error messages passed as a parameter to a file and stores it on Mongo
	 * @param error error to be write
	 * @param origin class or entity that throws the error
	 */
	public static void printErrorMessage(Throwable e, String origin)
	{
		Date date = new Date();
		@SuppressWarnings("deprecation")
		String error = date.toGMTString();

		try 
		{

			Logger log = Logger.getLogger("DEFAULT");
			FileHandler fh = new FileHandler(StoragePath,true);
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  

			log.info("\n------- \nERROR:"+origin+"\n");
			log.info(error + "\n" + e.getMessage()+"\n"+ e.getLocalizedMessage());

			log.log(Level.SEVERE, e.getMessage(), e);

			fh.close();


		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}

		//in case of log writing failure, write to the database on a different try-catch block
		try 
		{
			String ip =InetAddress.getLocalHost().getHostAddress();
			BasicDBObject store = new BasicDBObject("DATE" , new Date()).append("ERROR:",e.getMessage()).append("ORIGIN", origin).append("IP", ip);
			DAO.getInstance().insert(DAO.STATUS, store );

		}
		catch (Exception e2) 
		{
			e2.printStackTrace();
		}
	}


	/**
	 * Prints error messages passed as a parameter to a file and stores it on Mongo
	 * @param error error to be write
	 * @param origin class or entity that throws the error
	 */
	public static void printErrorMessage(Throwable e, String origin, String aditionalInfo)
	{
		Date date = new Date();
		@SuppressWarnings("deprecation")
		String error = date.toGMTString();

		try 
		{

			Logger log = Logger.getLogger("DEFAULT");
			FileHandler fh = new FileHandler(StoragePath,true);
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  

			log.info("\n------- \nERROR:"+origin+"\n");
			log.info(error + "\n" + "Message : " + e.getMessage()+"\n"+
					"LocalizedMessage : " +e.getLocalizedMessage()
					+ "Aditional Info:" + aditionalInfo);

			log.log(Level.SEVERE, e.getMessage(), e);

			fh.close();


		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}

		//in case of log writing failure, write to the database on a different try-catch block
		try 
		{
			String ip =InetAddress.getLocalHost().getHostAddress();
			BasicDBObject store = new BasicDBObject("DATE" , new Date()).append("ERROR:",e.getMessage())
					.append("ORIGIN", origin).append("IP", ip).append("ADITTIONALINFO", aditionalInfo);
			DAO.getInstance().insert(DAO.STATUS, store );

		}
		catch (Exception e2) 
		{
			e2.printStackTrace();
		}
	}


}
