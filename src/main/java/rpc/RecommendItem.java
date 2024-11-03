package rpc;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import entity.Item;
import recommendation.Recommendation;

/**
 * Servlet implementation class RecommendItem
 */

public class RecommendItem extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RecommendItem() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 会话验证
        if (!isSessionValid(request, response)) {
            return;
        }

        try {
            // 从请求中获取 user_id、lat 和 lon 参数
            String userId = request.getParameter("user_id");
            double lat = Double.parseDouble(request.getParameter("lat"));
            double lon = Double.parseDouble(request.getParameter("lon"));

            // 推荐系统生成推荐职位
            Recommendation recommendation = new Recommendation();
            List<Item> items = recommendation.recommendItems(userId, lat, lon);

            // 构建 JSON 响应
            JSONArray array = new JSONArray();
            items.forEach(item -> array.put(item.toJSONObject()));

            RpcHelper.writeJsonArray(response, array);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid latitude or longitude format.");
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
     * 验证会话是否有效
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
