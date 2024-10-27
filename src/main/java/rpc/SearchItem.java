package rpc;

import java.io.IOException;
import java.util.List;
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
import external.AdzunaClient;

/**
 * Servlet implementation class SearchItem
 */

public class SearchItem extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public SearchItem() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 会话验证
        if (!isSessionValid(request, response)) {
            return;
        }

        try {
        	String userId = request.getParameter("user_id");
    		
    		String apiId = request.getParameter("app_id");
    		String apiKey = request.getParameter("app_key");
    		String position = request.getParameter("what");
    		String location = request.getParameter("where");

            // 调用 GitHub API 获取职位列表
    		AdzunaClient client = new AdzunaClient();
            List<Item> items = client.search(apiId, apiKey, position, location);

            // 获取用户收藏的职位ID
            Set<String> favoritedItemIds;
            try (MySQLConnection connection = new MySQLConnection()) {
                favoritedItemIds = connection.getFavoriteItemIds(userId);
            }

            // 构建返回的 JSON 数组
            JSONArray array = new JSONArray();
            for (Item item : items) {
                JSONObject obj = item.toJSONObject();
                obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
                array.put(obj);
            }

            // 将 JSON 响应写入
            RpcHelper.writeJsonArray(response, array);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid location format.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(response.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * 会话验证
     */
    private boolean isSessionValid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("Session is invalid or expired.");
            return false;
        }
        return true;
    }
}
