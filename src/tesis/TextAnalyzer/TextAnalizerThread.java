package tesis.TextAnalyzer;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import tesis.PersistentStorage.DAO;
import tesis.informationCollector.Util;
import twitter4j.Status;

import com.mongodb.BasicDBObject;

public class TextAnalizerThread implements Runnable
{

	/**
	 * Database access
	 */
	private DAO dao;

	/**
	 * Used in the class as a reference of which language the stream is collecting tweets of
	 */
	private String language;

	/**
	 * Tool for text analysis
	 */
	private TextAnalyzerCore tac;

	/**
	 * Used in the class as a reference to the collection  the stream is collecting tweets on according to the selected language
	 */
	private String collectionName;

	/**
	 * a sycnrhonized queue where all tweets (status) received are retrieved to be processed
	 */

	private boolean flag = false;

	/**
	 * List reused everytime the tweet list is processed
	 */
	//	private LinkedList<Status> toProcess;

	private int count = 0;



	private BlockingQueue<Status> tweetList;

	private BasicDBObject insert;

	private String clean;

	private double sentiment;

	private long tiempoInicio;

	private Status st;

	public TextAnalizerThread(String Language, DAO iDao, BlockingQueue<Status>  atweetList)
	{
		Thread.currentThread().getName();
		language = Language;
		dao = iDao;
		tweetList = atweetList;
		//		toProcess = new LinkedList<Status>();
		if(Language.equals("SP"))
		{
			collectionName = DAO.SPANISHTWEETS;
		}
		else
		{
			collectionName = DAO.ENGLISHTWEETS;
		}

	}

	public void run()
	{
		tac = new TextAnalyzerCore(language);
		while(!Thread.interrupted() && !flag)
		{

			try
			{
				leerLista();
				//				
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	/**
	 * Reads the status list, drains it to other list and iterates over the last one to get the sentiments
	 * @throws InterruptedException 
	 */
	private void leerLista() throws InterruptedException
	{
		st = tweetList.poll();
		if(st!=null)
		{
			procesarStatus(st,tiempoInicio);
		}
		else
		{
			Thread.sleep(500);
		}
	}

	/**
	 * Procesa cada estado recibido de tweeter 
	 */
	private Status procesarStatus(Status status, long inicio)
	{
		count++;
		clean = Util.cleanTweet(status.getText()+" ", language);
		sentiment = -2.0;
		try
		{
			sentiment = getTextSentiment(clean);
		}
		catch(NegativeArraySizeException ef)
		{
			//not really important. one tweet could not be lemmatized
		}


		BasicDBObject basicObj = new BasicDBObject();
		basicObj.put("tweet_ID", status.getId());
		basicObj.put("TEXT", clean);
		basicObj.put("DATE", new Date());
		basicObj.put("SENTIMENT", sentiment);
		if( status.getGeoLocation()!=null)
		{ 
			basicObj.put("LATITUDE", status.getGeoLocation().getLatitude());
			basicObj.put("LONGITUDE", status.getGeoLocation().getLongitude());
		}
		try
		{

			dao.insert(collectionName,basicObj);
			
			insert = new BasicDBObject("DATE", new Date() ).append("SENTIMENT", sentiment).append("TYPE", "TWEET").append("LANGUAGE", language).append("ID", status.getId());
			dao.insert( DAO.EVALUATEDSENTIMENTS, insert);

			//			actual = System.currentTimeMillis();
			//			System.out.print("|| 3 : " + ( actual - inicio));
			//			inicio = actual;

		} 
		catch (Exception e) 
		{
			System.out.println("MongoDB Connection Error : " + e.getMessage());
			e.printStackTrace();
			Util.printErrorMessage(e, "TWITTER"+language);
		}
		return null;

	}



	/**
	 *  Uses the text analysis component to get the sentiment of th etext passed as parameter
	 *  
	 */
	private double getTextSentiment(String clean) 
	{

		if(count%2900 ==0)
			System.out.println("Count:" + count);
		if(count>3000)
		{

			count = 0;
			tac = new TextAnalyzerCore(language);
			System.gc();
		}
		if(language.contains("SP"))
		{
			return	tac.analyzeSpanishText(clean);
		}
		else
		{
			return tac.analyzeEnglishText(clean);
		}
	}

}
