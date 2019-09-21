package tesis.TextAnalyzer;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.tools.Tool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

import tesis.PersistentStorage.DAO;

/**
 * @author Bernd Bohnet, 13.09.2010
 * 
 * Illustrates the application full pipeline: lemmatizer, morphologic, tagger, and parser
 * 
 * 
 * CLase Completamente funcional
 */
public class SpanishLemmatizer 
{

	/**
	 * Returns a new instance of a lemmatizer pipeline; 
	 * Be aware, might take some time to initialize
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Lemmatizer createLemmatizer()
	{
		System.out.println("\nReading the model of the lemmatizer");
		
		String path = DAO.getInstance().getModelsPath() + "CoNLL2009-ST-Spanish-ALL.anna-3.3.lemmatizer.model";
		return new Lemmatizer(path); 
	}


	/**
	 * Lemmatizes the arry passed as parameter
	 * receives clean text
	 * and Returns the output of the lemmatization process
	 */
	public static ArrayList<String> Lematize(ArrayList<String> petition)
	{

		ArrayList<String> response = new ArrayList<>();

		long inicio = System.currentTimeMillis();
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Hora de inicio: " + timeStamp);

		System.out.println("\nReading the model of the lemmatizer");

		String path = SpanishLemmatizer.class.getClass().getClassLoader().getResource("./datos/models/CoNLL2009-ST-Spanish-ALL.anna-3.3.lemmatizer.model").getPath();
		
		@SuppressWarnings("deprecation")
		Tool lemmatizer = new Lemmatizer(path); 
		
		timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("comienzan iteraciones: " + timeStamp);
		String stringSucio = "";
		StringTokenizer st ;
		ArrayList<String> forms;
		String temp = "";
		for (int i = 0; i < petition.size(); i++)
		{
			temp = "";
			// Create a data container for a sentence
			SentenceData09 sd = new SentenceData09();

			stringSucio = petition.get(i); //obtiene este campo del item de la coleccion
			st = new StringTokenizer(stringSucio);

			forms = new ArrayList<String>();
			while(st.hasMoreTokens()) 
			{forms.add(st.nextToken());}

			sd.init(forms.toArray(new String[0]));
			lemmatizer.apply(sd);
			//			System.out.print(i.toString());
			for (String l : sd.plemmas) 
			{temp += (l+" ");}

			response.add(temp);
		}

		long fin = System.currentTimeMillis();
		String timeStamp2 = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Hora Fin : " + timeStamp2);
		System.out.println("Tiempo transcurrido en segundos : " + (fin - inicio)/1000);

		return response;

	}


	/**
	 * Lemmatizes the array passed as parameter and using the lemmatizer also passed as parameter
	 * this allows to use one lemmatizer for several calls
	 * Receives clean text
	 * and Returns the output of the lemmatization process
	 */
	public static ArrayList<String> Lematize(ArrayList<String> petition, Tool lemmatizer)
	{

		ArrayList<String> response = new ArrayList<>();

		long inicio = System.currentTimeMillis();
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Hora de inicio: " + timeStamp);

		timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("comienzan iteraciones: " + timeStamp);
		String stringSucio = "";
		StringTokenizer st ;
		ArrayList<String> forms;
		String temp = "";
		for (int i = 0; i < petition.size(); i++)
		{
			temp = "";
			// Create a data container for a sentence
			SentenceData09 sd = new SentenceData09();

			stringSucio = petition.get(i); 
			st = new StringTokenizer(stringSucio);

			forms = new ArrayList<String>();
			while(st.hasMoreTokens()) 
			{forms.add(st.nextToken());}

			sd.init(forms.toArray(new String[0]));
			lemmatizer.apply(sd);
			//			System.out.print(i.toString());
			for (String l : sd.plemmas) 
			{temp += (l+" ");}

			response.add(temp);
		}

		long fin = System.currentTimeMillis();
		String timeStamp2 = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println("Hora Fin : " + timeStamp2);
		System.out.println("Tiempo transcurrido en segundos : " + (fin - inicio)/1000);

		return response;

	}


	/**
	 * Lemmatizes the text passed as parameter and using the lemmatizer also passed as parameter
	 * this allows to use one lemmatizer for several calls
	 * Receives clean text
	 * and Returns the output of the lemmatization process
	 */
	public static String Lematize(String petition, Tool lemmatizer)
	{

		String response = "";


		StringTokenizer st ;
		ArrayList<String> forms;
		String temp = "";
		// Create a data container for a sentence
		SentenceData09 sd = new SentenceData09();

		st = new StringTokenizer(petition);

		forms = new ArrayList<String>();
		while(st.hasMoreTokens()) 
		{
			forms.add(st.nextToken());
		}

		sd.init(forms.toArray(new String[0]));
		
		if(sd.length()<0)
		{
			return petition; //the process could not be made
		}
		
		lemmatizer.apply(sd);
		for (String l : sd.plemmas) 
		{temp += (l+" ");}

		response = temp;


		return response;

	}



}