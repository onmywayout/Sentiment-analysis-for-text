package tesis.ProcessHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import tesis.PredictionUnit.PredicterCore;
import tesis.informationCollector.SourceHandler;
import tesis.informationCollector.Util;

/**
 * This is the entry point for the complete solution
 * It manages all components within the system and is responsible for the lifecycle of them
 * 
 * @author Nicolas
 *
 */
public class Orchestrator
{
	/**
	 * Parameter: File from where to load the RSSColector source URLs
	 */
	private String rssSources;

	/**
	 * Parameter: File from where to load companies Nemotecnics that will be evaluated
	 */
	private String stockCompanies;

	/**
	 * Parameter: File from where to load the google trends report (in csv format downladed from google)
	 */
	private String googleFile;

	/**
	 * InformationCollector component
	 * While this reference is maintained all the information collector threads will function
	 */
	private SourceHandler sourceHandler;


	/**
	 * Prediction Unit component
	 */
	private PredicterCore predicterCore;


	/**
	 * Storage location for errors
	 */
	static String StoragePath = System.getProperty("user.home")+  java.io.File.separator + "SOFIAERRORS" + java.io.File.separator;

	/**
	 * URL of the mongo database to be used 
	 */
	public String dbInstance;

	/**
	 * References to the sources that will be collected
	 */
	private boolean twitterSpanish;
	private boolean twitterEnglish;
	private boolean rss;
	private boolean google;
	private boolean stock;

	/**
	 * Indicates wheter or not this instance will generate predictions
	 */
	private boolean predicter;

	/**
	 * A boolean that defines whether the application should run ignoring the trading hours
	 */
	private boolean test;

	/**
	 * Access credentials to twitter
	 */
	private String tweeterKeys;

	/**
	 * Boolean array containing which collector will be initialized based on the parameters file
	 */
	private ArrayList<Boolean> collectors;

	/**
	 * Variables used to calculate the trading hours time
	 */
	private Calendar calendar;

	private int day;

	private Calendar compareBefore;

	private Calendar compareAfter;

	/**
	 * The predicter Core thread
	 */
	public Thread threadPredicter;

	/**
	 * Starts the application and runs the main loop
	 * @param params file with the configuration variables
	 */
	public Orchestrator(String params)
	{
		int count = 0;
		try
		{

			loadConfiguration(params);

			StartInformationCollectors();
			

			if(predicter)
			{
				predicterCore = new PredicterCore();
				threadPredicter = new Thread(predicterCore);
				threadPredicter.setName("threadPredicter");
			}

			applicationloop();


		}
		catch(Exception e) //unknown exception
		{

			Util.printErrorMessage(e, "ORCHESTRATOR" + count);
			if(test||count<5) //if it is a test keep running, otherwise only 5 chances
			{
				loadConfiguration(params);

				StartInformationCollectors();


				if(predicter)
				{
					predicterCore = new PredicterCore();
					threadPredicter = new Thread(predicterCore);
					threadPredicter.setName("threadPredicter");
				}
				applicationloop(); //starts it one more time
			}

			count++;

		}
	}


	/**
	 * The application loop
	 * It keeps the application functioning indefinitely
	 */
	private void applicationloop() 
	{

		//set stock collector to operate between 7:40 and 1:20pm in trading days
		boolean TD = tradingDay();
		boolean TH = tradingHours();
		boolean operating = false;
		while(true)
		{
			TD = tradingDay();
			TH = tradingHours();
			if((TD && TH) || test) //if it is stock time or a test; run; then act
			{
				if(!operating) //if the component is not running the threads start it
				{
					sourceHandler.startCollecting();
					if(predicter) //if this instance generates predictions
					{
						threadPredicter.start(); 
					}
					operating = true;
				}

				try
				{
					Thread.sleep(60*1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}

			}
			else //if it is not a trading day make the application sleep
			{
				if(operating)
				{
					
					sourceHandler.stopCollecting();
					if(predicter) //if this instance generates predictions
					{
						threadPredicter.interrupt(); 
					}
					operating = false;
				}
				try
				{



					Thread.sleep(timetoNextTradingDay()); //sleeps until next morning 8am
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					Util.printErrorMessage(e, "ORCHESTRATOR");
				}
			}
		}

	}


	/**
	 * returns the remainig time in milliseconds until the next  day at 8 am
	 * @return
	 */
	private long timetoNextTradingDay() 
	{
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));

		Calendar tobe =  Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));

		tobe.set(Calendar.DAY_OF_YEAR, current.get(Calendar.DAY_OF_YEAR)+1); //will throw error on 31 december
		tobe.set(Calendar.HOUR_OF_DAY, 8);
		tobe.set(Calendar.MINUTE, 0);
		tobe.set(Calendar.SECOND, 10);

		long response = tobe.getTimeInMillis() - current.getTimeInMillis() ; // 10 seconds are added for more reliability in the waking time

		return response;
	}



	/**
	 * Invokes the Information component  handler to set basic configuration
	 */
	private void StartInformationCollectors() 
	{
		ArrayList<String> feedsArray= new ArrayList<String>();
		ArrayList<String> idioma= new ArrayList<String>();
		ArrayList<String> observed= new ArrayList<String>();
		//reads the RSS parameters file
		try 
		{

			BufferedReader in = new BufferedReader(new FileReader(rssSources));
			String line;
			String temp [] ;
			while((line = in.readLine()) != null )
			{
				line = line.trim();
				if(line!="") //http://feeds.huffingtonpost.com/huffingtonpost/LatestNews;ENG
				{
					temp = line.split(";");
					feedsArray.add(temp[0]);
					idioma.add(temp[1]);
				}
			}
			in.close();

			//reads the observed companies file


			in = new BufferedReader(new FileReader(stockCompanies));
			while((line = in.readLine()) != null )
			{
				line = line.trim();
				if(line!="") //http://feeds.huffingtonpost.com/huffingtonpost/LatestNews;ENG
				{
					observed.add(line);
				}
			}
			in.close();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
			System.out.println("ERROR WHILE READING INITIALIZATION FILES : " + e1.getMessage() + "|||\n|||" + e1.getCause());
			Util.printErrorMessage(e1, "ORCHESTRATOR:LOADPARAMS");
		}

		sourceHandler = new SourceHandler(feedsArray,idioma,observed,dbInstance,googleFile,rssSources,collectors, tweeterKeys); 
	}



	/**
	 * Loads the parameters to be used during the execution
	 * @param params path of the text file with all the parameters
	 */ 
	private void loadConfiguration(String params) 
	{


		try 
		{

			BufferedReader in = new BufferedReader(new FileReader(params));
			String line;
			line = in.readLine();
			while (line.startsWith("#"))
			{
				line = in.readLine();
			}

			//the lines starting with #  at the begining of the file are not read
			rssSources = line;
			stockCompanies = in.readLine(); 
			googleFile = in.readLine();
			dbInstance = in.readLine();
			String temp [] = in.readLine().split(";");
			//3. list with feeds this instance will collect, separated by ; GoogleTrends;RSS;TwitterEnglish;TwitterSpanish;StockTracker
			google = Boolean.parseBoolean(temp[0]);
			rss = Boolean.parseBoolean(temp[1]);
			twitterEnglish= Boolean.parseBoolean(temp[2]);
			twitterSpanish = Boolean.parseBoolean(temp[3]);
			stock = Boolean.parseBoolean(temp[4]);
			//wheter this isntance will generate predictions
			predicter = Boolean.parseBoolean(in.readLine());
			//twetter authentication keys
			tweeterKeys = in.readLine();
			//igoogle,  irss,  itwitterEnglish,  itwitterSpanish,  iStock
			collectors = new ArrayList<Boolean>();
			collectors.add(google);
			collectors.add(rss);
			collectors.add(twitterEnglish);
			collectors.add(twitterSpanish);
			collectors.add(stock);
			test = false;
			try
			{
				test = Boolean.parseBoolean(in.readLine());
			}
			catch(Exception notImportant)
			{}


			in.close();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
			Util.printErrorMessage(e1, "ORCHESTRATOR");
			System.out.println("ERROR WHILE READING THE CONFIGURATION FILES");
		}



	}

	/**
	 * Verifies if the current day is a trading day
	 * @returns false if it is saturday or sunday, true otherwise
	 */
	private boolean tradingDay() 
	{
		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));
		day = calendar.get(Calendar.DAY_OF_WEEK);

		if(day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			return false;
		else
			return true;
	}

	/**
	 * Verifies if the current hours are trading hours 
	 * @returns true if it is between 8 and 13:21
	 * @return
	 */
	private boolean tradingHours()
	{

		calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));

		compareBefore = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));

		compareBefore.set(Calendar.HOUR_OF_DAY, 13);
		compareBefore.set(Calendar.MINUTE, 21);

		compareAfter = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));

		compareAfter.set(Calendar.HOUR_OF_DAY, 7);
		compareAfter.set(Calendar.MINUTE, 40);

		if( calendar.after(compareAfter) && calendar.before(compareBefore)) // if the current time is between 8 and 1:20pm
			return true;
		else
			return false;

	}


	/**
	 * Entrypoint for the application
	 * @param args
	 */
	public static void main(String[] args) 
	{
		//./datos/params/params.txt
		new Orchestrator((args.length ==0)?"./datos/params/params.txt":args[0]); 
	}

}
