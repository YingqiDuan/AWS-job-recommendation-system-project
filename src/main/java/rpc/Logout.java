package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Logout
 */

public class Logout extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Logout() {
        super();
        // 构造函数留空，因为 HttpServlet 没有特殊逻辑需要在构造时执行
    }

    /**
     * Handles GET requests for logging out a user.
     * Invalidates the current session and redirects the user to the homepage (index.html).
     *
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Attempt to retrieve the current session, without creating a new one if it doesn't exist
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Invalidate the session if it exists, destroying the session and removing all associated attributes
            session.invalidate();
        }
        // Redirect the user to the homepage after logging out
        response.sendRedirect("index.html");
    }

    /**
     * Handles POST requests by delegating to the GET method.
     *
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Forward POST requests to the doGet method for unified logout logic
        doGet(request, response);
    }
}
