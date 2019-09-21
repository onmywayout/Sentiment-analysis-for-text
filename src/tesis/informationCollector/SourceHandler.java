package tesis.informationCollector;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import tesis.PersistentStorage.DAO;
import tesis.TextAnalyzer.TextAnalizerThread;
import twitter4j.Status;

import com.mongodb.BasicDBObject;

/**
 * Handles all the sources connections,
 * Acts a central start point for imformation collection in different threads
 * 
 * @author Nicolas
 *
 */
public class SourceHandler
{

	/**
	 * Thread Twitter Stream Collector English
	 */
	public Thread TtscE;

	/**
	 * Thread Twitter Stream Collector Spanish
	 */
	public Thread TtscS;

	/**
	 * Thread Twitter Stream Collector English Text analyzer
	 */
	public Thread TtscETA1;
	public Thread TtscETA2;

	/**
	 * Thread Twitter Stream Collector Spanish Text analyzer
	 */
	public Thread TtscSTA;

	/**
	 * Rss Thread
	 */
	public Thread Trd;
	


	/**
	 * Stock Tracker Thread
	 */
	public Thread Tst;

	/**
	 * Database access
	 */
	private DAO dao;

	private String twitterKeys;

	/**
	 * File from where the google data will be loaded (in csv)
	 */
	private String googleFile;
	
	
	/**
	 * The list where the twitter spanish thread adds collected tweets and spanish text analyzer threads processes them from
	 */
	private BlockingQueue<Status> spanishTweetList ;

	/**
	 * The list where the twitter english thread adds collected tweets and english text analyzer threads processes them from
	 */
	private BlockingQueue<Status> englishTweetList;

	/**
	 * Catches exceptions thrown by the collecting threads
	 */
	private UncaughtExceptionHandler catcher = new Thread.UncaughtExceptionHandler() //catches the threads exceptions
	{
		public void uncaughtException(Thread th, Throwable ex) 
		{
			System.out.println("Uncaught exception: " + ex);
			Util.printErrorMessage(ex, "SOURCEHANDLER:"+ th.getName());
		}
	};


	/**
	 * Booleans that determine if each one of the collectors will be initiated
	 */
	boolean rss ;
	boolean twitterEnglish ;
	boolean twitterSpanish ;
	boolean stock ;
	boolean google ;

	private Thread TGT;


	/**
	 * sets the information parameters for the different threads that collect data
	 * @param RssParams  URLS of the RSS feeds to be downloaded
	 * @param RssParamsLang Language of the RSS feeds to be downloaded
	 * @param observedCompanies file with the Nemotecnics of the companies whose stock is going to be followed
	 * @param DbInstance URl of the database to be used
	 * @param collectors array of booleans defining which instances to initialize 
	 *  igoogle,  irss,  itwitterEnglish,  itwitterSpanish,  iStock
	 * 
	 */
	public SourceHandler(ArrayList<String> RssParams,ArrayList<String> RssParamsLang, ArrayList<String> observedCompanies,
			String dbInstance, String igoogleFile,String sourcesFile, ArrayList<Boolean> collectors, String atwitterKeys)
	{

		//boolean igoogle, boolean irss, boolean itwitterEnglish, boolean itwitterSpanish, boolean iStock
		google = collectors.get(0);
		rss = collectors.get(1);
		twitterEnglish = collectors.get(2);
		twitterSpanish = collectors.get(3);
		stock = collectors.get(4);

		dao = DAO.getInstance(dbInstance);
		googleFile = igoogleFile;
		twitterKeys = atwitterKeys;
		//so it loads the previously created models from the same directory as the google data 
		String modelsPath = new File(sourcesFile).getParentFile().getAbsolutePath() + java.io.File.separator;
		System.out.println(modelsPath);
		dao.setModelsPath(modelsPath);

		try
		{
			String ip =InetAddress.getLocalHost().getHostAddress();
			String services = (google? "Google":"") + ";"  + (rss? "rss":"") + ";"  + (twitterEnglish? "twitEng":"") + ";"  + (twitterSpanish? "twitSpa":"") + ";"  +  (stock? "stock":"");
			BasicDBObject store = new BasicDBObject("DATE" , new Date()).append("INFO:","ALIVE").append("ORIGIN", "INFO").append("SERVICES", services).append("IP", ip);
			dao.insert(DAO.STATUS, store );
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}

		spanishTweetList = new LinkedBlockingQueue<Status>();
		englishTweetList  = new LinkedBlockingQueue<Status>();

		if(twitterSpanish == true)
		{
			TwitterStreamCollector tscS = new TwitterStreamCollector("SP",  twitterKeys,spanishTweetList);

			TtscS = new Thread(tscS);
			TtscS.setName("TwitterSpanish");
			TtscS.setUncaughtExceptionHandler(catcher);

		}

		if(twitterEnglish == true)
		{
			TwitterStreamCollector tscE = new TwitterStreamCollector("EN", twitterKeys,englishTweetList);
			TtscE = new Thread(tscE);
			TtscE.setName("TwitterEnglish");
			TtscE.setUncaughtExceptionHandler(catcher);
		}
		
		
		if(twitterSpanish == true)
		{
			TextAnalizerThread tscSTA = new TextAnalizerThread("SP", dao,spanishTweetList);

			TtscSTA = new Thread(tscSTA);
			TtscSTA.setName("TwitterSpanishTA");
			TtscSTA.setUncaughtExceptionHandler(catcher);

		}

		if(twitterEnglish == true)
		{
			TextAnalizerThread tscETA1 = new TextAnalizerThread("EN", dao,englishTweetList);
			TtscETA1 = new Thread(tscETA1);
			TtscETA1.setName("TwitterEnglishTA1");
			TtscETA1.setUncaughtExceptionHandler(catcher);
			
			
			TextAnalizerThread tscETA2 = new TextAnalizerThread("EN", dao,englishTweetList);
			TtscETA2 = new Thread(tscETA2);
			TtscETA2.setName("TwitterEnglishTA2");
			TtscETA2.setUncaughtExceptionHandler(catcher);
		}
		
		if(stock == true)
		{
			StockTracker st = new StockTracker(observedCompanies,dao);
			Tst = new Thread(st);
			Tst.setName("StockTracker");
			Tst.setUncaughtExceptionHandler(catcher);
		}

		if (rss == true) //if the isntance downlaods the rss it also downlaods google
		{
			RssDownloader rd = new RssDownloader(RssParams, RssParamsLang, dao);
			Trd = new Thread(rd);
			Trd.setName("Rss");
			Trd.setUncaughtExceptionHandler(catcher);
		}
		if(google == true)
		{
			GoogleTrends gt = new GoogleTrends(googleFile,dao);
			TGT = new Thread(gt);
			TGT.setName("GoogleTrends");
			TGT.setUncaughtExceptionHandler(catcher);
		}



	}



	/**
	 * Stops the current information collection threads
	 */
	public void stopCollecting()
	{
		if(twitterEnglish == true)
		{
			TtscE.interrupt();
			TtscETA1.interrupt();
			TtscETA2.interrupt();
		}
		if(twitterSpanish == true)
		{
			TtscS.interrupt();
			TtscSTA.interrupt();
		}

		if(stock == true)
		{
			Tst.interrupt(); 
		}

		if (rss == true)
		{
			Trd.interrupt();
		}
		if(google == true)
		{
			TGT.interrupt();
		}

	}

	/**
	 * starts the information collection threads
	 */
	public void startCollecting()
	{
		if(twitterEnglish == true)
		{
			TtscE.start();
			TtscETA1.start();
			TtscETA2.start();
		}
		if(twitterSpanish == true)
		{
			TtscS.start();
			TtscSTA.start();
		}

		if(stock == true)
		{
			Tst.start(); 
		}

		if (rss == true)
		{
			Trd.start();
		}
		if(google == true)
		{
			TGT.start();
		}

	}

	/**
	 * returns the current connection to the database
	 * @return
	 */
	public DAO getDao()
	{
		return dao;
	}



}
