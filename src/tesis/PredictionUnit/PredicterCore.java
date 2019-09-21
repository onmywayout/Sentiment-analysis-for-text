package tesis.PredictionUnit;

import java.util.Date;
import java.util.LinkedList;

import tesis.PersistentStorage.DAO;
import tesis.informationCollector.Util;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


/**
 * This class references all the information collected by the other components of the solution 
 * and processes it to output a prediction
 * @author Nicolas
 *
 */
public class PredicterCore implements Runnable
{

	/**
	 * Coefficient for RSS sentiment multiplication
	 */
	double rssCoef = 0.33;

	/**
	 * Coefficient for twitter sentiment multiplication
	 */
	double twitterCoef = 0.33;

	/**
	 * Coefficient for google trends multiplication
	 */
	double googleTrendsCoef = 0.33;

	/**
	 * Coefficient for facebook sentiment multiplication
	 */
//	double fbCoef = 0.2; //unused


	/**
	 * Database access
	 */
	private DAO dao;

	public PredicterCore()
	{

		dao = DAO.getInstance("");
	}



	private void generatePrediction( Date current)
	{


		String type = "TWEET";
		String language = "EN";
		double tweetSP[] = aggregateAllSentiments(type, language, current);

		type = "TWEET";
		language = "SP";
		double tweetEN[] = aggregateAllSentiments(type, language, current);

		type = "RSS";
		language = "SP";
		double rssSP[] = aggregateAllSentiments(type, language, current);

		type = "RSS";
		language = "EN";
		double rssEN[] = aggregateAllSentiments(type, language, current);

		double GT = getGoogleSentiment(current); 


		//this should range from [-1 , 1]
		double AverageSentiment[] = getAverageFeelings(tweetEN,tweetSP,rssEN,rssSP,GT); 

		double COPUSDVar = 0.05;


		//get the values from the current element
		BasicDBObject queryUntil = new BasicDBObject("DATE", current);
		BasicDBObject currentObj = (BasicDBObject) dao.query(DAO.FOREXMARKET, queryUntil).next();
		double currentVal = 0;
		try
		{
			currentVal = currentObj.getDouble("VALUE");
		}
		catch (Exception e)
		{
			try
			{
				currentVal = currentObj.getDouble("VALUEY");
			}
			catch(Exception ef)
			{
				currentVal = currentObj.getDouble("VALUEG");
			}
		}

		double predictionCOP[] =  new double[4];

		for (int i = 0; i < predictionCOP.length; i++)
		{
			predictionCOP[i] = truncate((currentVal* ( 1 + (COPUSDVar*AverageSentiment[i])))) ;
		}

		System.out.println(current.toString());
		BasicDBObject storeResult = new BasicDBObject("DATE", current).append("COPACTUAL", currentVal).append("10COPPREDICTED", predictionCOP[0])
				.append("30COPPREDICTED", predictionCOP[1]).append("60COPPREDICTED", predictionCOP[2]).append("2COPPREDICTED", predictionCOP[3]);
//				.append("24COPPREDICTED", predictionCOP[4]);



		dao.insert( DAO.RESULTS, storeResult); //updates collection to be used by GUI


		BasicDBObject storeSentimentStore = new BasicDBObject("DATE", current).append("COPACTUAL", currentVal).append(  "GOOGLESENTIMENT", GT)

				.append("10SENTIMENT",AverageSentiment[0]).append("10TWITTERENGLISH", tweetEN[0]).append("10TWITTERSPANISH",tweetSP[0]).append("10RSSENGLISH", rssEN[0]).append("10RSSSPANISH", rssSP[0])
				.append("30SENTIMENT",AverageSentiment[1]).append("30TWITTERENGLISH", tweetEN[1]).append("30TWITTERSPANISH",tweetSP[1]).append("30RSSENGLISH", rssEN[1]).append("30RSSSPANISH", rssSP[1])
				.append("60SENTIMENT",AverageSentiment[2]).append("60TWITTERENGLISH", tweetEN[2]).append("60TWITTERSPANISH",tweetSP[2]).append("60RSSENGLISH", rssEN[2]).append("60RSSSPANISH", rssSP[2])
				.append("2SENTIMENT",AverageSentiment[3]).append("2TWITTERENGLISH", tweetEN[3]).append("2TWITTERSPANISH",tweetSP[3]).append("2RSSENGLISH", rssEN[3]).append("2RSSSPANISH", rssSP[3]);
//				.append("24SENTIMENT",AverageSentiment[4]).append("24TWITTERENGLISH", tweetEN[4]).append("24TWITTERSPANISH",tweetSP[4]).append("24RSSENGLISH", rssEN[4]).append("24RSSSPANISH", rssSP[4]);

		dao.insert(DAO.RESULTSDETAIL, storeSentimentStore); //updates colleciton to be used by GUI

		//		System.out.println(predictionCOP[0] + " | " + predictionCOP[1] + " | " + predictionCOP[2] + " | " + predictionCOP[3] + " | " + predictionCOP[4] );

				System.out.println();
	}


	/**
	 * Gets the average sentiments for each one of the time frames taken into consideration
	 * @return an array with the avearage feeling calculated for 10,30 and 60 minutes and 2 and 24 hours
	 */
	private double[] getAverageFeelings(double[] tweetEN, double[] tweetSP, double[] rssEN, double[] rssSP, double GT)
	{

		double response [] = new double [4];

		for (int i = 0; i < response.length; i++)
		{
			response[i] = truncate(GT*googleTrendsCoef + rssEN[i]*rssCoef/2 + rssSP[i]*rssCoef/2 
					+ tweetEN[i]*twitterCoef/2 + tweetSP[i]*twitterCoef/2);
		}

		return response;
	}



	/**
	 * Code simplification, creates and array with all the aggregate queries that have to be made
	 * @param type
	 * @param language
	 * @param current
	 * @return
	 */
	private double [] aggregateAllSentiments(String type, String language, Date current)
	{
		double [] response =  {
				aggregateSentiment(type, language,current,10),
				aggregateSentiment(type, language,current,30),
				aggregateSentiment(type, language,current,60),
				aggregateSentiment(type, language,current,120),
		};
		return response;
	}

	/**
	 * Does an aggregation query over the evaluated sentiments collection in order to get the average feeling for the period of time passed
	 * as a parameter
	 * @param type Rss, Tweet or GT
	 * @param language EN or SP
	 * @param current Date that is going to be taken as the upper bound of the interval upon which the query will be made
	 * @param nminutes number of minutes that should be taken as timeframe for the aggregation query
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private double aggregateSentiment(String type, String language, Date current, int nminutes)
	{
		//		the mongo query to be translated (identical but the latest is more efficient)	
		//		db.evaluatedsentiments.aggregate( {$match: {DATE :{$gt: new ISODate('2014-05-04T23:59:59.000Z') ,$lt: new ISODate('2014-05-06T23:59:59.000Z') }}}, {$match: {TYPE:"TWEET"}}, {$match: {LANGUAGE:"SP"}} ,{$group: {_id: null, PROM: { $avg: "$SENTIMENT"}}})   
		//		db.evaluatedsentiments.aggregate( {$match: {DATE :{$gt: new ISODate('2014-06-12T22:59:59.000Z') ,$lt: new ISODate('2014-06-12T23:59:59.000Z') },TYPE:"TWEET",LANGUAGE:"SP"}} ,{$group: {_id: null, PROM: { $avg: "$SENTIMENT"}}})

		Date	from = new Date(current.getTime()-1000*60*nminutes);// nnimnutes minutes before

		BasicDBObject timeFramedQuery = new BasicDBObject("DATE", new BasicDBObject("$gte",from).append("$lte", current));

		//{$match: {DATE :{$gt: new ISODate('2014-06-12T22:59:59.000Z') ,$lt: new ISODate('2014-06-12T23:59:59.000Z') },TYPE:"TWEET"}} 
		DBObject match = new BasicDBObject("$match",timeFramedQuery.append("LANGUAGE",language).append("TYPE", type));

		//{$group: {_id: null, PROM: { $avg: "$SENTIMENT"}}})
		DBObject group = new BasicDBObject("$group", new BasicDBObject("_id","null").append("AVER", new BasicDBObject("$avg", "$SENTIMENT")));

		DBCollection evaluatedsentiments = dao.getRawCollection(DAO.EVALUATEDSENTIMENTS);


		LinkedList<DBObject> parameters = new LinkedList<>();
		parameters.add(match); 
		parameters.add(group);


		AggregationOutput output = evaluatedsentiments.aggregate( parameters);//runs the aggregation query

		BasicDBObject result = output.getCommandResult();

		BasicDBList  t = ((BasicDBList)result.get("result"));

		if(t.size()>0)
		{
			return truncate(((BasicDBObject) t.get(0)).getDouble("AVER"));
		}
		else
			return 0.0000001;

	}







	/**
	 * Calculates the google sentiment, which is the percentage of deviation from the last week value
	 * if previous week values is 60 and the current value for the last week (most recent data)is 30, the sentiment is effectively -0.5
	 *  If the current value equals that of the mean then the current sentiment is 0
	 * @return
	 */

	private double getGoogleSentiment(Date current) 
	{

		////gets the average for the previous last 2 weeks of all fields and then substracts the value of the last week
		// then calculates the deviation of that last value from the previously found value

		//gets the last  inserted documents
		//db.foo.find().sort({_id:1}).limit(1);
		long offset = 3600*24*7*2*((long)1000);
		Date	from = new Date(     ( (long)(current.getTime() - offset)   ));// 2 weeks before --if not converted again to long the numbers become negatives
		BasicDBObject timeFramedQuery = new BasicDBObject("DATE", new BasicDBObject("$gte",from).append("$lte", current));

		BasicDBObject sort = new BasicDBObject("DATE",-1);
		DBCursor last4 = dao.getRawCollection(DAO.GOOGLETRENDS).find(timeFramedQuery).sort(sort);//
		//		DBCursor last4 = dao.query(DAO.GOOGLETRENDS, timeFramedQuery);

		double dolarHoy = 0;
		double precioDolar = 0;
		double trm = 0;
		double dolar = 0;
		BasicDBObject last = (BasicDBObject) last4.next(); //we make sure not to take the last element of the list

		int n = 0;
		BasicDBObject element;
		while (last4.hasNext())
		{
			element = (BasicDBObject) last4.next();
			dolarHoy += element.getDouble("DOLARHOY");
			precioDolar += element.getDouble("PRECIODOLAR");
			trm += element.getDouble("TRM");
			dolar += element.getDouble("DOLAR");
			n++;
		}


		double deviationDolarHoy = 0;
		double deviationPrecioDolar = 0;
		double deviationTrm = 0;
		double deviationDolar = 0;

		//calculates the actual deviation from the average (last - average)/average
		deviationDolarHoy = (last.getDouble("DOLARHOY") - (dolarHoy/n))/(dolarHoy/n);
		deviationPrecioDolar = (last.getDouble("PRECIODOLAR") - (precioDolar/n))/(precioDolar/n);
		deviationTrm = (last.getDouble("TRM") - (trm/n))/(trm/n);
		deviationDolar = (last.getDouble("DOLAR") - (dolar/n))/(dolar/n);


		//calculates the average of the 4 variables
		Double average = ((deviationDolarHoy + deviationPrecioDolar + deviationTrm + deviationDolar))/4;

		return truncate(average);
	}



	/**
	 * truncates to 11 digits
	 * @param average
	 * @return
	 */
	private double truncate(Double average)
	{
		String tmp = Double.toString(average);
		String dosDecimales = tmp.length()>11? tmp.substring(0, 11) : tmp;
		return  Double.parseDouble(dosDecimales);
	}

/*
	public static void main(String[] args) 
	{

		PredicterCore objeto = new PredicterCore();

		DAO dao = DAO.getInstance();

		//int dia = 125 - 30; // 14 de abril


		//sets caendars for the end and begginig of this day so a time frame can be used in the query
		Calendar dayEnd = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));
		Calendar dayStart = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));



		dayStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(args[0]));
		dayStart.set(Calendar.MONTH, Integer.parseInt(args[1])-1);
		dayStart.set(Calendar.HOUR_OF_DAY, 00);
		dayStart.set(Calendar.MINUTE, 00);

		dayEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(args[2]));
		dayEnd.set(Calendar.MONTH, Integer.parseInt(args[3])-1);
		dayEnd.set(Calendar.HOUR_OF_DAY, 22);
		dayStart.set(Calendar.MINUTE, 59);


		BasicDBObject timeFramedQuery = new BasicDBObject("DATE", 
				new BasicDBObject("$gte",new Date(dayStart.getTimeInMillis()))
		.append("$lte", new Date(dayEnd.getTimeInMillis())));



		//	BasicDBObject query = new BasicDBObject();
		BasicDBObject sort = new BasicDBObject("DATE",1);
		Cursor forexQuotes = dao.sort(DAO.FOREXMARKET, timeFramedQuery, sort);

		Date current = (Date) forexQuotes.next().get("DATE");

		Date next = null;

		try
		{
			while(forexQuotes.hasNext())
			{

				//selects the appropiate type of source
				next = (Date) forexQuotes.next().get("DATE");

				if((next.getTime())-current.getTime()>1000*60*9) //9 minutes difference
				{
					objeto.generatePrediction(current);
					//					objeto.getGoogleSentiment(current);
					current = next;
				}

			}
		}
		catch(Exception e)
		{
			System.out.println("The list is over : " + e.getMessage() );
			e.printStackTrace();
		}

	}
*/



	/**
	 * Runs the prediction threads which generates a prediction every 10 minutes
	 */
	public void run()
	{
		System.out.println("Prediction unit starts");
		while(!Thread.interrupted())
		{
			try
			{
				process();
				System.gc(); //calls the garbage collector just in case
				Thread.sleep(1000*60*10); 
			}
			catch (InterruptedException e)
			{
				Util.printErrorMessage(e, "Prediction Unit");

				run();
			}


		}
	}


	/**
	 * gets the current date and runs the predictions for the last forex market value
	 */
	private void process()
	{

		Date current = new Date();
		Date	from = new Date(     ( (long)(current.getTime() - 1000*60*10)   ));// 10 minutes before --if not converted again to long, the numbers may become negatives
		BasicDBObject timeFramedQuery = new BasicDBObject("DATE", new BasicDBObject("$gte",from).append("$lte", current));

		BasicDBObject sort = new BasicDBObject("DATE",-1); //get the last value entered
		Cursor forexQuotes = dao.sort(DAO.FOREXMARKET, timeFramedQuery, sort);
		System.out.println(Thread.currentThread().getName());
		try
		{
			if(forexQuotes.hasNext()) //it should always have a next one if the Information Collector - forex market component is working
			{
				current = (Date) forexQuotes.next().get("DATE");
				generatePrediction(current);
			}
		}
		catch(Exception e)
		{
			System.out.println("The list is over : " + e.getMessage() );
			e.printStackTrace();
		}
	}



}
