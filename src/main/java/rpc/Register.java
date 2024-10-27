package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import db.MySQLConnection;


public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public Register() {
        super();
    }

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject input = RpcHelper.readJSONObject(request);

		// 读取并验证用户输入
		try {
			String userId = input.getString("user_id");
			String password = input.getString("password");
			String firstname = input.getString("first_name");
			String lastname = input.getString("last_name");

			// 创建数据库连接
			MySQLConnection connection = new MySQLConnection();
			JSONObject obj = new JSONObject();
			
			// 调用 addUser 添加用户
			if (connection.addUser(userId, password, firstname, lastname)) {
				obj.put("status", "OK");
			} else {
				obj.put("status", "User Already Exists");
			}

			// 关闭数据库连接
			connection.close();
			
			// 返回响应
			RpcHelper.writeJsonObject(response, obj);
			
		} catch (Exception e) {
			// 错误处理
			e.printStackTrace();
			JSONObject error = new JSONObject();
			error.put("status", "error");
			error.put("message", e.getMessage());
			RpcHelper.writeJsonObject(response, error);
		}
	}
}
