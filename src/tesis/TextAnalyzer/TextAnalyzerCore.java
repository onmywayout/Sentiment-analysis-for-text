package tesis.TextAnalyzer;

import is2.tools.Tool;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class processes serves to information collectors in determining the sentiment associated with a given text 
 */
public class TextAnalyzerCore 
{

	/**
	 * Lemmatize tool used to ...lemmatize texts
	 */
	Tool lemma;

	/**
	 * Lingpipe classifier used to get sentiment analysis in Spanish
	 */
	LingPipeClassifier classifier;


	/**
	 * Stanford Sentiment analysis pipeline for English
	 */
	StanfordCoreNLP pipeline;

	/**
	 * Dummy Variables constantly used in the pipeline process
	 */
	private Annotation annotation;
	private double answer;
	private int sentiment;
	private List<CoreMap> sentences;
	private Tree tree;
	private double n;
	private String[] tokenized;
	private String inProcess;
	private String chunk;


	/**
	 * Creates new instances of the analysis tools, which may take some time to load
	 * @param lang language to by analyzed by the calling class: SP for Spanish or EN for English
	 */
	public TextAnalyzerCore(String lang) 
	{
		if(lang.contains("SP"))
		{
			System.out.println("Reading classifier Spanish");
			classifier = new LingPipeClassifier();
			lemma = SpanishLemmatizer.createLemmatizer();
			System.out.println("Spanish Model loaded");
		}
		else
		{

			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, parse, sentiment");
			pipeline = new StanfordCoreNLP(props);
			System.out.println("English Model loaded");
		}

	}


	/**
	 * Lemmatizes and does a sentiment analysis over the text passed as parameter
	 * @param text Clean text to be analyzed
	 * @return text sentiment
	 */
	public double analyzeSpanishText(String text)
	{
		if(text == "" || text == null ||text.length()<5) 	
		{ return 0; } //nothing to analyze

		String inProcess = SpanishLemmatizer.Lematize(text, lemma);

		return Double.parseDouble(classifier.classifyLanguageModel(inProcess)); 

	}


	/**
	 * Lemmatizes and does a sentiment analysis over the text passed as parameter
	 * @param rss Clean rss to be analyzed
	 * @return text sentiment
	 */
	public double analyzeSpanishRss(String rss)
	{
		if(rss == "" || rss == null ||rss.length()<5) 	
		{ return 0; } //nothing to analyze

		n = 0;
		tokenized = rss.split("\\.");

		answer = 0.0;

		for (int i = 0; i < tokenized.length; i++)
		{
			if(tokenized[i].length()>5)
			{
				inProcess = SpanishLemmatizer.Lematize(tokenized[i], lemma);
				answer += Double.parseDouble(classifier.classifyLanguageModel(inProcess));
				n++;

			}
		}
		
		return (n==0)? 0 : answer/n; 

	}

	/**
	 * Uses Stanford sentiment analysis tool on the text passed as parameter
	 * @param rss feed text to be analyzed
	 * @return the calculated sentiment for the given rss feed
	 */
	public double analyzeEnglishRss(String rss)
	{
		annotation = null;
		if(rss == "" || rss == null ||rss.length()<5) 	
		{ return 0; } //nothing to analyze

		answer = 0.0;

		n = 0.0;
		tokenized  = rss.replace("?", "").split("\\.");

		double total = 0.0;
		 chunk = "";
		for (int i = 0; i < tokenized.length; i++)
		{
//			long t1 = System.currentTimeMillis();
			chunk = tokenized[i]; 

			if(chunk.length()>5 && chunk.length()<220)
			{
				//text to be analyzed 
				annotation = new Annotation(chunk);
				pipeline.annotate(annotation);

				sentiment = 0;
				sentences = annotation.get(SentencesAnnotation.class);

				for (CoreMap sentence : sentences) 
				{
					// traversing the words in the current sentence a CoreLabel is a CoreMap with additional token-specific methods
					//https://github.com/stanfordnlp/CoreNLP/issues/7
					//**************Sentiment Analysis 
					tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
					sentiment = RNNCoreAnnotations.getPredictedClass(tree); //stanford rating: 1 = negative,2 = neutro, 3 = positivo
				}


				//conversion to standar format
				switch(sentiment)
				{
				case 1:  
				{ 
					answer = -0.5;	
					break;
				} 
				case 2:
				{ 
					answer = 0;	
					break;
				} 
				case 3:
				{ 
					answer = 0.5;	
					break;
				} 
				}
				total += answer;

				n++;

			}
//			if(( System.currentTimeMillis() - t1)>500)
//			{
//				System.out.println("----" + (System.currentTimeMillis() - t1) + " : " + chunk);
//			}
//			t1 = System.currentTimeMillis();
		}
		
		return (n==0)? 0 : total/n;  

	}



	/**
	 * Uses Stanford sentiment analysis tool on the text passed as parameter
	 * @param text to be analyzed
	 * @return the calculated sentiment for the given text
	 */
	public double analyzeEnglishText(String text)
	{
		answer = 0;
		if(text == "" || text == null ||text.length()<5) 	
		{ return 0; } //nothing to analyze


		//text to be analyzed
		annotation = new Annotation(text);
		pipeline.annotate(annotation);
		//		System.out.println();
		//		System.out.println(annotation.toString());
		//		System.out.println(text);
		sentiment = 0;
		sentences = annotation.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) 
		{
			// traversing the words in the current sentence a CoreLabel is a CoreMap with additional token-specific methods
			//https://github.com/stanfordnlp/CoreNLP/issues/7
			//**************Sentiment Analysis 
			tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
			sentiment = RNNCoreAnnotations.getPredictedClass(tree); //stanford rating: 1 = negative,2 = neutro, 3 = positivo

		}

		//		System.out.println(sentiment);
		//conversion to standar format
		switch(sentiment)
		{
		case 1:  
		{ 
			answer = -0.5;	
			break;
		} 
		case 2:
		{ 
			answer = 0;	
			break;
		} 
		case 3:
		{ 
			answer = 0.5;	
			break;
		} 
		}

		//		System.out.println("Sentiment Text : " + answer);
		return answer;

	}





}
