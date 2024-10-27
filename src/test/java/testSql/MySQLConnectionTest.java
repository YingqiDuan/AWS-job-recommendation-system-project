package testSql;

import db.MySQLConnection;
import entity.Item;

import java.util.HashSet;
import java.util.Set;

public class MySQLConnectionTest {

    public static void main(String[] args) {
        // 创建数据库连接实例
        MySQLConnection conn = new MySQLConnection();

        // 测试添加用户
        System.out.println("Testing addUser...");
        boolean userAdded = conn.addUser("12345", "password123", "John", "Doe");
        if (userAdded) {
            System.out.println("User added successfully.");
        } else {
            System.out.println("Failed to add user.");
        }

        // 测试登录验证
        System.out.println("Testing verifyLogin...");
        boolean loginSuccess = conn.verifyLogin("12345", "password123");
        if (loginSuccess) {
            System.out.println("Login successful.");
        } else {
            System.out.println("Login failed.");
        }

        // 创建一个测试Item对象
        Set<String> keywords = new HashSet<>();
        keywords.add("electronics");
        keywords.add("laptop");

        Item item = new Item.ItemBuilder("001", "Laptop")
                .setAddress("123 Tech Ave")
                .setImageUrl("http://example.com/laptop.jpg")
                .setUrl("http://example.com/laptop")
                .setKeywords(keywords)
                .build();

        // 测试用户添加收藏
        System.out.println("Testing setFavoriteItems...");
        conn.setFavoriteItems("12345", item);
        System.out.println("Favorite item added.");

        // 获取用户收藏的物品
        System.out.println("Testing getFavoriteItems...");
        Set<Item> favoriteItems = conn.getFavoriteItems("12345");
        for (Item favItem : favoriteItems) {
            System.out.println("Favorite Item: " + favItem.getName());
        }

        // 测试取消收藏
        System.out.println("Testing unsetFavoriteItems...");
        conn.unsetFavoriteItems("12345", "001");
        System.out.println("Favorite item removed.");

        // 再次检查收藏的物品
        favoriteItems = conn.getFavoriteItems("12345");
        if (favoriteItems.isEmpty()) {
            System.out.println("No favorite items left.");
        } else {
            for (Item favItem : favoriteItems) {
                System.out.println("Favorite Item after removal: " + favItem.getName());
            }
        }

        // 测试获取用户全名
        System.out.println("Testing getFullname...");
        String fullname = conn.getFullname("12345");
        System.out.println("Full name: " + fullname);

        // 关闭数据库连接
        conn.close();
    }
}
