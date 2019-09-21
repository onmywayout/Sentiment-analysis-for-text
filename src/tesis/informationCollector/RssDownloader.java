package tesis.informationCollector;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tesis.PersistentStorage.DAO;
import tesis.TextAnalyzer.TextAnalyzerCore;

import com.mongodb.BasicDBObject;


public class RssDownloader implements Runnable
{


	/**
	 * Database access
	 */
	private DAO dao;

	/**
	 * Ruta donde se almacenan los feeds
	 */
	//Unix:  /User/User_name/RssDownloads/
	// Windows: C:users\\User_name\\RssDownloads\\

	//




	/**
	 * Feeds to be downladed
	 */
	ArrayList<String> feedsArray= new ArrayList<String>();

	/**
	 * feeds language
	 */
	ArrayList<String> language= new ArrayList<String>();


	String errors = "";

	/**
	 * Variables used during the downloading of each RssFeed
	 */
	URL server;
	URLConnection urlCon;
	HttpURLConnection con;

	/**
	 * Tool for text analysis in Spanish
	 */
	private TextAnalyzerCore tacSP;



	/**
	 * Tool for text analysis in English
	 */
	private TextAnalyzerCore tacEN;


	/**
	 * Hold the Regex patterns used in Matchers
	 */
	private Pattern items = Pattern.compile("<(entry|item)>(.*?)</(entry|item)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);				
	private Pattern title = Pattern.compile("<title>(.*)</title>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	private Pattern _date = Pattern.compile("<(pubDate|published)>(.*?)</(pubDate|published)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	/**
	 * Matches patterns in strings
	 */
	private Matcher itemsMatches;
	private Matcher _dateMatches;
	private Matcher titleMatches;
	private double sentiment;
	private Date date;
	private BasicDBObject query;
	private BasicDBObject update;
	private BasicDBObject updateEvaluated;
	private String collectionName;
	private String pubDate;
	private String contenidoItem;
	private String nombreArch;

	/**
	 * @param args 
	 * @param dao 
	 * @see HttpServlet#HttpServlet()
	 */   
	public  RssDownloader(ArrayList<String> ifeedsArray, ArrayList<String> iLanguage, DAO iDao) 
	{
		//carga las fuentes de datos

		feedsArray = ifeedsArray;
		language = iLanguage;
		dao = iDao;


	}

	/**
	 * Convierte un stream http en un string
	 * @param is
	 * @return
	 */
	@SuppressWarnings("resource")
	static String convertStreamToString(java.io.InputStream is)
	{
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\\\A");
		return s.hasNext() ? s.next() : "";
	}


	/**
	 * Runs the download thread
	 * It processes the sources and then sleeps for 5 minutes
	 */
	public void run() 
	{
		System.out.println("RSS Downloader Starts");
		tacEN = new TextAnalyzerCore("EN");
		tacSP = new TextAnalyzerCore("SP");
		while(!Thread.interrupted())
		{
			try
			{
				process();
				System.gc(); //calls the garbage collector just in case
				Thread.sleep(1000*60*3); //runs every 3 minutes
			}
			catch (InterruptedException e)
			{
				Util.printErrorMessage(e, "RSS");

				run();
			}


		}

	}

	/**
	 * Does the actual proccessing
	 */
	private void process() 
	{
		System.out.println();

		//downloads the feeds content
		ArrayList<String> feedsContent= new ArrayList<String>();
		for (int i=0; i<feedsArray.size(); i++)
		{
//			if(i==28)
//				System.out.println("Meet me Halfway");

			try 
			{
				// Descarga el contenido
				server = new URL(feedsArray.get(i));
				urlCon = server.openConnection();
				con = (HttpURLConnection)urlCon;
				//	String contenido = IOUtils. toString(con.getInputStream());					
				String content = convertStreamToString(con.getInputStream());
				feedsContent.add(content);
				con.disconnect();

			} 
			catch (Exception ex)
			{
				feedsContent.add(i,""); //previene errores, todos los arreglos tienen el mismo tamaño
//				ex.printStackTrace();
//				Util.printErrorMessage(ex, Thread.currentThread().getName(), "Feed no alcanzable");
			}
		}

		

		for (int j=0; j<feedsContent.size(); j++)
		{
			itemsMatches = items.matcher(feedsContent.get(j));
			while (itemsMatches.find())
			{
				evaluateArticle(j);
			};
		}

	}

	
	/**
	 * evaluates an article in a feed to get its feeling and verify if it already exists
	 * @param j
	 */
	private void evaluateArticle(int j)
	{
		contenidoItem = itemsMatches.group().toString();

		nombreArch = j + "_"; //posicion del feed en el arreglo
		// Saca los titulos
		titleMatches = title.matcher(contenidoItem);

		if(titleMatches.find())
			nombreArch += (titleMatches.group(1).replace("<title>", "").replace("</title>", "")).hashCode()+"_";//

		// Obtiene fecha
		_dateMatches = _date.matcher(contenidoItem);						

		if (_dateMatches.find())
		{
			pubDate = _dateMatches.group(2) ;
			nombreArch += pubDate.replaceAll(" ", "")+ "_";	
		}

		//compatibilidades del nombre de archivo
		nombreArch = nombreArch.replace(":","-");
		//				System.out.println(contenidoItem);
		if(language.get(j).equals("EN"))
		{
			collectionName = DAO.ENGLISHRSS;
		}
		else if (language.get(j).equals("SP"))
		{
			collectionName = DAO.SPANISHRSS;
		}

		query = new BasicDBObject("ID",nombreArch);
		
		//if the object already exists in the database then don{t evaluate it again
		if(dao.query(collectionName, query).hasNext())
		{
			return;
		}
		
		contenidoItem = Util.cleanRSS(contenidoItem);
		
		sentiment = 0.0;

		

		if(language.get(j).equals("EN"))
		{
			sentiment = tacEN.analyzeEnglishRss(contenidoItem);
		}
		else if (language.get(j).equals("SP"))
		{
			sentiment = tacSP.analyzeSpanishRss(contenidoItem);
		}
		date = dateFormatting(pubDate);//feed date
		
		update = new BasicDBObject("ID", nombreArch).append("DATE", date).append("SENTIMENT", sentiment).append("TEXT", contenidoItem).append("SOURCE", feedsArray.get(j));
		dao.insert( collectionName, update);
//		System.out.println(nombreArch);
		updateEvaluated = new BasicDBObject("DATE", date ).append("SENTIMENT", sentiment).append("TYPE", "RSS").append("LANGUAGE", language.get(j)).append("ID", nombreArch);
		dao.insert( DAO.EVALUATEDSENTIMENTS, updateEvaluated);
//		System.out.println("RSS Running: " + j+"/" + " Sentiment " + sentiment);
	}

	/**
	 * converts several date formats to a date object
	 * @return
	 */
	private static Date dateFormatting(String fechaOriginal)
	{

		String[] date_formats = {
				"EEE, dd MMM yyyy HH:mm:ss Z",
				"yyyy-MM-dd'T'HH:mm:ss'Z'",
		};

		for (String formatString : date_formats)
		{
			try
			{    
				SimpleDateFormat sdf = new SimpleDateFormat(formatString,Locale.ENGLISH);
				Date mydate = sdf.parse(fechaOriginal);
				return mydate;
			}
			catch (ParseException e)
			{

				//				System.out.println("mensaje:" +	e.getMessage() + "|causa:" +e.getCause());
				//tries next date formar
			}
		}
		return null;
	}



}
