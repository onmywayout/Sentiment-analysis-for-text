package tesis.informationCollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tesis.PersistentStorage.DAO;

import com.mongodb.BasicDBObject;

/**
 * @author Nicolas
 *
 *
 *
 
 		/*
		 * // Data table response
				google.visualization.Query.setResponse({"version":"0.6","status":"ok","sig":"1149890885",
				"table":{"cols":[{"id":"date","label":"Date","type":"date","pattern":""},{"id":"query0","label":"dolar","type":"number",
				"pattern":""},{"id":"query1","label":"precio dolar","type":"number","pattern":""},{"id":"query2","label":"trm","type":"number",
				"pattern":""},{"id":"query3","label":"divisas","type":"number","pattern":""}],"rows":[{"c":[{"v":new Date(2014,5,8),"f":
				"domingo, 8 de junio de 2014"},{"v":41.0,"f":"41"},{"v":14.0,"f":"14"},{"v":10.0,"f":"10"},{"v":0.0,"f":"0"}]},{"c":[{"v":
				new Date(2014,5,9),"f":"lunes, 9 de junio de 2014"},{"v":98.0,"f":"98"},{"v":25.0,"f":"25"},{"v":63.0,"f":"63"},{"v":9.0,"f":"9"}]}
				,{"c":[{"v":new Date(2014,5,10),"f":"martes, 10 de junio de 2014"},{"v":100.0,"f":"100"},{"v":24.0,"f":"24"},{"v":59.0,"f":"59"},
				{"v":8.0,"f":"8"}]},{"c":[{"v":new Date(2014,5,11),"f":"miércoles, 11 de junio de 2014"},{"v":83.0,"f":"83"},{"v":16.0,"f":"16"},
				{"v":48.0,"f":"48"},{"v":7.0,"f":"7"}]},{"c":[{"v":new Date(2014,5,12),"f":"jueves, 12 de junio de 2014"},,,,{"v":null}]},{"c":
				[{"v":new Date(2014,5,13),"f":"viernes, 13 de junio de 2014"},,,,{"v":null}]},{"c":[{"v":new Date(2014,5,14),"f":"sábado, 14 de junio de 2014"},,,,{"v":null}]}]}});
		 
		
		//response for 7 days and for 1 year
// 		content="// Data table response\ngoogle.visualization.Query.setResponse({\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"1149890885\",\"table\":{\"cols\":[{\"id\":\"date\",\"label\":\"Date\",\"type\":\"date\",\"pattern\":\"\"},{\"id\":\"query0\",\"label\":\"dolar\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query1\",\"label\":\"precio dolar\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query2\",\"label\":\"trm\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query3\",\"label\":\"divisas\",\"type\":\"number\",\"pattern\":\"\"}],\"rows\":[{\"c\":[{\"v\":new Date(2014,5,8),\"f\":\"domingo, 8 de junio de 2014\"},{\"v\":41.0,\"f\":\"41\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":10.0,\"f\":\"10\"},{\"v\":0.0,\"f\":\"0\"}]},{\"c\":[{\"v\":new Date(2014,5,9),\"f\":\"lunes, 9 de junio de 2014\"},{\"v\":98.0,\"f\":\"98\"},{\"v\":25.0,\"f\":\"25\"},{\"v\":63.0,\"f\":\"63\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2014,5,10),\"f\":\"martes, 10 de junio de 2014\"},{\"v\":100.0,\"f\":\"100\"},{\"v\":24.0,\"f\":\"24\"},{\"v\":59.0,\"f\":\"59\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,5,11),\"f\":\"mi\u00e9rcoles, 11 de junio de 2014\"},{\"v\":83.0,\"f\":\"83\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":48.0,\"f\":\"48\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,5,12),\"f\":\"jueves, 12 de junio de 2014\"},,,,{\"v\":null}]},{\"c\":[{\"v\":new Date(2014,5,13),\"f\":\"viernes, 13 de junio de 2014\"},,,,{\"v\":null}]},{\"c\":[{\"v\":new Date(2014,5,14),\"f\":\"s\u00e1bado, 14 de junio de 2014\"},,,,{\"v\":null}]}]}});";
//		content = "// Data table response\ngoogle.visualization.Query.setResponse({\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"1984979772\",\"table\":{\"cols\":[{\"id\":\"date\",\"label\":\"Date\",\"type\":\"date\",\"pattern\":\"\"},{\"id\":\"query0\",\"label\":\"dolar\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query1\",\"label\":\"precio dolar\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query2\",\"label\":\"trm\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query3\",\"label\":\"divisas\",\"type\":\"number\",\"pattern\":\"\"}],\"rows\":[{\"c\":[{\"v\":new Date(2013,5,16),\"f\":\"16\\u201322 jun. 2013\"},{\"v\":86.0,\"f\":\"86\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,5,23),\"f\":\"23\\u201329 jun. 2013\"},{\"v\":88.0,\"f\":\"88\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,5,30),\"f\":\"30 jun.\\u20136 jul. de 2013\"},{\"v\":83.0,\"f\":\"83\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,7),\"f\":\"7\\u201313 jul. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,14),\"f\":\"14\\u201320 jul. 2013\"},{\"v\":79.0,\"f\":\"79\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,21),\"f\":\"21\\u201327 jul. 2013\"},{\"v\":80.0,\"f\":\"80\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,28),\"f\":\"28 jul.\\u20133 ago. de 2013\"},{\"v\":73.0,\"f\":\"73\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,7,4),\"f\":\"4\\u201310 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":28.0,\"f\":\"28\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,7,11),\"f\":\"11\\u201317 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,7,18),\"f\":\"18\\u201324 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,7,25),\"f\":\"25\\u201331 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,8,1),\"f\":\"1\\u20137 sept. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,8,8),\"f\":\"8\\u201314 sept. 2013\"},{\"v\":78.0,\"f\":\"78\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,8,15),\"f\":\"15\\u201321 sept. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,8,22),\"f\":\"22\\u201328 sept. 2013\"},{\"v\":74.0,\"f\":\"74\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,8,29),\"f\":\"29 sept.\\u20135 oct. de 2013\"},{\"v\":76.0,\"f\":\"76\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,9,6),\"f\":\"6\\u201312 oct. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,9,13),\"f\":\"13\\u201319 oct. 2013\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":25.0,\"f\":\"25\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,9,20),\"f\":\"20\\u201326 oct. 2013\"},{\"v\":64.0,\"f\":\"64\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":28.0,\"f\":\"28\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,9,27),\"f\":\"27 oct.\\u20132 nov. de 2013\"},{\"v\":68.0,\"f\":\"68\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,10,3),\"f\":\"3\\u20139 nov. 2013\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":10.0,\"f\":\"10\"}]},{\"c\":[{\"v\":new Date(2013,10,10),\"f\":\"10\\u201316 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,10,17),\"f\":\"17\\u201323 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":10.0,\"f\":\"10\"}]},{\"c\":[{\"v\":new Date(2013,10,24),\"f\":\"24\\u201330 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,11,1),\"f\":\"1\\u20137 dic. 2013\"},{\"v\":76.0,\"f\":\"76\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,11,8),\"f\":\"8\\u201314 dic. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,11,15),\"f\":\"15\\u201321 dic. 2013\"},{\"v\":73.0,\"f\":\"73\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,11,22),\"f\":\"22\\u201328 dic. 2013\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":26.0,\"f\":\"26\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,11,29),\"f\":\"29 dic. de 2013\\u20134 ene. de 2014\"},{\"v\":76.0,\"f\":\"76\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":25.0,\"f\":\"25\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2014,0,5),\"f\":\"5\\u201311 ene. 2014\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,0,12),\"f\":\"12\\u201318 ene. 2014\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,0,19),\"f\":\"19\\u201325 ene. 2014\"},{\"v\":84.0,\"f\":\"84\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,0,26),\"f\":\"26 ene.\\u20131 feb. de 2014\"},{\"v\":98.0,\"f\":\"98\"},{\"v\":23.0,\"f\":\"23\"},{\"v\":38.0,\"f\":\"38\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,1,2),\"f\":\"2\\u20138 feb. 2014\"},{\"v\":100.0,\"f\":\"100\"},{\"v\":24.0,\"f\":\"24\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,1,9),\"f\":\"9\\u201315 feb. 2014\"},{\"v\":92.0,\"f\":\"92\"},{\"v\":22.0,\"f\":\"22\"},{\"v\":36.0,\"f\":\"36\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,1,16),\"f\":\"16\\u201322 feb. 2014\"},{\"v\":88.0,\"f\":\"88\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,1,23),\"f\":\"23 feb.\\u20131 mar. de 2014\"},{\"v\":84.0,\"f\":\"84\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,2,2),\"f\":\"2\\u20138 mar. 2014\"},{\"v\":79.0,\"f\":\"79\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,2,9),\"f\":\"9\\u201316 mar. 2014\"},{\"v\":73.0,\"f\":\"73\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,2,16),\"f\":\"16\\u201322 mar. 2014\"},{\"v\":78.0,\"f\":\"78\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,2,23),\"f\":\"23\\u201329 mar. 2014\"},{\"v\":90.0,\"f\":\"90\"},{\"v\":22.0,\"f\":\"22\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,2,30),\"f\":\"30 mar.\\u20135 abr. de 2014\"},{\"v\":81.0,\"f\":\"81\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":36.0,\"f\":\"36\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,3,6),\"f\":\"6\\u201312 abr. 2014\"},{\"v\":81.0,\"f\":\"81\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,3,13),\"f\":\"13\\u201319 abr. 2014\"},{\"v\":65.0,\"f\":\"65\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":22.0,\"f\":\"22\"},{\"v\":4.0,\"f\":\"4\"}]},{\"c\":[{\"v\":new Date(2014,3,20),\"f\":\"20\\u201326 abr. 2014\"},{\"v\":75.0,\"f\":\"75\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,3,27),\"f\":\"27 abr.\\u20133 may. de 2014\"},{\"v\":66.0,\"f\":\"66\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2014,4,4),\"f\":\"4\\u201310 may. 2014\"},{\"v\":70.0,\"f\":\"70\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,4,11),\"f\":\"11\\u201317 may. 2014\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2014,4,18),\"f\":\"18\\u201324 may. 2014\"},{\"v\":64.0,\"f\":\"64\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,4,25),\"f\":\"25\\u201331 may. 2014\"},{\"v\":65.0,\"f\":\"65\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,5,1),\"f\":\"1\\u20137 jun. 2014\"},{\"v\":64.0,\"f\":\"64\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,5,8),\"f\":\"8\\u201314 jun. 2014\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":38.0,\"f\":\"38\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,5,15),\"f\":\"15\\u201321 jun. 2014\"},,,,{\"v\":null}]}]}});";
		//TRM, Precio dolar, dolar hoy
		//URL for 7 days and for 1 year
//		String GTUrl = "http://www.google.com/trends/fetchComponent?q=dolar%2C%20precio%20dolar%2C%20TRM%2C%20divisas&geo=CO&date=today%207-d&cmpt=q&cid=TIMESERIES_GRAPH_0&export=3";
		
 
 
 */
public class GoogleTrends implements Runnable
{

	DAO dao;
	private String googleFile;



	/**
	 * Reads A file containing the google trends search on selected terms and stores the results on mongo
	 * If no file is determined, the data will be loaded from the internet,
	 * if the file path is passed but not found "./datos/GoogleTrends/report.csv" wil be used
	 * @param iDao data acces object to be used
	 * @param googleFile path to where the data file is stored
	 * @throws ParseException if the date from GT is not on the correct format
	 */
	public GoogleTrends(String igoogleFile,DAO iDao)
	{

		dao = iDao;
		googleFile = igoogleFile;
	
	}

	/**
	 * Loads the Google Data from the webpage that provides data to the google trends service
	 * Not officially supported by google nevertheless it has been working for over 3 years
	 */
	private void loadFromWeb()
	{

		
				String content1 = "";
//				content1 = "// Data table response\ngoogle.visualization.Query.setResponse({\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"1984979772\",\"table\":{\"cols\":[{\"id\":\"date\",\"label\":\"Date\",\"type\":\"date\",\"pattern\":\"\"},{\"id\":\"query0\",\"label\":\"dolar\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query1\",\"label\":\"precio dolar\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query2\",\"label\":\"trm\",\"type\":\"number\",\"pattern\":\"\"},{\"id\":\"query3\",\"label\":\"divisas\",\"type\":\"number\",\"pattern\":\"\"}],\"rows\":[{\"c\":[{\"v\":new Date(2013,5,16),\"f\":\"16\\u201322 jun. 2013\"},{\"v\":86.0,\"f\":\"86\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,5,23),\"f\":\"23\\u201329 jun. 2013\"},{\"v\":88.0,\"f\":\"88\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,5,30),\"f\":\"30 jun.\\u20136 jul. de 2013\"},{\"v\":83.0,\"f\":\"83\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,7),\"f\":\"7\\u201313 jul. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,14),\"f\":\"14\\u201320 jul. 2013\"},{\"v\":79.0,\"f\":\"79\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,21),\"f\":\"21\\u201327 jul. 2013\"},{\"v\":80.0,\"f\":\"80\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,6,28),\"f\":\"28 jul.\\u20133 ago. de 2013\"},{\"v\":73.0,\"f\":\"73\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,7,4),\"f\":\"4\\u201310 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":28.0,\"f\":\"28\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,7,11),\"f\":\"11\\u201317 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,7,18),\"f\":\"18\\u201324 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,7,25),\"f\":\"25\\u201331 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,8,1),\"f\":\"1\\u20137 sept. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,8,8),\"f\":\"8\\u201314 sept. 2013\"},{\"v\":78.0,\"f\":\"78\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,8,15),\"f\":\"15\\u201321 sept. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,8,22),\"f\":\"22\\u201328 sept. 2013\"},{\"v\":74.0,\"f\":\"74\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,8,29),\"f\":\"29 sept.\\u20135 oct. de 2013\"},{\"v\":76.0,\"f\":\"76\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,9,6),\"f\":\"6\\u201312 oct. 2013\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,9,13),\"f\":\"13\\u201319 oct. 2013\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":25.0,\"f\":\"25\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,9,20),\"f\":\"20\\u201326 oct. 2013\"},{\"v\":64.0,\"f\":\"64\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":28.0,\"f\":\"28\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,9,27),\"f\":\"27 oct.\\u20132 nov. de 2013\"},{\"v\":68.0,\"f\":\"68\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,10,3),\"f\":\"3\\u20139 nov. 2013\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":10.0,\"f\":\"10\"}]},{\"c\":[{\"v\":new Date(2013,10,10),\"f\":\"10\\u201316 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2013,10,17),\"f\":\"17\\u201323 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":29.0,\"f\":\"29\"},{\"v\":10.0,\"f\":\"10\"}]},{\"c\":[{\"v\":new Date(2013,10,24),\"f\":\"24\\u201330 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":9.0,\"f\":\"9\"}]},{\"c\":[{\"v\":new Date(2013,11,1),\"f\":\"1\\u20137 dic. 2013\"},{\"v\":76.0,\"f\":\"76\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2013,11,8),\"f\":\"8\\u201314 dic. 2013\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,11,15),\"f\":\"15\\u201321 dic. 2013\"},{\"v\":73.0,\"f\":\"73\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,11,22),\"f\":\"22\\u201328 dic. 2013\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":26.0,\"f\":\"26\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2013,11,29),\"f\":\"29 dic. de 2013\\u20134 ene. de 2014\"},{\"v\":76.0,\"f\":\"76\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":25.0,\"f\":\"25\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2014,0,5),\"f\":\"5\\u201311 ene. 2014\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,0,12),\"f\":\"12\\u201318 ene. 2014\"},{\"v\":77.0,\"f\":\"77\"},{\"v\":19.0,\"f\":\"19\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,0,19),\"f\":\"19\\u201325 ene. 2014\"},{\"v\":84.0,\"f\":\"84\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,0,26),\"f\":\"26 ene.\\u20131 feb. de 2014\"},{\"v\":98.0,\"f\":\"98\"},{\"v\":23.0,\"f\":\"23\"},{\"v\":38.0,\"f\":\"38\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,1,2),\"f\":\"2\\u20138 feb. 2014\"},{\"v\":100.0,\"f\":\"100\"},{\"v\":24.0,\"f\":\"24\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,1,9),\"f\":\"9\\u201315 feb. 2014\"},{\"v\":92.0,\"f\":\"92\"},{\"v\":22.0,\"f\":\"22\"},{\"v\":36.0,\"f\":\"36\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,1,16),\"f\":\"16\\u201322 feb. 2014\"},{\"v\":88.0,\"f\":\"88\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,1,23),\"f\":\"23 feb.\\u20131 mar. de 2014\"},{\"v\":84.0,\"f\":\"84\"},{\"v\":21.0,\"f\":\"21\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,2,2),\"f\":\"2\\u20138 mar. 2014\"},{\"v\":79.0,\"f\":\"79\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,2,9),\"f\":\"9\\u201316 mar. 2014\"},{\"v\":73.0,\"f\":\"73\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,2,16),\"f\":\"16\\u201322 mar. 2014\"},{\"v\":78.0,\"f\":\"78\"},{\"v\":18.0,\"f\":\"18\"},{\"v\":33.0,\"f\":\"33\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,2,23),\"f\":\"23\\u201329 mar. 2014\"},{\"v\":90.0,\"f\":\"90\"},{\"v\":22.0,\"f\":\"22\"},{\"v\":35.0,\"f\":\"35\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,2,30),\"f\":\"30 mar.\\u20135 abr. de 2014\"},{\"v\":81.0,\"f\":\"81\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":36.0,\"f\":\"36\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,3,6),\"f\":\"6\\u201312 abr. 2014\"},{\"v\":81.0,\"f\":\"81\"},{\"v\":20.0,\"f\":\"20\"},{\"v\":37.0,\"f\":\"37\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,3,13),\"f\":\"13\\u201319 abr. 2014\"},{\"v\":65.0,\"f\":\"65\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":22.0,\"f\":\"22\"},{\"v\":4.0,\"f\":\"4\"}]},{\"c\":[{\"v\":new Date(2014,3,20),\"f\":\"20\\u201326 abr. 2014\"},{\"v\":75.0,\"f\":\"75\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":8.0,\"f\":\"8\"}]},{\"c\":[{\"v\":new Date(2014,3,27),\"f\":\"27 abr.\\u20133 may. de 2014\"},{\"v\":66.0,\"f\":\"66\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":32.0,\"f\":\"32\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2014,4,4),\"f\":\"4\\u201310 may. 2014\"},{\"v\":70.0,\"f\":\"70\"},{\"v\":17.0,\"f\":\"17\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,4,11),\"f\":\"11\\u201317 may. 2014\"},{\"v\":67.0,\"f\":\"67\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":34.0,\"f\":\"34\"},{\"v\":6.0,\"f\":\"6\"}]},{\"c\":[{\"v\":new Date(2014,4,18),\"f\":\"18\\u201324 may. 2014\"},{\"v\":64.0,\"f\":\"64\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":30.0,\"f\":\"30\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,4,25),\"f\":\"25\\u201331 may. 2014\"},{\"v\":65.0,\"f\":\"65\"},{\"v\":16.0,\"f\":\"16\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,5,1),\"f\":\"1\\u20137 jun. 2014\"},{\"v\":64.0,\"f\":\"64\"},{\"v\":14.0,\"f\":\"14\"},{\"v\":31.0,\"f\":\"31\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,5,8),\"f\":\"8\\u201314 jun. 2014\"},{\"v\":71.0,\"f\":\"71\"},{\"v\":15.0,\"f\":\"15\"},{\"v\":38.0,\"f\":\"38\"},{\"v\":7.0,\"f\":\"7\"}]},{\"c\":[{\"v\":new Date(2014,5,15),\"f\":\"15\\u201321 jun. 2014\"},,,,{\"v\":null}]}]}});";

				String content2 = "";
//				content2 = "// Data table response\ngoogle.visualization.Query.setResponse({\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"1358824954\",\"table\":{\"cols\":[{\"id\":\"date\",\"label\":\"Date\",\"type\":\"date\",\"pattern\":\"\"},{\"id\":\"query0\",\"label\":\"dolar\",\"type\":\"number\",\"pattern\":\"\"}],\"rows\":[{\"c\":[{\"v\":new Date(2013,5,16),\"f\":\"16\\u201322 jun. 2013\"},{\"v\":86.0,\"f\":\"86\"}]},{\"c\":[{\"v\":new Date(2013,5,23),\"f\":\"23\\u201329 jun. 2013\"},{\"v\":88.0,\"f\":\"88\"}]},{\"c\":[{\"v\":new Date(2013,5,30),\"f\":\"30 jun.\\u20136 jul. de 2013\"},{\"v\":83.0,\"f\":\"83\"}]},{\"c\":[{\"v\":new Date(2013,6,7),\"f\":\"7\\u201313 jul. 2013\"},{\"v\":77.0,\"f\":\"77\"}]},{\"c\":[{\"v\":new Date(2013,6,14),\"f\":\"14\\u201320 jul. 2013\"},{\"v\":79.0,\"f\":\"79\"}]},{\"c\":[{\"v\":new Date(2013,6,21),\"f\":\"21\\u201327 jul. 2013\"},{\"v\":80.0,\"f\":\"80\"}]},{\"c\":[{\"v\":new Date(2013,6,28),\"f\":\"28 jul.\\u20133 ago. de 2013\"},{\"v\":73.0,\"f\":\"73\"}]},{\"c\":[{\"v\":new Date(2013,7,4),\"f\":\"4\\u201310 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"}]},{\"c\":[{\"v\":new Date(2013,7,11),\"f\":\"11\\u201317 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"}]},{\"c\":[{\"v\":new Date(2013,7,18),\"f\":\"18\\u201324 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"}]},{\"c\":[{\"v\":new Date(2013,7,25),\"f\":\"25\\u201331 ago. 2013\"},{\"v\":72.0,\"f\":\"72\"}]},{\"c\":[{\"v\":new Date(2013,8,1),\"f\":\"1\\u20137 sept. 2013\"},{\"v\":77.0,\"f\":\"77\"}]},{\"c\":[{\"v\":new Date(2013,8,8),\"f\":\"8\\u201314 sept. 2013\"},{\"v\":78.0,\"f\":\"78\"}]},{\"c\":[{\"v\":new Date(2013,8,15),\"f\":\"15\\u201321 sept. 2013\"},{\"v\":77.0,\"f\":\"77\"}]},{\"c\":[{\"v\":new Date(2013,8,22),\"f\":\"22\\u201328 sept. 2013\"},{\"v\":74.0,\"f\":\"74\"}]},{\"c\":[{\"v\":new Date(2013,8,29),\"f\":\"29 sept.\\u20135 oct. de 2013\"},{\"v\":76.0,\"f\":\"76\"}]},{\"c\":[{\"v\":new Date(2013,9,6),\"f\":\"6\\u201312 oct. 2013\"},{\"v\":77.0,\"f\":\"77\"}]},{\"c\":[{\"v\":new Date(2013,9,13),\"f\":\"13\\u201319 oct. 2013\"},{\"v\":67.0,\"f\":\"67\"}]},{\"c\":[{\"v\":new Date(2013,9,20),\"f\":\"20\\u201326 oct. 2013\"},{\"v\":64.0,\"f\":\"64\"}]},{\"c\":[{\"v\":new Date(2013,9,27),\"f\":\"27 oct.\\u20132 nov. de 2013\"},{\"v\":68.0,\"f\":\"68\"}]},{\"c\":[{\"v\":new Date(2013,10,3),\"f\":\"3\\u20139 nov. 2013\"},{\"v\":67.0,\"f\":\"67\"}]},{\"c\":[{\"v\":new Date(2013,10,10),\"f\":\"10\\u201316 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"}]},{\"c\":[{\"v\":new Date(2013,10,17),\"f\":\"17\\u201323 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"}]},{\"c\":[{\"v\":new Date(2013,10,24),\"f\":\"24\\u201330 nov. 2013\"},{\"v\":71.0,\"f\":\"71\"}]},{\"c\":[{\"v\":new Date(2013,11,1),\"f\":\"1\\u20137 dic. 2013\"},{\"v\":76.0,\"f\":\"76\"}]},{\"c\":[{\"v\":new Date(2013,11,8),\"f\":\"8\\u201314 dic. 2013\"},{\"v\":71.0,\"f\":\"71\"}]},{\"c\":[{\"v\":new Date(2013,11,15),\"f\":\"15\\u201321 dic. 2013\"},{\"v\":73.0,\"f\":\"73\"}]},{\"c\":[{\"v\":new Date(2013,11,22),\"f\":\"22\\u201328 dic. 2013\"},{\"v\":67.0,\"f\":\"67\"}]},{\"c\":[{\"v\":new Date(2013,11,29),\"f\":\"29 dic. de 2013\\u20134 ene. de 2014\"},{\"v\":76.0,\"f\":\"76\"}]},{\"c\":[{\"v\":new Date(2014,0,5),\"f\":\"5\\u201311 ene. 2014\"},{\"v\":77.0,\"f\":\"77\"}]},{\"c\":[{\"v\":new Date(2014,0,12),\"f\":\"12\\u201318 ene. 2014\"},{\"v\":77.0,\"f\":\"77\"}]},{\"c\":[{\"v\":new Date(2014,0,19),\"f\":\"19\\u201325 ene. 2014\"},{\"v\":84.0,\"f\":\"84\"}]},{\"c\":[{\"v\":new Date(2014,0,26),\"f\":\"26 ene.\\u20131 feb. de 2014\"},{\"v\":98.0,\"f\":\"98\"}]},{\"c\":[{\"v\":new Date(2014,1,2),\"f\":\"2\\u20138 feb. 2014\"},{\"v\":100.0,\"f\":\"100\"}]},{\"c\":[{\"v\":new Date(2014,1,9),\"f\":\"9\\u201315 feb. 2014\"},{\"v\":92.0,\"f\":\"92\"}]},{\"c\":[{\"v\":new Date(2014,1,16),\"f\":\"16\\u201322 feb. 2014\"},{\"v\":88.0,\"f\":\"88\"}]},{\"c\":[{\"v\":new Date(2014,1,23),\"f\":\"23 feb.\\u20131 mar. de 2014\"},{\"v\":84.0,\"f\":\"84\"}]},{\"c\":[{\"v\":new Date(2014,2,2),\"f\":\"2\\u20138 mar. 2014\"},{\"v\":79.0,\"f\":\"79\"}]},{\"c\":[{\"v\":new Date(2014,2,9),\"f\":\"9\\u201316 mar. 2014\"},{\"v\":73.0,\"f\":\"73\"}]},{\"c\":[{\"v\":new Date(2014,2,16),\"f\":\"16\\u201322 mar. 2014\"},{\"v\":78.0,\"f\":\"78\"}]},{\"c\":[{\"v\":new Date(2014,2,23),\"f\":\"23\\u201329 mar. 2014\"},{\"v\":90.0,\"f\":\"90\"}]},{\"c\":[{\"v\":new Date(2014,2,30),\"f\":\"30 mar.\\u20135 abr. de 2014\"},{\"v\":81.0,\"f\":\"81\"}]},{\"c\":[{\"v\":new Date(2014,3,6),\"f\":\"6\\u201312 abr. 2014\"},{\"v\":81.0,\"f\":\"81\"}]},{\"c\":[{\"v\":new Date(2014,3,13),\"f\":\"13\\u201319 abr. 2014\"},{\"v\":65.0,\"f\":\"65\"}]},{\"c\":[{\"v\":new Date(2014,3,20),\"f\":\"20\\u201326 abr. 2014\"},{\"v\":75.0,\"f\":\"75\"}]},{\"c\":[{\"v\":new Date(2014,3,27),\"f\":\"27 abr.\\u20133 may. de 2014\"},{\"v\":66.0,\"f\":\"66\"}]},{\"c\":[{\"v\":new Date(2014,4,4),\"f\":\"4\\u201310 may. 2014\"},{\"v\":70.0,\"f\":\"70\"}]},{\"c\":[{\"v\":new Date(2014,4,11),\"f\":\"11\\u201317 may. 2014\"},{\"v\":67.0,\"f\":\"67\"}]},{\"c\":[{\"v\":new Date(2014,4,18),\"f\":\"18\\u201324 may. 2014\"},{\"v\":64.0,\"f\":\"64\"}]},{\"c\":[{\"v\":new Date(2014,4,25),\"f\":\"25\\u201331 may. 2014\"},{\"v\":65.0,\"f\":\"65\"}]},{\"c\":[{\"v\":new Date(2014,5,1),\"f\":\"1\\u20137 jun. 2014\"},{\"v\":64.0,\"f\":\"64\"}]},{\"c\":[{\"v\":new Date(2014,5,8),\"f\":\"8\\u201314 jun. 2014\"},{\"v\":71.0,\"f\":\"71\"}]},{\"c\":[{\"v\":new Date(2014,5,15),\"f\":\"15\\u201321 jun. 2014\"},{\"v\":null}]}]}});";
				//dolar hoy, precio dolar, TRM 
				String GTUrl1 = "http://www.google.com/trends/fetchComponent?q=dolar%20hoy%2C%20precio%20dolar%2C%20TRM&geo=CO&date=today%2012-m&cmpt=q&cid=TIMESERIES_GRAPH_0&export=3";
				
				//dolar
				String GTUrl2 = "http://www.google.com/trends/fetchComponent?q=dolar&geo=CO&date=today%2012-m&cmpt=q&cid=TIMESERIES_GRAPH_0&export=3";
				
		try 
		{
			URL server = new URL(GTUrl1);
			URLConnection urlCon = server.openConnection();
			HttpURLConnection con = (HttpURLConnection)urlCon;
			content1 = convertStreamToString(con.getInputStream());
			con.disconnect();
			
			Random m = new Random();
			double randome = m.nextDouble()*6; //any value between 0 and 6 minutes
			
			Thread.sleep((long)(1000*60*randome)); //to avoid query google too often
			
			 server = new URL(GTUrl2);
			 urlCon = server.openConnection();
			 con = (HttpURLConnection)urlCon;
			content2 = convertStreamToString(con.getInputStream());
			con.disconnect();
			
			

		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			Util.printErrorMessage(ex, Thread.currentThread().getName(), "Error descargando los datos desde Google Trends");
		}
		
		//formats the string so it can be converted to a JSON
		content1 = content1.replace("// Data table response\ngoogle.visualization.Query.setResponse(", "");
		content1 = content1.replace(");", "");
		content1 = content1.replaceAll("(new Date\\(\\d+,\\d+,\\d+\\))", "\"$1\"");
		content1 = content1.replaceAll(",,,,", ",{},{},{},");

		content2 = content2.replace("// Data table response\ngoogle.visualization.Query.setResponse(", "");
		content2 = content2.replace(");", "");
		content2 = content2.replaceAll("(new Date\\(\\d+,\\d+,\\d+\\))", "\"$1\"");
//		content2 = content2.replaceAll(",,,,", ",{},{},{},");
		
		JSONObject converted1 = null;
		JSONObject converted2 = null;
		
		
		Date day;
		double dolarHoy;
		double precioDolar;
		double trm;
		double dolar;
		BasicDBObject query;
		BasicDBObject update;
		JSONArray c1;
		JSONArray c2;
		try
		{
			converted1 = new JSONObject(content1);
			JSONArray rows1 = converted1.getJSONObject("table").getJSONArray("rows");
			
			converted2 = new JSONObject(content2);
			JSONArray rows2 = converted2.getJSONObject("table").getJSONArray("rows");
			//the first item is the date
			for (int i = 0; i < rows1.length()-1; i++)//the last week does not have information
			{
				
				 c1 = rows1.getJSONObject(i).getJSONArray("c");
				c2 = rows2.getJSONObject(i).getJSONArray("c");
				String f [] = c1.getJSONObject(0).getString("f").split("–");
				day = dateFormatting(f[1]);
				dolarHoy = c1.getJSONObject(1).getDouble("v");
				precioDolar = c1.getJSONObject(2).getDouble("v");
				trm = c1.getJSONObject(3).getDouble("v");
				
				dolar = c2.getJSONObject(1).getDouble("v");
				
				
				query = new BasicDBObject("DATE", day);
				update = new BasicDBObject("DATE", day).append("DOLARHOY", dolarHoy).append("PRECIODOLAR", precioDolar).append("TRM", trm).append("DOLAR", dolar);
				
				dao.update(query, update, true, DAO.GOOGLETRENDS);
			
				System.out.println("day:" +  day.toString() + " | Dolar Hoy: " + dolarHoy + " | Precio Dolar: " + precioDolar + " | TRM: " + trm + " | Dolar: " + dolar);
			}
			
		} catch (JSONException e)
		{
			e.printStackTrace();
			Util.printErrorMessage(e, "Google trends invalid connection error", content1);
		} 

		
	}




	/**
	 * Reads the GT from a file passed as a parameter, this has to be provided by the user in the parameters section and
	 * has to be downloaded from the Google Trends webpage as a CSV (report.csv)
	 * @param googleFile
	 */
	public void loadFromFile(String googleFile) throws ParseException
	{
		ArrayList<String []> results = new ArrayList<>();
		String [] headers = null;

		if(googleFile.equals(""))
			googleFile = "./datos/GoogleTrends/report.csv";

		try (BufferedReader in = Files.newBufferedReader(Paths.get(googleFile),Charset.forName("UTF-8")))
		{
			String line = null;

			//ignores the first lines

			for (int i = 0; i < 4; i++) 
			{
				in.readLine();
			}

			//Semana,dolar,ecopetrol,bolsa de valores colombia,bvc,precio dolar
			headers = in.readLine().split(","); //retrieves the headers

			//2014-04-13 - 2014-04-19,67,40,4,7,15
			while ((line = in.readLine()) != null)
			{
				if(line.startsWith(";;")) //end of related information
					break;

				String [] values = line.split(",");
				results.add(values);

			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		System.out.println("Google Trends Running");
		String[] values = null;

		for (int i = 0; i < results.size(); i++) 
		{
			values = results.get(i); //row values

			//gets the end week value /2014-04-13 - 2014-04-19

			String endWeek[] = values[0].split(" - ");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			Date weekAsDate = sdf.parse(endWeek[0]);

			BasicDBObject query = new BasicDBObject("ENDWEEK",weekAsDate); // "week", 2014-04-19

			BasicDBObject storeValues = new BasicDBObject();

			for (int j = 1; j < headers.length-1; j++) 
			{
				storeValues.append(headers[j].toUpperCase(), Integer.parseInt(values[j] ));
			}

			storeValues.append(headers[headers.length-1].replace(";", ""), Integer.parseInt(values[headers.length-1].replace(";", "")) ); //glitch in doc

			BasicDBObject store = new BasicDBObject("$set",storeValues); //updates the desired values on the database, if they dont exist, creates a new record

			//update or insert?
			dao.update(query, 
					store, 
					true, 
					DAO.GOOGLETRENDS);
		}


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
	 * converts several date formats to a date object
	 * if not conversion format suited the given string null is returned
	 * @return
	 */
	private static Date dateFormatting(String fechaOriginal)
	{
		SimpleDateFormat sdf;
		Date mydate;
		
		String[] date_formats = 
		{
				"dd MMM'.' yyyy",
				"dd MMM'. de' yyyy",
				"dd MMMM'.' yyyy",
				"dd MMMM'. de' yyyy",
		};
		
		fechaOriginal = fechaOriginal.replace("sept", "sep");

		for (String formatString : date_formats)
		{
			try
			{    
				 sdf = new SimpleDateFormat(formatString,new Locale("es","CO")); //dates come in spanish
				mydate = sdf.parse(fechaOriginal);
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


	public void run()
	{
		System.out.println("Prediction unit starts");
		while(!Thread.interrupted())
		{
			try
			{
				if(googleFile.trim().equalsIgnoreCase(""))
					loadFromWeb();
					else
					loadFromFile(googleFile);
				
				System.gc(); //calls the garbage collector just in case
				Random m = new Random();
				double randome = m.nextDouble()*120; //any value between 0 and 120
				long sleeping = (long)(1000*3600*24) + (long)randome*1000*60;
				Thread.sleep(sleeping);  //sleeps for a day and a randome time, toprevent from blocking the ip as an automated service
			}
			catch (Exception e)
			{
				Util.printErrorMessage(e, "Google trends unknown error");

				run();
			}


		}
		
	}
	
}
