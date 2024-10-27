package external;

import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;

import org.json.JSONObject;
import org.json.JSONException;

public class NLU_example {
	public static boolean isValidJSON(AnalysisResults response) {
        try {
            new JSONObject(response); // 尝试解析为 JSONObject
            return true;  // 如果没有抛出异常，则 JSON 合法
        } catch (JSONException ex) {
            return false; // 如果抛出异常，说明不是有效的 JSON
        }
    }
	
    public static void main(String[] args) {
        // Create an authenticator instance using your API key
    	IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey("6CdKaFalSQC8weFKox40hX8ab5MA-qYYM0Gd0o4_9a_M").build();
        
        // Create an instance of the Natural Language Understanding service
        NaturalLanguageUnderstanding naturalLanguageUnderstanding = new NaturalLanguageUnderstanding("2022-04-07", authenticator);
        
        // Set the service URL
        naturalLanguageUnderstanding.setServiceUrl("https://api.us-south.natural-language-understanding.watson.cloud.ibm.com/instances/55ab98a3-5f81-4ce5-a18e-077fe64cb09d");
        
        // URL to analyze
        String url = "https://www.cnn.com";
        
        // Create an EntitiesOptions object with sentiment analysis enabled and limit results to 1 entity
        EntitiesOptions entities = new EntitiesOptions.Builder()
            .sentiment(false)
            .limit(3L)
            .build();
        
        // Create a Features object that includes entity analysis
        Features features = new Features.Builder()
            .entities(entities)
            .build();
        
        // Build the analysis options with the given URL and features
        AnalyzeOptions parameters = new AnalyzeOptions.Builder()
            .url(url)
            .features(features)
            .build();
        
        // Execute the analysis and get the result
        AnalysisResults response = naturalLanguageUnderstanding
            .analyze(parameters)
            .execute()
            .getResult();
        
        // Print the result to the console
        System.out.println(response);
        
        if (isValidJSON(response)) {
            System.out.println("该字符串是有效的 JSON。");
        } else {
            System.out.println("该字符串不是有效的 JSON。");
        }
    }
}
