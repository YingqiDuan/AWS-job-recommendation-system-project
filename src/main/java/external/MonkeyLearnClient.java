package external;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.monkeylearn.ExtraParam;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnResponse;
import com.monkeylearn.MonkeyLearnException;

public class MonkeyLearnClient {
	
	// 存储 MonkeyLearn API 的密钥 (需替换为您的 API 密钥)
	private static final String API_KEY = "your_monkeylearn_api_key";
	
	// 模型 ID (需替换为正确的模型 ID)
	private static final String MODEL_ID = "your_model_id";

	// 提取关键词的主要方法
    public static List<List<String>> extractKeywords(String[] text) {
		// 如果输入文本为空，返回空列表
        if (text == null || text.length == 0) {
            return new ArrayList<>();
        }

		// 初始化 MonkeyLearn 客户端
        MonkeyLearn ml = new MonkeyLearn(API_KEY);
        
		// 设置额外参数，提取每个文本的最多3个关键词
        ExtraParam[] extraParams = {new ExtraParam("max_keywords", "3")};

		try {
			// 调用 MonkeyLearn API 进行关键词提取
			MonkeyLearnResponse response = ml.extractors.extract(MODEL_ID, text, extraParams);
			
			// 解析 API 响应并返回关键词列表
			JSONArray resultArray = response.arrayResult;
			return getKeywords(resultArray);

		} catch (MonkeyLearnException e) {
			// 捕获 API 调用过程中可能的异常
			System.err.println("Failed to extract keywords: " + e.getMessage());
			e.printStackTrace();
		}
		
		// 如果 API 调用失败，返回空的关键词列表
        return new ArrayList<>();
    }

	// 从 MonkeyLearn API 响应中提取关键词
    private static List<List<String>> getKeywords(JSONArray resultArray) {
		// 创建外层列表，用于存储每个文本的关键词
        List<List<String>> topKeywords = new ArrayList<>();
        
		// 遍历每个文本的关键词结果
        for (int i = 0; i < resultArray.size(); i++) {
            List<String> keywords = new ArrayList<>();
            JSONArray keywordsArray = (JSONArray) resultArray.get(i);

            // 遍历每个文本的关键词并添加到关键词列表
            for (Object keywordObj : keywordsArray) {
                JSONObject keywordJSON = (JSONObject) keywordObj;
                String keyword = (String) keywordJSON.get("keyword");
                keywords.add(keyword);
            }

            // 将关键词列表添加到外层列表
            topKeywords.add(keywords);
        }
        
		// 返回关键词结果
        return topKeywords;
    }

	// 主函数用于测试关键词提取功能
    public static void main(String[] args) throws MonkeyLearnException {
        // 示例文本数据
        String[] data = {
            "Amazon has many new job openings",
            "A maple tree is a tree",
            "Google has many openings in software development",
            "Elon Musk has shared a photo of the spacesuit designed by SpaceX."
        };

        // 提取关键词并打印结果
        List<List<String>> words = extractKeywords(data);
        for (List<String> ws : words) {
            for (String w : ws) {
                System.out.println(w);
            }
            System.out.println();
        }
    }
}
