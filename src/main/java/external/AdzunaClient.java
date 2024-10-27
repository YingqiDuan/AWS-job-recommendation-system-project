package external;

import java.io.BufferedReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import entity.Item;
import entity.Item.ItemBuilder;

public class AdzunaClient {
	private static final String URL_TEMPLATE = 
			"https://api.adzuna.com/v1/api/jobs/us/search/"
			+ "1?app_id=%s&app_key=%s&results_per_page=50&what=%s&where=%s&distance=100&max_days_old=60&sort_by=salary&full_time=1";
	
	public static final String APP_ID = "4b5a8275";
	public static final String APP_KEY = "f2e0a2a056514d512f7fb39560ff31b3";
	private static final String DEFAULT_KEYWORD = "software engineer";
	private static final String DEFAULT_LOCATION = "San Jose";
	public static final String COUNTRY ="us";
	
	public String getID() {
		return APP_ID;
	}
	
	public String getKEY() {
		return APP_KEY;
	}
	
//	private String encode(String value) {
//	    try {
//	        return URLEncoder.encode(value, "UTF-8");
//	    } catch (UnsupportedEncodingException e) {
//	        e.printStackTrace();
//	        return "";
//	    }
//	}

	
	
	public List<Item> search(String id, String key, String keyword, String location) {
		id = (id == null) ? APP_ID : id;
        key = (key == null) ? APP_KEY : key;
        keyword = (keyword == null) ? DEFAULT_KEYWORD : keyword;
        location = (location == null) ? DEFAULT_LOCATION : location;
		
//        String url = String.format(URL_TEMPLATE, encode(id), encode(key), encode(keyword), encode(location));

		try {
			// 构建 URI
            URI uri = null;
			try {
				uri = new URIBuilder()
				        .setScheme("https")
				        .setHost("api.adzuna.com")
				        .setPath("/v1/api/jobs/" + COUNTRY + "/search/1")
				        .setParameter("app_id", APP_ID)
				        .setParameter("app_key", APP_KEY)
				        .setParameter("results_per_page", "50")
				        .setParameter("what", DEFAULT_KEYWORD)
				        .setParameter("where", DEFAULT_LOCATION)
				        .build();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // 创建 HTTP 客户端
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(uri);
            // 发送请求
            HttpResponse response = httpClient.execute(request);

            // 检查响应状态
            int statusCode = response.getStatusLine().getStatusCode();
			
			if (statusCode != 200) {
				return new ArrayList<Item>();
			}
		
		
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return new ArrayList<Item>();
			}
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
			    StringBuilder responseBody = new StringBuilder();
			    String line;
			    while ((line = reader.readLine()) != null) {
			        responseBody.append(line);
			    }
			    
			    ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(responseBody.toString());
                JsonNode results = rootNode.path("results");
				return getItemList(results);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<Item>();
	}
	
	public List<Item> getItemList(JsonNode arr) {
		List<Item> itemList = new ArrayList<>();
		List<String> descriptionList = new ArrayList<>();
		
		for (JsonNode obj : arr) {
			String id = obj.path("id").asText();
            String title = obj.path("title").asText();
            String address = obj.path("location").path("display_name").asText();
            String url = obj.path("redirect_url").asText();
            String description=obj.path("description").asText();
			
			ItemBuilder builder = new ItemBuilder(id, title);
			builder.setUrl(url);
	        builder.setDescription(description);
	        builder.setAddress(address);
			
			
	        if ("\n".equals(description)) {
	            descriptionList.add(title);
	        } else {
	            descriptionList.add(description);
	        }
			
			Item item = builder.build();
			itemList.add(item);
		}
		
		// 使用并发调用 IBM Watson API
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<List<String>>> futures = new ArrayList<>();

        for (String description : descriptionList) {
            futures.add(executor.submit(() -> {
                AnalysisResults res = IBMWatsonNLU.analyzeKeywords(description);
                return IBMWatsonNLU.extractTextFromJson(res);
            }));
        }

        // 获取关键词并设置到 Item 中
        for (int i = 0; i < futures.size(); ++i) {
            try {
                List<String> keywords = futures.get(i).get();
                Set<String> keywordSet = new HashSet<>(keywords);
                itemList.get(i).setKeywords(keywordSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
		return itemList;
	}
	
//	private String getStringFieldOrEmpty(JsonNode obj, String field) {
//		JsonNode node = obj.path(field);
//	    return (node == null || node.isNull()) ? "" : node.asText();
//	}
	
	
//	public static void main(String[] args) {
//		AdzunaClient client = new AdzunaClient();
//		List<Item> itemList = client.search(APP_ID, APP_KEY, DEFAULT_KEYWORD, DEFAULT_LOCATION, COUNTRY);
//		for (Item item : itemList) {
//			JSONObject jObj = item.toJSONObject();
//			System.out.println(jObj);
//		}
//	}
		
}
