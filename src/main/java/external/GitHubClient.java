package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
    private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
    private static final String DEFAULT_KEYWORD = "developer";

    public List<Item> search(double lat, double lon, String keyword) {
        if (keyword == null) {
            keyword = DEFAULT_KEYWORD;
        }
        // 编码关键词以防止特殊字符
        keyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        String url = String.format(URL_TEMPLATE, keyword, lat, lon);

        // 使用 try-with-resources 确保资源自动关闭
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

            if (response.getStatusLine().getStatusCode() != 200) {
                return new ArrayList<>();
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return new ArrayList<>();
            }

            // 读取响应内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }

            // 解析为 JSON 数组并转换为 Item 列表
            JSONArray array = new JSONArray(responseBody.toString());
            return getItemList(array);

        } catch (IOException e) {
            // 记录异常信息或返回错误
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // 将 JSONArray 转换为 List<Item>
    private List<Item> getItemList(JSONArray array) {
        List<Item> itemList = new ArrayList<>();
        List<String> descriptionList = new ArrayList<>();

        // 提取 description 或 title
        for (int i = 0; i < array.length(); i++) {
            String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
            if (description.isEmpty() || description.equals("\n")) {
                descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
            } else {
                descriptionList.add(description);
            }
        }

        // 通过 MonkeyLearn 提取关键词
        List<List<String>> keywords = MonkeyLearnClient.extractKeywords(descriptionList.toArray(new String[0]));

        // 创建 Item 对象
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            // 确保获取 itemId 和 name 这两个必需字段
            String itemId = getStringFieldOrEmpty(object, "id");
            String name = getStringFieldOrEmpty(object, "title");

            if (itemId.isEmpty() || name.isEmpty()) {
                // 如果 itemId 或 name 为空，则跳过此项
                continue;
            }

            // 使用新的构造函数构建 ItemBuilder
            ItemBuilder builder = new ItemBuilder(itemId, name);

            // 设置可选字段
            builder.setAddress(getStringFieldOrEmpty(object, "location"))
                   .setUrl(getStringFieldOrEmpty(object, "url"))
                   .setImageUrl(getStringFieldOrEmpty(object, "company_logo"));

            // 设置关键词
            List<String> list = keywords.get(i);
            builder.setKeywords(new HashSet<>(list));

            // 构建 Item 对象并添加到列表中
            itemList.add(builder.build());
        }
        return itemList;
    }

    // 获取 JSONObject 中指定字段的值，如果字段不存在则返回空字符串
    private String getStringFieldOrEmpty(JSONObject obj, String field) {
        return obj.isNull(field) ? "" : obj.getString(field);
    }
}
