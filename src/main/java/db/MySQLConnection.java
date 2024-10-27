package db;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

public class MySQLConnection implements AutoCloseable {
    // 数据库连接
    private Connection conn;

    // 构造函数，连接到数据库，并从 db.properties 文件中读取连接信息
    public MySQLConnection() {
        Properties properties = new Properties();
        try (InputStream input = MySQLConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("对不起，无法找到 db.properties 文件");
                return;
            }
            // 加载属性文件
            properties.load(input);

            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            // 加载驱动程序并连接数据库
            Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
            conn = DriverManager.getConnection(url, username, password);

        } catch (IOException | SQLException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    // 关闭数据库连接
    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 检查数据库连接是否可用
    private boolean isConnectionAvailable() {
        if (conn == null) {
            System.err.println("DB connection failed");
            return false;
        }
        return true;
    }

    // 添加用户的收藏项
    public void setFavoriteItems(String userId, Item item) {
        if (!isConnectionAvailable()) return;
        saveItem(item);

        String sql = "INSERT INTO history (user_id, item_id) VALUES(?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, item.getItemId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 取消用户的收藏项
    public void unsetFavoriteItems(String userId, String itemId) {
        if (!isConnectionAvailable()) return;

        String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 保存物品信息和关键词
    public void saveItem(Item item) {
        if (!isConnectionAvailable()) return;

        String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
        String sql1 = "INSERT IGNORE INTO keywords VALUES (?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             PreparedStatement statement1 = conn.prepareStatement(sql1)) {

            // 插入物品信息
            statement.setString(1, item.getItemId());
            statement.setString(2, item.getName());
            statement.setString(3, item.getAddress());
            statement.setString(4, item.getImageUrl());
            statement.setString(5, item.getUrl());
            statement.executeUpdate();

            // 批量插入关键词
            for (String keyword : item.getKeywords()) {
                statement1.setString(1, item.getItemId());
                statement1.setString(2, keyword);
                statement1.addBatch();
            }
            statement1.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取用户收藏的物品 ID
    public Set<String> getFavoriteItemIds(String userId) {
        if (!isConnectionAvailable()) return new HashSet<>();

        Set<String> favoriteItems = new HashSet<>();
        String sql = "SELECT item_id FROM history WHERE user_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                favoriteItems.add(rs.getString("item_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteItems;
    }

    // 获取用户收藏的物品信息
    public Set<Item> getFavoriteItems(String userId) {
        if (!isConnectionAvailable()) return new HashSet<>();

        Set<Item> favoriteItems = new HashSet<>();
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);
        String sql = "SELECT * FROM items WHERE item_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                try (ResultSet rs = statement.executeQuery()) {
                    // 在调用 rs.getString() 之前，必须调用 rs.next()
                    if (rs.next()) {
                        ItemBuilder builder = new ItemBuilder(rs.getString("item_id"),rs.getString("name"));
                        builder.setAddress(rs.getString("address"))
                               .setImageUrl(rs.getString("image_url"))
                               .setUrl(rs.getString("url"))
                               .setKeywords(getKeywords(itemId));
                        favoriteItems.add(builder.build());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteItems;
    }


    // 获取物品的关键词
    public Set<String> getKeywords(String itemId) {
        if (!isConnectionAvailable()) return new HashSet<>();

        Set<String> keywords = new HashSet<>();
        String sql = "SELECT keyword FROM keywords WHERE item_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, itemId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                keywords.add(rs.getString("keyword"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keywords;
    }

    // 获取用户全名
    public String getFullname(String userId) {
        if (!isConnectionAvailable()) return "";

        String name = "";
        String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name = rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    // 验证登录
    public boolean verifyLogin(String userId, String password) {
        if (!isConnectionAvailable()) return false;

        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, encryptPassword(password));
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 添加新用户
    public boolean addUser(String userId, String password, String firstName, String lastName) {
        if (!isConnectionAvailable()) return false;

        String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, encryptPassword(password)); // 存储加密后的密码
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 密码加密方法
    public String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
