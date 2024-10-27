package external;

import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;

public class IBMWatsonNLU {

    // 将 API 密钥设置为环境变量
	static final String IBM_API_KEY = "6CdKaFalSQC8weFKox40hX8ab5MA-qYYM0Gd0o4_9a_M";
    private static NaturalLanguageUnderstanding service;

    // 初始化服务实例
    static {
        IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(IBM_API_KEY).build();
        service = new NaturalLanguageUnderstanding("2022-04-07", authenticator);
        service.setServiceUrl("https://api.us-south.natural-language-understanding.watson.cloud.ibm.com/instances/55ab98a3-5f81-4ce5-a18e-077fe64cb09d");
    }

    static AnalysisResults analyzeKeywords(String text) {
        try {
            EntitiesOptions entities = new EntitiesOptions.Builder()
                .sentiment(false)
                .limit(3L)
                .build();
            Features features = new Features.Builder()
                .entities(entities)
                .build();
            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                .text(text)
                .features(features)
                .build();

            return service.analyze(parameters).execute().getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static List<String> extractTextFromJson(AnalysisResults analysisResults) {
    	if (analysisResults == null) {
          return new ArrayList<>();
    	}
    	
    	JSONObject jsonObject = new JSONObject(analysisResults);
    	List<String> entityTexts = extractEntityTexts(jsonObject);
    	return entityTexts;
    }
    
    private static List<String> extractEntityTexts(JSONObject jsonObject) {
        List<String> entityTexts = new ArrayList<>();
        
        // 检查是否存在entities数组
        if (jsonObject.has("entities")) {
            JSONArray entitiesArray = jsonObject.getJSONArray("entities");
            
            // 遍历entities数组并提取每个实体的text字段
            for (int i = 0; i < entitiesArray.length(); i++) {
                JSONObject entity = entitiesArray.getJSONObject(i);
                if (entity.has("text")) {
                    entityTexts.add(entity.getString("text"));
                }
            }
        }
        
        return entityTexts;
    }
    

//    // 提取 JSON 中的文本字段
//    public static List<String> extractTextFromJson(AnalysisResults analysisResults) {
//        if (analysisResults == null) {
//            return new ArrayList<>();
//        }
//        JSONObject json = new JSONObject(analysisResults.toString());
//        List<String> extractedTexts = new ArrayList<>();
//        extractTextRecursively(json, extractedTexts);
//        return extractedTexts;
//    }
//
//    private static void extractTextRecursively(Object element, List<String> texts) {
//        if (element instanceof JSONObject) {
//            JSONObject jsonObject = (JSONObject) element;
//            jsonObject.keySet().forEach(key -> {
//                if ("text".equals(key)) {
//                    texts.add(jsonObject.get(key).toString());
//                } else {
//                    extractTextRecursively(jsonObject.get(key), texts);
//                }
//            });
//        } else if (element instanceof JSONArray) {
//            JSONArray jsonArray = (JSONArray) element;
//            for (int i = 0; i < jsonArray.length(); i++) {
//                extractTextRecursively(jsonArray.get(i), texts);
//            }
//        }
//    }
}
