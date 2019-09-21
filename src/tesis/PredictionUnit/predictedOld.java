package tesis.PredictionUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;

import tesis.PersistentStorage.DAO;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


/**
 * This class references all the information collected by the other components of the solution 
 * and processes it to output a prediction
 * @author Nicolas
 *
 */
public class predictedOld
{

	/**
	 * Coefficient for RSS sentiment multiplication
	 */
	double rssCoef = 0.40;

	/**
	 * Coefficient for twitter sentiment multiplication
	 */
	double twitterCoef = 0.3;

	/**
	 * Coefficient for google trends multiplication
	 */
	double googleTrendsCoef = 0.3;

	/**
	 * Coefficient for facebook sentiment multiplication
	 */
	double fbCoef = 0.2;


	/**
	 * Database access
	 */
	private DAO dao;

	public  predictedOld()
	{

		dao = DAO.getInstance("");
	}


	/**
	 * Generates a prediction within the time frame specified
	 * if both parameters are null then it generates a prediction based on the last 60 minutes of data
	 * @param from
	 * @param until
	 * @param previous 
	 * @return
	 */
	private void generatePrediction( Date current, Date next)
	{


		Date	from = new Date(System.currentTimeMillis()-1000*60*20);//20 minutes before

		//db.googleTrends.find({"ENDWEEK": {$gt: ISODate("2014-01-15T00:00:00Z"), $lt: ISODate("2014-05-15T00:00:00Z")}}).count();

		// get the sentiments for the timeframe
		BasicDBObject timeFramedQuery = new BasicDBObject("DATE", new BasicDBObject("$gte",from).append("$lte", current));

		Cursor evaluatedSentiments = dao.query(DAO.EVALUATEDSENTIMENTS, timeFramedQuery);

		DBObject element = new BasicDBObject();

		double GoogleSentiment = 0;

		//Variable initialization
		double ASRSSEnglish = 0; //Average Sentiment RSS English
		double ASRSSSpanish  = 0;
		double ASTwitterEnglish  = 0;
		double ASTwitterSpanish  = 0;

		double nASRSSEnglish = 0; //number of elements in Sentiment RSS English
		double nASRSSSpanish  = 0;
		double nASTwitterEnglish  = 0;
		double nASTwitterSpanish  = 0;
		ArrayList<Double> numeros = new ArrayList<>();
		double currentSent = 0.0;
		String type = "";
		String lang = "";
		//iterates over the sentiments
		while(evaluatedSentiments.hasNext())
		{
			
			//verificar NaN
			element = evaluatedSentiments.next();
			type = (String) element.get("TYPE");
			currentSent = (double) element.get("SENTIMENT"); 
			numeros.add(currentSent);

			lang = (String) element.get("LANGUAGE");
			//selects the appropiate type of source
			switch(type)
			{
			case "RSS":
			{
				if(lang.endsWith("EN"))
				{
					if(!Double.isNaN(currentSent))
					{
						ASRSSEnglish += currentSent; nASRSSEnglish ++;
					}
				}
				else
				{
					ASRSSSpanish += currentSent; nASRSSSpanish ++;
				}

				break;
			}
			case "TWEET":

				if(lang.endsWith("EN"))
				{
					ASTwitterEnglish += currentSent;nASTwitterEnglish++;
				}
				else
				{
					ASTwitterSpanish += currentSent;nASTwitterSpanish++;
				}

				break;
			}

		}

		nASRSSEnglish = (nASRSSEnglish== 0)? 1: nASRSSEnglish;
		nASRSSSpanish = (nASRSSSpanish== 0)? 1: nASRSSSpanish;
		nASTwitterEnglish = (nASTwitterEnglish== 0)? 1: nASTwitterEnglish;
		nASTwitterSpanish = (nASTwitterSpanish== 0)? 1: nASTwitterSpanish;

		//dividides by n and multiplies by its respective coefficient
		ASRSSEnglish = (ASRSSEnglish/nASRSSEnglish)*(rssCoef/2); // (divided by 2 so they add to 1 in RSS Weight)
		ASRSSSpanish  = (ASRSSSpanish/nASRSSSpanish)*(rssCoef/2);
		ASTwitterEnglish  = (ASTwitterEnglish/nASTwitterEnglish)*(twitterCoef/2);  // (divided by 2 so they add to 1 in Twitter Weight)
		ASTwitterSpanish  = (ASTwitterSpanish/nASTwitterSpanish)*(twitterCoef/2);   



		GoogleSentiment = getGoogleSentiment("DOLAR");

		GoogleSentiment = GoogleSentiment*googleTrendsCoef;



		//this should range from [-1 , 1]
		double AverageSentiment = GoogleSentiment + ASTwitterEnglish + ASTwitterSpanish + ASRSSEnglish + ASRSSSpanish;

		//All the evaluated markets

		//		double colcap = 0;//todo
		//		double colcapVar = 0;
		//		double DJI = 0;
		//		double DJIVar = 0;
		double COPUSDVar = 0.05;


		//get the values from the next element

		BasicDBObject queryUntil = new BasicDBObject("DATE", next);
			//last = gt.find(timeFramedQuery).limit(1).sort(sort).next(); //Example: { "_id" : { "$oid" : "535d86077c11fb1a23e458a0"} , "BOLSA DE VALORES COLOMBIA" : 7 , "BVC" : 11 , "DOLAR" : 57 , "ECOPETROL" : 66 , "ENDWEEK" : { "$date" : "2013-04-14T05:00:00.000Z"} , "precio dolar" : 13}
		DBObject last = dao.query(DAO.FOREXMARKET, queryUntil).next();
		

		double lastValue = 0;
		try
		{
			lastValue = (double)last.get("VALUEY");
		}
		catch (Exception e)
		{
			try
			{
				lastValue = (double)last.get("VALUE");
			}
			catch(Exception ef)
			{
				lastValue = (double)last.get("VALUEG");
			}
		}

		
		//get the values from the current element
		 queryUntil = new BasicDBObject("DATE", current);
		DBObject currentObj = dao.query(DAO.FOREXMARKET, queryUntil).next();
		double currentVal = 0;
		try
		{
			currentVal = (double)currentObj.get("VALUEY");
		}
		catch (Exception e)
		{
			try
			{
			currentVal = (double)currentObj.get("VALUE");
			}
			catch(Exception ef)
			{
				currentVal = (double)currentObj.get("VALUEG");
			}
		}
		


		double predictionCOP = (currentVal* ( 1 + (COPUSDVar*AverageSentiment))) ;

		//String TrendValue = "COPUSD" + ":" + predictionCOP + ":" + lastValue;
		//				+"colcap" + ":" + (colcap*(colcapVar*AverageSentiment))
		//				+ "DJI" + ":" + (DJI*(DJIVar*AverageSentiment));


		BasicDBObject queryResult = new BasicDBObject("DATE", next);
		BasicDBObject storeResult = new BasicDBObject("DATE", next).append("COPACTUAL", lastValue).append("COPPREDICTED", predictionCOP);

		dao.update(queryResult, storeResult, true, DAO.RESULTS); //updates colleciton to be used by GUI

		BasicDBObject querySentimentStore =new BasicDBObject("DATE", next);
		BasicDBObject storeSentimentStore = new BasicDBObject("DATE", next).append("SENTIMENT",AverageSentiment).append(  "GOOGLESENTIMENT", GoogleSentiment)
																									.append("TWITTERENGLISH", ASTwitterEnglish).append("TWITTERSPANISH",ASTwitterSpanish)
																									.append("RSSENGLISH", ASRSSEnglish).append("RSSSPANISH", ASRSSSpanish);
		dao.update(querySentimentStore, storeSentimentStore, true, DAO.RESULTSDETAIL); //updates colleciton to be used by GUI

		System.out.println(predictionCOP);

	}




	/**
	 * Calculates the google sentiment, which is the percentage of deviation from the average mean for the selected terms
	 * thus, if the average mean (number of searches in the week) is 30 for the last 2 weeks
	 *  and the current value for the last week (most recent data)is 60, the sentiment is effectively +1
	 *  If the current value equals that of the mean then the current sentiment is 0
	 * @return
	 */

	@SuppressWarnings("deprecation")
	private double getGoogleSentiment(String field) 
	{

		////gets the average for the last 2 weekss in the dollar field and then substracts the value of the last week
		// then calculates the deviation of that last value from the previously found average

		//makes a query for the week average


		Date weekAsDate = new Date();
		int currentMonth = weekAsDate.getMonth();
		int month = ((currentMonth -3) <0)? currentMonth + 9: currentMonth - 3;
		weekAsDate.setMonth(month);//sets the date to 3 months ago 

		//db.googleTrends.aggregate([ { "$match": { "ENDWEEK": { "$gt":  ISODate("2014-01-15T00:00:00Z") } }},
		//{ "$group": {"_id": null, "BVC": { "$avg": "$BVC" } }} ])

		// --does an aggregation query

		DBObject match = new BasicDBObject("$match",new BasicDBObject("ENDWEEK", new BasicDBObject("$gt", weekAsDate)));		
		DBObject group = new BasicDBObject("$group", new BasicDBObject("_id",null).append(field, new BasicDBObject("$sum", "$" + field)));

		LinkedList<DBObject> parameters = new LinkedList<>();
		parameters.add(match); 
		parameters.add(group);

		DBCollection gt = dao.getRawCollection(DAO.GOOGLETRENDS);

		AggregationOutput output = gt.aggregate( parameters);//runs the aggregation query

		BasicDBObject result = output.getCommandResult();


		BasicDBList t = ((BasicDBList)result.get("result"));
		String preAvg =  ((BasicDBObject) t.get(0)).getString(field);

		// --does a query for the number of objects
		BasicDBObject number = new BasicDBObject("ENDWEEK",new BasicDBObject("$gt", weekAsDate));
		int resultsNumber = dao.query(DAO.GOOGLETRENDS, number).count();


		//gets the last inserted document
		//db.foo.find().sort({_id:1}).limit(1);
		BasicDBObject sort = new BasicDBObject("_id",1);
		DBObject last = gt.find().limit(1).sort(sort).next(); //Example: { "_id" : { "$oid" : "535d86077c11fb1a23e458a0"} , "BOLSA DE VALORES COLOMBIA" : 7 , "BVC" : 11 , "DOLAR" : 57 , "ECOPETROL" : 66 , "ENDWEEK" : { "$date" : "2013-04-14T05:00:00.000Z"} , "precio dolar" : 13}
		int lastValue = (int)last.get(field);

		//calculates the past average
		Double average = (Double.parseDouble(preAvg) - lastValue)/(resultsNumber-1);

		System.out.println("Prediction Running");


		return (lastValue/average)-1;
	}

/*
	public static void main(String[] args) 
	{
		PredicterCore objeto = new PredicterCore();

		DAO dao = DAO.getInstance();


		//sets caendars for the end and begginig of this day so a time frame can be used in the query
		Calendar dayEnd = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));
		Calendar dayStart = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));

		
		int dia = 30;
		int mes = 3; //abril = 3
		dayStart.set(Calendar.HOUR_OF_DAY, 00);
		dayStart.set(Calendar.MINUTE, 00);
		dayStart.set(Calendar.DAY_OF_MONTH, dia);
		dayStart.set(Calendar.MONTH, mes);
		dayEnd.set(Calendar.HOUR_OF_DAY, 23);
		dayEnd.set(Calendar.MINUTE, 59);
		dayEnd.set(Calendar.DAY_OF_MONTH, dia);
		dayEnd.set(Calendar.MONTH, mes);

//		BasicDBObject timeFramedQuery = new BasicDBObject("DATE", 
			//	new BasicDBObject("$gte",new Date(dayStart.getTimeInMillis()))
	//	.append("$lte", new Date(dayEnd.getTimeInMillis())));

		
		
		BasicDBObject query = new BasicDBObject();
		BasicDBObject sort = new BasicDBObject("DATE",1);
		Cursor forexQuotes = dao.sort(DAO.FOREXMARKET, query, sort);

		Date current = (Date) forexQuotes.next().get("DATE");

		Date next = null;

		try
		{
			while(forexQuotes.hasNext())
			{

				//selects the appropiate type of source
				next = (Date) forexQuotes.next().get("DATE");

				if((next.getTime())-current.getTime()>1000*30) //30 seconds difference
				{
						objeto.generatePrediction(current, next);
				}
				current = next;
				
			}
		}
		catch(Exception e)
		{
			System.out.println("The list is over : " + e.getMessage() );
		}
	}

	/**
	 * Makes the component generate a predicion on the latest 3 stock market values
	 */
	public  void runComponent()
	{
		predictedOld objeto = new predictedOld();

		DAO dao = DAO.getInstance();


		//sets caendars for the end and begginig of this day so a time frame can be used in the query
		Calendar dayEnd = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));
		Calendar dayStart = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));

		
		int dia = 30;
		int mes = 3; //abril = 3
		dayStart.set(Calendar.HOUR_OF_DAY, 00);
		dayStart.set(Calendar.MINUTE, 00);
		dayStart.set(Calendar.DAY_OF_MONTH, dia);
		dayStart.set(Calendar.MONTH, mes);
		dayEnd.set(Calendar.HOUR_OF_DAY, 23);
		dayEnd.set(Calendar.MINUTE, 59);
		dayEnd.set(Calendar.DAY_OF_MONTH, dia);
		dayEnd.set(Calendar.MONTH, mes);

		BasicDBObject timeFramedQuery = new BasicDBObject("DATE", 
				new BasicDBObject("$gte",new Date(dayStart.getTimeInMillis()))
		.append("$lte", new Date(dayEnd.getTimeInMillis())));

		Cursor forexQuotes = dao.query(DAO.FOREXMARKET, timeFramedQuery);

		Date current = (Date) forexQuotes.next().get("DATE");

		Date next = null;

		try
		{
			while(forexQuotes.hasNext())
			{

				//selects the appropiate type of source
				next = (Date) forexQuotes.next().get("DATE");

				if((current.getTime()-next.getTime())>1000*30) //30 seconds difference
				{
						objeto.generatePrediction(current, next);
				}
				current = next;
				
			}
		}
		catch(Exception e)
		{
			System.out.println("The list is over : " + e.getMessage() );
		}
	}

	/**
	 * Retrieves the latest market data from the database
	 * @params where to load the arguments to, from the database
	 */
	public void getMarketValues(double colcap, double colcapVar, double dJI,double dJIVar, double cOPUSD, double cOPUSDVar) 
	{


	}


}
