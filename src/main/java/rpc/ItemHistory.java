package rpc;

import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import db.MySQLConnection;
import entity.Item;

public class ItemHistory extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ItemHistory() {
        super();
    }

    // 辅助方法：检查会话是否有效
    private boolean checkSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);  // Forbidden
            return false;
        }
        return true;
    }

    // 处理 GET 请求，获取用户收藏的项目
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!checkSession(request, response)) return;

        String userId = request.getSession().getAttribute("user_id").toString(); // 从会话中获取 user_id
        try (MySQLConnection connection = new MySQLConnection()) {
            Set<Item> items = connection.getFavoriteItems(userId);
            JSONArray array = new JSONArray();
            for (Item item : items) {
                JSONObject obj = item.toJSONObject();
                obj.put("favorite", true);
                array.put(obj);
            }
            RpcHelper.writeJsonArray(response, array);
        } catch (Exception e) {
            response.setStatus(500);  // Internal Server Error
            e.printStackTrace();
        }
    }

    // 处理 POST 请求，添加用户收藏的项目
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!checkSession(request, response)) return;

        try (MySQLConnection connection = new MySQLConnection()) {
            JSONObject input = RpcHelper.readJSONObject(request);
            String userId = request.getSession().getAttribute("user_id").toString();
            Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));
            connection.setFavoriteItems(userId, item);
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace();
        }
    }

    // 处理 DELETE 请求，删除用户收藏的项目
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!checkSession(request, response)) return;

        try (MySQLConnection connection = new MySQLConnection()) {
            JSONObject input = RpcHelper.readJSONObject(request);
            String userId = request.getSession().getAttribute("user_id").toString();
            Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));
            connection.unsetFavoriteItems(userId, item.getItemId());
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
        } catch (Exception e) {
            response.setStatus(500);
            e.printStackTrace();
        }
    }
}
