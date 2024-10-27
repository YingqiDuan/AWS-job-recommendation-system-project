package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import db.MySQLConnection;

/**
 * Servlet implementation class Login
 * Handles user login and session management.
 */

public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor.
     */
    public Login() {
        super();
    }

    /**
     * Handle GET requests to check user session status.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);  // Get session if exists, otherwise return null
        JSONObject obj = new JSONObject();

        if (session != null) {
            // Session exists, retrieve user information
            String userId = session.getAttribute("user_id").toString();
            try (MySQLConnection connection = new MySQLConnection()) {  // Auto-close connection with try-with-resources
                obj.put("status", "OK")
                   .put("user_id", userId)
                   .put("name", connection.getFullname(userId));
            } catch (Exception e) {
                obj.put("status", "Database Error").put("error", e.getMessage());
                response.setStatus(500);
            }
        } else {
            // No valid session found
            obj.put("status", "Invalid Session");
            response.setStatus(403);  // Forbidden
        }

        RpcHelper.writeJsonObject(response, obj);
    }

    /**
     * Handle POST requests for user login.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject input = RpcHelper.readJSONObject(request);
        String userId = input.optString("user_id", "");
        String password = input.optString("password", "");

        JSONObject obj = new JSONObject();
        
        if (userId.isEmpty() || password.isEmpty()) {
            obj.put("status", "Missing Credentials");
            response.setStatus(400);  // Bad Request
        } else {
            // Proceed with user validation
            try (MySQLConnection connection = new MySQLConnection()) {
                if (connection.verifyLogin(userId, password)) {
                    // Valid credentials, create a session
                    HttpSession session = request.getSession();
                    session.setAttribute("user_id", userId);
                    session.setMaxInactiveInterval(600);  // 10 minutes session timeout
                    obj.put("status", "OK")
                       .put("user_id", userId)
                       .put("name", connection.getFullname(userId));
                } else {
                    // Invalid credentials
                    obj.put("status", "User Doesn't Exist");
                    response.setStatus(401);  // Unauthorized
                }
            } catch (Exception e) {
                obj.put("status", "Database Error").put("error", e.getMessage());
                response.setStatus(500);  // Internal Server Error
            }
        }

        RpcHelper.writeJsonObject(response, obj);
    }
}
