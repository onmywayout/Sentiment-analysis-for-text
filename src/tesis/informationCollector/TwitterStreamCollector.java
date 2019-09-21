package tesis.informationCollector;

import java.util.concurrent.BlockingQueue;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterStreamCollector implements Runnable
{


	/**
	 * Comunication channel with twitter
	 */
	TwitterStream twitterStream;

	/**
	 * listenr of the twitterStream
	 */
	StatusListener listener;

	/**
	 * Coordinates to bound the tweets from
	 */
	double [][] coordinates;



	/**
	 * Used in the class as a reference of the country of interest for the tweets according to the chosen language
	 */
	private String countryCode;

	/**
	 * Used in the class as a reference of which language the stream is collecting tweets of
	 */
	private String language;
	/**
	 * a sycnrhonized list where all tweets (status) received are stored so other thread can processes them
	 */

	private BlockingQueue<Status> tweetList;

	/**
	 * keys to be used in the twitter application
	 */
	private String keys;

	/**
	 * keeps track of the number of error thrown
	 */
	private int count = 0;

	/**
	 * Keeps track of the number of null errors thrown from the listener
	 */
	private int nullErrors = 0;


	/**
	 * A flag inidcating whether the interrupt Thread actions has been invoked over this thread
	 */
	private boolean flag = false;

	/**
	 * Language to be collected by the stream: SP for Spanish or EN for English
	 * @param Language
	 * @param twitterKeys 
	 * @param spanishTweetList 
	 */
	public TwitterStreamCollector(String Language, String twitterKeys, BlockingQueue<Status> atweetList)
	{
		tweetList = atweetList;
		language = Language;
		keys = twitterKeys;

		if(Language.equals("SP"))
		{
			//			Colombian coordinates	 Cuadrado 1 (long, lat) 
			//				pasto -77.367676, 1.125403 			Cucuta -72.447773, 8.053173 			Rioacha -73.181885,11.366342

			coordinates = new double [][] {{-77.367676, 1.125403},{-72.447773, 11.366342}};
			countryCode = "CO";
		}
		else
		{
			//US Coordinates -125.00','25.0','-70.00','50.00',
			coordinates = new double [][] {{-125.00,25.0},{-70.00, 50.00}};
			countryCode = "US";
		}
	}

	/**
	 * Starts the thread
	 */
	public void run() 
	{
		try
		{
			
			if(listener!=null)
			{
				stopThread();
			}

			StartCollecting();
		}
		catch(Exception e)
		{
			System.out.println("Soy yo, Twitter, el problematico");
			Util.printErrorMessage(e,"TWITTER"+language,"DEMASIADOS ERRORES TWITTER: RELAUNCHING:Count:"+count);
			if(count>200)//demasiados errores
				stopThread();
			count++;
			if(!flag) //if it was not interrupted, then relaunch
				run();

		}

	}

	private void StartCollecting() 
	{
		System.out.println("Twitter collector " + language+" : I am Alive!");

		String access [] = keys.split(";");

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(access[0]); //credentials for @iFeelYourTweet
		cb.setOAuthConsumerSecret(access[1]);
		cb.setOAuthAccessToken(access[2]);
		cb.setOAuthAccessTokenSecret(access[3]);

		listener = statusListener();


		twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		FilterQuery fq = new FilterQuery();

		fq.locations(coordinates);
		twitterStream.addListener(listener);
		twitterStream.filter(fq);
	}

	/**
	 * Stops this thread by killing the stream 
	 */
	private void stopThread()
	{
		twitterStream.cleanUp();
		twitterStream.shutdown();
		listener = null;

	}



	/**
	 * does the actual listening process for the Streaming
	 * @return returns the listening interface
	 */
	private StatusListener statusListener() 
	{
		StatusListener listener = new StatusListener()
		{
//			long inicio = System.currentTimeMillis();
//			long actual = 0;
			
			/**
			 * For each status received..
			 */
			public void onStatus(Status status) 
			{

				
				try
				{


					if(status.getPlace().getCountryCode().equals(countryCode))  //checks if it comes from Colombia, otherwise ignores it
					{
						int p = tweetList.size();
//						if(p%1000 ==0 && p >0)
//						System.out.println("Tweeter:" +tweetList.size());
						if(p>3500) //if too much elements are on queue it might exceed memory capacity
						{
							Util.printErrorMessage(new Exception("Queue Sofia capactiy Exceeded"), "Twitter"+language, "Queue Capacity exceeed:"+tweetList.size());
							tweetList.clear();
							//cl 26 # 92 '32 dificio G4 local 4
							System.gc();
						}
//						inicio = System.currentTimeMillis();
						tweetList.offer(status);
//						if((inicio - actual)>10)
//							System.out.println("---------Twitter:" + (inicio - actual));
//						actual = inicio;
					}
//					System.out.println(actual-inicio);

					if(Thread.interrupted())//if the thread interrupted method was called
					{
						flag = true;
						stopThread();
					}
				}
				catch (NullPointerException e) //atrapa errores null del status
				{
					nullErrors++;
					if(nullErrors%2000 == 0)
					{
						Util.printErrorMessage(e, "Twitter"+language, "NullError:"+nullErrors);
					}

				}

			}
			

			/**
			 * Twitter unused methods for the action listener
			 */
			public void onException(Exception arg0) 
			{	Util.printErrorMessage(arg0, "TWITTERlistener"+language);		}
			public void onTrackLimitationNotice(int arg0) 
			{			}
			public void onStallWarning(StallWarning arg0) 
			{			}
			public void onScrubGeo(long arg0, long arg1) 
			{			}
			public void onDeletionNotice(StatusDeletionNotice arg0) 
			{			}
		};
		return listener;
	}







}
