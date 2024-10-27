package external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class Adzuna_example {
    // 替换为你的 Adzuna API 凭证
    private static final String APP_ID = "4b5a8275";
    private static final String APP_KEY = "f2e0a2a056514d512f7fb39560ff31b3";
    private static final String COUNTRY = "us"; // 例如 "us", "uk", "au" 等

    public static void main(String[] args) {
        try {
            // 构建 URI
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.adzuna.com")
                    .setPath("/v1/api/jobs/" + COUNTRY + "/search/1")
                    .setParameter("app_id", APP_ID)
                    .setParameter("app_key", APP_KEY)
                    .setParameter("results_per_page", "50")
                    .setParameter("what", "software engineer")
                    .setParameter("where", "San Jose")
                    .build();

            // 创建 HTTP 客户端
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(uri);

            // 发送请求
            HttpResponse response = httpClient.execute(request);

            // 检查响应状态
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // 读取响应内容
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                StringBuilder responseBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }

                // 解析 JSON
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(responseBody.toString());
                JsonNode results = rootNode.path("results");

                // 遍历并打印职位信息
                if (results.isArray()) {
                    for (JsonNode job : results) {
                    	String id = job.path("id").asText();
                        String title = job.path("title").asText();
                        String company = job.path("company").path("display_name").asText();
                        String location = job.path("location").path("display_name").asText();
                        String url = job.path("redirect_url").asText();
                        String description=job.path("description").asText();

                        System.out.println("id: " + id);
                        System.out.println("职位名称: " + title);
                        System.out.println("公司: " + company);
                        System.out.println("地点: " + location);
                        System.out.println("链接: " + url);
                        System.out.println("描述: " + description);
                        System.out.println("-----------------------------------");
                    }
                }
            } else {
                System.out.println("请求失败，状态码: " + statusCode);
            }

            // 关闭客户端
            httpClient.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
