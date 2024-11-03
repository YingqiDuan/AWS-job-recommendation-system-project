package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class RpcHelper {

    // 将响应的 ContentType 设置为 application/json
    private static void setResponseContentType(HttpServletResponse response) {
        response.setContentType("application/json");
    }

    // Writes a JSONArray to HTTP response
    public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
        setResponseContentType(response);
        response.getWriter().print(array.toString());
    }

    // Writes a JSONObject to HTTP response
    public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {
        setResponseContentType(response);
        response.getWriter().print(obj.toString());
    }

    // 从 HTTP 请求中读取并解析 JSONObject，增加了异常处理
    public static JSONObject readJSONObject(HttpServletRequest request) throws IOException {
        StringBuilder requestBody = new StringBuilder();

        // 使用 try-with-resources 确保资源关闭
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (IOException e) {
            throw new IOException("Failed to read the request body", e);
        }

        try {
            return new JSONObject(requestBody.toString());
        } catch (JSONException e) {
            throw new IOException("Invalid JSON format", e);
        }
    }

    // 将 JSONObject 转换为 Item 对象，增加了异常处理
    public static Item parseFavoriteItem(JSONObject favoriteItem) {
        ItemBuilder builder = new ItemBuilder(favoriteItem.getString("item_id"),favoriteItem.getString("name"));

        try {
            builder.setAddress(favoriteItem.getString("address"));
            builder.setUrl(favoriteItem.getString("url"));
            builder.setImageUrl(favoriteItem.getString("image_url"));

            Set<String> keywords = new HashSet<>();
            JSONArray array = favoriteItem.getJSONArray("keywords");
            for (int i = 0; i < array.length(); i++) {
                keywords.add(array.getString(i));
            }
            builder.setKeywords(keywords);

        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid JSON structure", e);
        }

        return builder.build();
    }
}
