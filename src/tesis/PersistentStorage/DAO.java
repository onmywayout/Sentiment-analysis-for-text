package tesis.PersistentStorage;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;


/**
 * This class provide an interface (Data Access Object) for all classes to access the default database (Mongo)
 * Follows a Singleton Model
 * Use DAO.getInstance() to obtain a reference to the connection
 * 
 * @author Nicolas
 *
 */
@SuppressWarnings("unused")
public class DAO
{
	
	/**
	 * Constants that define the different collections in the database
	 */
	public static String ENGLISHRSS = "englishrs";
	public static String SPANISHRSS = "spanishrs";
	public static String ENGLISHTWEETS = "englishtweets";
	public static String SPANISHTWEETSLEMMATIZED = "spanishtweetslemmatized";
	public static String SPANISHTWEETS = "spanishtweets";
	public static String GOOGLETRENDS = "googletrends";
	public static String FOREXMARKET = "forexmarkets";
	public static String STOCKMARKETINGCOL = "stockmarketcols";
	public static String EVALUATEDSENTIMENTS = "evaluatedsentiments";
	public static String RESULTS = "results";
	public static String RESULTSDETAIL = "resultsdetails";
	public static String TEST = "tests";
	public static String STATUS = "status";
	
	
	
	/**
	 * Singleton instance
	 */
	private static DAO instance;
	
	/**
	 * Connection to the mongo server
	 */
	MongoClient mongo;
	
	/**
	 * The database itself
	 */
	private DB db;

	/**
	 * path attribute to be used by the models to load its resources
	 */
	private String modelsPath;
	private int SPAtweets;
	private int ENGtweets;
	
	
	/**
	 * Here the english tweets are stored from the twitter strea threaad until the text analizer picks them for analysis
	 */
	public ArrayList<String> concurrentEnglishTweets;

	/**
	 * Here the spanish tweets are stored from the twitter strea threaad until the text analizer picks them for analysis
	 */
	public ArrayList<String> concurrentSpanishTweets;

	/**
	 * initializes the instance
	 * by default this creates a connection to the database passed as a parameter, if an empty parameter is pass then localhost is used
	 */
	private DAO(String host)
	{
		connectDB(host);
	}

	
	/**
	 * Updates a value in the database
	 * @param element query Element to be updated
	 * @param update values to update
	 * @param boolean upsert if the element cannot be found whether to insert it or not.
	 * @param collectionName Collection where the element will be updated
	 * @return upserted id
	 * i.e. BasicDBObject update = new BasicDBObject().append("$inc", new BasicDBObject().append("mentions", 1)); //aumente el campo mentions en 1
	 * geolocalizacion.update(new BasicDBObject().append("City", palabra).append("longitud", element.get("Longitude") ).append("latitud",  element.get("Latitude")), update,true,false); //busque en el campo author el que corresponda con el nombreUsuario y subale 1, si no existe creelo

	 * 
 */
	public String update(BasicDBObject query, BasicDBObject update, boolean upsert, String collectionName)
	{
		DBCollection temp = db.getCollection(collectionName);
		WriteResult response = temp.update(query, update,upsert,false); //busque en el campo author el que corresponda con el nombreUsuario y subale 1, si no existe creelo
		return "";
		/*if(response.isUpdateOfExisting())
		return "";
		else
		return response.getUpsertedId().toString();
		*/
	}
	
	/**
	 * Queries the collection for the desired object 
	 * @param query BasicDBObject query = new BasicDBObject();	query.put("City", palabra);
	 * @param collectionName Collection where the element will be query
	 * @return a MongoDb cursor with the query response
	 */
	public DBCursor query(String collectionName,BasicDBObject query)
	{
		return   db.getCollection(collectionName).find(query);
	}
	
	
	/**
	 * Performs a distinct operation over the field on the collection passed as aprameter 
	 * @param field String: field name i.e. DATE
	 * @param collectionName Collection where the operatio will be performed
	 * @return a MongoDb cursor with the query response
	 */
	@SuppressWarnings("unchecked")
	public List<Object> distinct(String collectionName,String field)
	{
		return   db.getCollection(collectionName).distinct(field);
	}
	
	/**
	 * Queries the collection for the desired object and returns the result in the desired order
	 * @param query BasicDBObject query = new BasicDBObject();	query.put("City", palabra);
	 * @param collectionName Collection where the element will be query
	 * @return a MongoDb cursor with the query response
	 * @param sort the sorting field and value
	 */
	public DBCursor sort(String collectionName,BasicDBObject query,BasicDBObject sort)
	{
		return   db.getCollection(collectionName).find(query).sort(sort);
	}
	
	
	/**
	 * Inserts the desired object in the collection
	 * @param element to insert in the collection
	 * @param collectionName Collection where the element will be inserted
	 * @return a MongoDb cursor with the query response
	 */
	public void insert(String collectionName,BasicDBObject element)
	{
		db.getCollection(collectionName).insert(element);
	}
	
	
	/**
	 * Gets the whole collection 
	 * @param collectionName Collection to be returned
	 * @return a MongoDb cursor with the collection
	 */
	public DBCursor getCollection(String collectionName)
	{
		return   db.getCollection(collectionName).find();
	}
	
	/**
	 * Returns the collection object from the database
	 * Most calls can and should be satisfied with the cursor return in getCollection() 
	 * 	as it mildly isolates persistence from logic level
	 * @param collectionName
	 * @return
	 */
	public DBCollection getRawCollection(String collectionName)
	{
		return   db.getCollection(collectionName);
	}
	/**
	 * Gets the number of elements within the collection
	 * @param collectionName Collection of interest
	 * @return total number of elements wihtin the collection
	 */
	public long getCount(String collectionName)
	{
		return   db.getCollection(collectionName).count();
	}
	
	/**
	 * Creates  a connection to the database passed as a parameter, if an empty parameter is pass then localhost is used	
	 * @param host Ip adress of the Mongo Host
	 */
	private void connectDB(String host) 
	{
		try {

			System.out.println("Connecting to Mongo DB..");

			if(host.trim()=="")
				mongo = new MongoClient(); //localhost;
			else
				mongo = new MongoClient(host.trim());

			db = mongo.getDB("SourcePrediction");

			System.out.println("Successfully connected to MongoDB");

		} 
		catch (Exception ex)
		{
			System.out.println("MongoException :" + ex.getMessage());
		}

	}
	
	/**
	 * Returns all the collections in the current Database
	 * @return Iterator with the collections in the database
	 */
	public Iterable<String> getCollections()
	{
		return db.getCollectionNames();
	}
	
	
	
	/**
	 * Disconnects from the current Database
	 * 
	 */
	public void disconnectDB()
	{
		
		mongo.close();
	}
	
	/**
	 * Gets the current instance of the class. 
	 * Singleton Model
	 * by default this creates a connection to the database passed as a parameter, if an empty parameter is pass then localhost is used
	 * @param host
	 * @return
	 */
	public static DAO getInstance(String host)
	{
		if(instance == null)
		{
			instance =  new DAO(host);
		}
			return instance;
	}
	
	/**
	 * Gets the current instance of the class. 
	 * Singleton Model
	 * by default this creates a connection to the localhost,
	 * @param host
	 * @return
	 */
	public static DAO getInstance()
	{
		if(instance == null)
		{
			instance =  new DAO("");
		}
			return instance;
	}


	/**
	 * @return the modelsPath
	 */
	public String getModelsPath() 
	{
		return modelsPath;
	}


	/**
	 * @param modelsPath the modelsPath to set
	 */
	public void setModelsPath(String modelsPath)
	{
		this.modelsPath = modelsPath;
	}
	

	/**
	 * set the number of Spanish tweets collected
	 * @return
	 */
	public void setSPATweetsNumber(int n)
	{
		 SPAtweets += n;
	}
	

	/**
	 * sets the number of english tweets collected
	 * @return
	 */
	public void setENGTweetsNumber(int n)
	{
		ENGtweets += n;
	}
	
	/**
	 * Returns the number of Spanishtweets collected
	 * @return
	 */
	public int getSPATweetsNumber()
	{
		return SPAtweets;
	}

	/**
	 * Returns the number of English tweets collected
	 * @return
	 */
	public int getENGTweetsNumber()
	{
		return ENGtweets;
	}
	
	
	
	
	
	
}
