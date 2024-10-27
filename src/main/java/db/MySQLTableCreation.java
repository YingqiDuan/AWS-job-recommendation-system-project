package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MySQLTableCreation {

    // SQL 语句提取为常量
    private static final String DROP_TABLE_KEYWORDS = "DROP TABLE IF EXISTS keywords";
    private static final String DROP_TABLE_HISTORY = "DROP TABLE IF EXISTS history";
    private static final String DROP_TABLE_ITEMS = "DROP TABLE IF EXISTS items";
    private static final String DROP_TABLE_USERS = "DROP TABLE IF EXISTS users";

    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE items ("
            + "item_id VARCHAR(255) NOT NULL,"
            + "name VARCHAR(255),"
            + "address VARCHAR(255),"
            + "image_url VARCHAR(255),"
            + "url VARCHAR(255),"
            + "PRIMARY KEY (item_id)"
            + ")";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE users ("
            + "user_id VARCHAR(255) NOT NULL,"
            + "password VARCHAR(255) NOT NULL,"
            + "first_name VARCHAR(255),"
            + "last_name VARCHAR(255),"
            + "PRIMARY KEY (user_id)"
            + ")";

    private static final String CREATE_TABLE_KEYWORDS = "CREATE TABLE keywords ("
            + "item_id VARCHAR(255) NOT NULL,"
            + "keyword VARCHAR(255) NOT NULL,"
            + "PRIMARY KEY (item_id, keyword),"
            + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
            + ")";

    private static final String CREATE_TABLE_HISTORY = "CREATE TABLE history ("
            + "user_id VARCHAR(255) NOT NULL,"
            + "item_id VARCHAR(255) NOT NULL,"
            + "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "PRIMARY KEY (user_id, item_id),"
            + "FOREIGN KEY (user_id) REFERENCES users(user_id),"
            + "FOREIGN KEY (item_id) REFERENCES items(item_id)"
            + ")";

    private static final String INSERT_FAKE_USER = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";

    public static void main(String[] args) {
        Properties properties = new Properties();

        // 从 db.properties 中加载数据库连接信息
        try (InputStream input = MySQLTableCreation.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("对不起，无法找到 db.properties 文件");
                return;
            }

            // 加载属性文件
            properties.load(input);

            // 从 db.properties 中获取数据库连接 URL、用户名和密码
            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            System.out.println("Connecting to " + url);

            // 使用 try-with-resources 确保资源自动关闭
            try (Connection conn = DriverManager.getConnection(url, username, password);
                 Statement statement = conn.createStatement()) {

                // Step 1: 删除已有的表（如果存在）
                statement.executeUpdate(DROP_TABLE_KEYWORDS);
                statement.executeUpdate(DROP_TABLE_HISTORY);
                statement.executeUpdate(DROP_TABLE_ITEMS);
                statement.executeUpdate(DROP_TABLE_USERS);

                // Step 2: 创建新的表
                statement.executeUpdate(CREATE_TABLE_ITEMS);
                statement.executeUpdate(CREATE_TABLE_USERS);
                statement.executeUpdate(CREATE_TABLE_KEYWORDS);
                statement.executeUpdate(CREATE_TABLE_HISTORY);

                // Step 3: 插入一个示例用户
                statement.executeUpdate(INSERT_FAKE_USER);

                System.out.println("Import done successfully");

            } catch (SQLException e) {
                // 捕获具体的 SQL 异常并处理
                System.err.println("Database operation failed");
                e.printStackTrace();
            }

        } catch (IOException e) {
            // 捕获文件 IO 异常
            System.err.println("Failed to load db.properties file");
            e.printStackTrace();
        }
    }
}
