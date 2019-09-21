package tesis.informationCollector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;

import org.json.JSONArray;
import org.json.JSONException;

import tesis.PersistentStorage.DAO;

import com.mongodb.BasicDBObject;

public class StockTracker implements Runnable
{

	/**
	 * Parameter with the companies that are relevant for the study and thus its value is going to be stored
	 */
	ArrayList<String> observedCompanies;


	/**
	 * Storage location for errors
	 */
	static String StoragePath = System.getProperty("user.home")+  java.io.File.separator + "SOFIAERRORS" + java.io.File.separator;


	DAO dao;

	/**
	 * Initialize the class, required.
	 * @param iObservedCompanies companies that are relevant for the study and thus its value will be stored
	 * @param dao 
	 */
	public StockTracker(ArrayList<String> iObservedCompanies, DAO idao)
	{
		observedCompanies = iObservedCompanies;
		dao = idao;
	}



	/**
	 * Downloads live data from the COlombian market trough BVC
	 * http://www.bvc.com.co/mercados/DescargaXlsServlet?archivo=acciones&fecha=2014-04-22&resultados=100&tipoMercado=
	 * @throws IOException
	 * @throws JSONException
	 */
	@SuppressWarnings("unused")
	private void getStocksBVC() throws Exception
	{

		//"http://www.bvc.com.co/mercados/DescargaXlsServlet?archivo=acciones&fecha=2014-04-25&resultados=100&tipoMercado="
		URL server;
		URLConnection urlCon;
		HttpURLConnection con;

		//makes date formatting

		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
		SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

		Date current = new Date(System.currentTimeMillis()-(1000*60*60*5)); //Colombia's stock market time zone

		String url = "http://www.bvc.com.co/mercados/DescargaXlsServlet?archivo=acciones&fecha="
				+  yearFormat.format(current)
				+ "-"
				+  monthFormat.format(current) //returns the curent month in the appropiate format
				+ "-"
				+ dayFormat.format(current)
				+ "&resultados=100&tipoMercado=";


		// downloads the content
		server = new URL(url);
		urlCon = server.openConnection();
		con = (HttpURLConnection)urlCon;


		Workbook w = Workbook.getWorkbook(con.getInputStream());

		String company;
		double volume = 00;
		double lastPrice = 0;
		double variation = 0;

		//for values parsing
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
		df.setDecimalFormatSymbols(symbols);

		BasicDBObject quote = new BasicDBObject("DATE",current);
		// Get the first sheet
		Sheet sheet = w.getSheet(0);
		// Loop over first 10 column and lines
		for (int j = 2; j < sheet.getRows(); j++) 
		{//	0 1Cantidad	2Nemotecnico	3Volumen	4Ultimo Precio	5Variacion%

			company = sheet.getCell(2,j).getContents().trim();
			//checks whether we are interested in the stock of this company as defined by the parameters
			String element = null;
			for (int i = 0; i < observedCompanies.size(); i++)
			{
				element = observedCompanies.get(i);
				if(element.equalsIgnoreCase(company))
				{
					volume = df.parse(sheet.getCell(3,j).getContents()).doubleValue();
					lastPrice = df.parse(sheet.getCell(4,j).getContents()).doubleValue();
					variation = df.parse(sheet.getCell(5,j).getContents()).doubleValue();

					quote.append(company + "VOL" , volume);
					quote.append(company + "VAL" , lastPrice);
					quote.append(company + "VAR" , variation);
				}
			}
		}

		if(quote.size()>1)
			dao.insert(DAO.STOCKMARKETINGCOL, quote);
//		System.out.println(quote.toString());

	}

	private void getYahooForex() throws ParseException
	{


		String contenido = "";
		try 
		{
			// Se conecta a la URL
			URL server;
			URLConnection urlCon;
			HttpURLConnection con;

			// Descarga el contenido
			server = new URL("http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote");
			urlCon = server.openConnection();
			con = (HttpURLConnection)urlCon;
			//					String contenido = IOUtils. toString(con.getInputStream());					
			contenido = convertStreamToString(con.getInputStream());

		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			Util.printErrorMessage(ex, Thread.currentThread().getName(), "YahooForex");
		}


		/*
		 *  <resource classname="Quote">
			<field name="name">USD/COP</field>
			<field name="price">1931.099976</field>
			<field name="symbol">COP=X</field>
			<field name="ts">1398691505</field>
			<field name="type">currency</field>
			<field name="utctime">2014-04-28T13:25:05+0000</field>
			<field name="volume">0</field>
			</resource>
		 */

		// Identifies item of interest
		Pattern items = Pattern.compile("USD\\/COP<\\/field>\\s?<field name=\"price\">(.*)<\\/field>\\s<field.*>\\s<field name=\"ts\">(.*)<\\/field>");				
		Matcher itemsMatches = items.matcher(contenido);
		double value = 0.0;
		long unixDate = 0;


		while (itemsMatches.find()) //only enters if COP/USD is found, which should be
		{
			// gets the value
			value = Double.parseDouble(itemsMatches.group(1));
			unixDate = Long.parseLong(itemsMatches.group(2));

		}

		Date date = new Date(unixDate*1000); //GTM - 5
		System.out.println(date.toString() + " : " +value);

		BasicDBObject store = new BasicDBObject("DATE", date).append("VALUEY",value);
		dao.update(store ,store ,true ,DAO.FOREXMARKET);

	}

	/**
	 * Downloads live data with the current value of USDCOP and stores its values on DAO.FOREXMARKET
	 * @throws IOException
	 * @throws JSONException
	 */
	@SuppressWarnings("unused")
	private void getGoogleForex() throws Exception
	{

		// Se conecta a la URL
		URL server;
		URLConnection urlCon;
		HttpURLConnection con;

		// Descarga el contenido
		server = new URL("http://www.google.com/finance/info?q=CURRENCY%3aUSDCOP");
		urlCon = server.openConnection();
		con = (HttpURLConnection)urlCon;
		//					String contenido = IOUtils. toString(con.getInputStream());					
		String contenido = convertStreamToString(con.getInputStream());

		JSONArray resp = new JSONArray(contenido.replaceFirst("//", ""));
		/* [ { "id": "304466804484872" ,"t" : "GOOG" ,"e" : "NASDAQ" ,"l" : "515.92" ,"l_fix" : "515.92" ,"l_cur" : "515.92" ,
		"s": "0" ,"ltt":"2:34PM EDT" ,"lt" : "Apr 25, 2:34PM EDT" ,"lt_dts" : "2014-04-25T14:34:06Z" ,"c" : "-9.24" ,"c_fix" : "-9.24" 
		,"cp" : "-1.76" ,"cp_fix" : "-1.76" ,"ccol" : "chr" ,"pcls_fix" : "525.16" } ]

		t: currencies
		l: current value
		c: difference with previous (day?) value
		cp: same as above but as a percentage

		 */

//		System.out.println("t: " + resp.getJSONObject(0).get("t"));
//		System.out.println(contenido);
		//we format the numbers with the english format to make it system independent. Otherwise it wouldnt work on a spanish configured system, due to commas used as floating point
		NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
		Number number = null;
		Number number2 = null;

		number = format.parse((String) resp.getJSONObject(0).get("l"));
		number2 = format.parse((String) resp.getJSONObject(0).get("cp"));


		double value = number.doubleValue();
		double variation = number2.doubleValue();

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd',' h:mma z/yyyy", Locale.ENGLISH); //lt" : "Apr 25, 2:34PM EDT  "Apr 25, 8:00PM GMT"

		Date date = sdf.parse(resp.getJSONObject(0).getString("lt")+"/" + Calendar.getInstance().get(Calendar.YEAR));
		//		date = new Date(date.getTime()-(1000*60*60*5));

		BasicDBObject query = new BasicDBObject("DATE", date).append("VALUEG",value).append("VARIATIONG", variation);

		BasicDBObject store = new BasicDBObject("DATE", date).append("VALUEG",value).append("VARIATIONG", variation);
		dao.update(query,store,true, DAO.FOREXMARKET);

	}

	/**
	 * Converts a  http stream to a string
	 * @param is
	 * @return
	 */
	@SuppressWarnings("resource")
	static String convertStreamToString(java.io.InputStream is)
	{
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}


	public static void main(String[] args)
	{
		ArrayList <String> test = new ArrayList<String>();
		test.add("PREC");
		test.add("ECOPETROL");
		test.add("ISA");
		test.add("PFBCOLOM");
		test.add("PFCEMARGOS");
		test.add("ICOLCAP");
		test.add("BVC");
		test.add("PFGRUPSURA");
		new StockTracker(test,DAO.getInstance() ).run();;

	}



	/**
	 * starts the Stock and Forex Market data recollection 
	 */
	public void run()
	{
		int i = 0;
		int sto = 0;
		while(!Thread.interrupted()) 
		{
			try
			{
//				getStocksBVC();
			}
			catch (Exception e2) //no es  importante para SOFIA que este falle
			{
				sto++;
				e2.printStackTrace();
				if(sto>100)
				{
					Util.printErrorMessage(e2, "STOCK", "BVC_ERROR:"+sto);
//					break;
					sto = 0;
				}
			}

			try
			{	
//				getGoogleForex();
				getYahooForex();
//				System.out.println("Stock Tracker: sleep " + i);
				Thread.sleep(1000*60*1); //sleep for 1 minute
			}
			catch (Exception e)
			{
				Util.printErrorMessage(e, "STOCK:"+i);
				try 
				{
					Thread.sleep(1000*60*1);
				} catch (InterruptedException e1) 
				{
					e1.printStackTrace();
				}
				if(i>100)
				{
					Util.printErrorMessage(e, "STOCK:"+i, "NO INFO COMING FROM GOOGLE OR YAHOO");
//					break;
				}
				i++;


			}
		}


	}

}
