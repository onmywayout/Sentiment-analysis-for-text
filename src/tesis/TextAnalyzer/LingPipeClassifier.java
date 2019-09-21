package tesis.TextAnalyzer;

import java.io.File;

import tesis.PersistentStorage.DAO;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LMClassifier;
import com.aliasi.util.AbstractExternalizable;


@SuppressWarnings("rawtypes")
public class LingPipeClassifier
{
	
	LMClassifier  naturalClassificator;
	
	LMClassifier naiveBayesClassifier;
	
	/**
	 * Loads from disk 2 models to be used in the classification process
	 */
	public LingPipeClassifier()
	{

		try 
		{

			
			naiveBayesClassifier = (LMClassifier) AbstractExternalizable.readObject(new File(DAO.getInstance().getModelsPath() + "naiveBayes.lp"));
			naturalClassificator= (LMClassifier ) AbstractExternalizable.readObject(new File(DAO.getInstance().getModelsPath() + "languageSmoother.lp"));
			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		

	}

	/**
	 * uses a naive Bayes trained classifier
	 * @param text text to be analyzed
	 * @return text best category, [-1, 1]
	 */
	public String classifyNaiveBayes(String text) 
	{
		ConditionalClassification classification = naturalClassificator.classify(text);
		return classification.bestCategory();
	}
	
	
	/**
	 * Uses the 8-gram character language model classifier
	 * @param text text to be analyzed
	 * @return text best category
	 */
	public String classifyLanguageModel(String text)
	{
		ConditionalClassification classification = naiveBayesClassifier.classify(text);
		return classification.bestCategory();
	}
	
	

}
