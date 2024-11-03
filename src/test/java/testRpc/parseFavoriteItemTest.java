package testRpc;

import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import entity.Item;

import rpc.RpcHelper;

public class parseFavoriteItemTest {

    @Test
    public void testParseFavoriteItem() {
        // 准备要解析的 JSON 对象
        JSONObject json = new JSONObject();
        json.put("item_id", "001");
        json.put("name", "Laptop");
        json.put("address", "123 Tech Ave");
        json.put("image_url", "http://example.com/laptop.jpg");
        json.put("url", "http://example.com/laptop");
        json.put("keywords", new JSONArray().put("electronics").put("laptop"));

        // 调用被测试的方法
        Item item = RpcHelper.parseFavoriteItem(json);

        // 验证结果
        assertEquals("001", item.getItemId());
        assertEquals("Laptop", item.getName());
        assertEquals("123 Tech Ave", item.getAddress());
        assertEquals("http://example.com/laptop.jpg", item.getImageUrl());
        assertEquals("http://example.com/laptop", item.getUrl());
        assertTrue(item.getKeywords().contains("electronics"));
        assertTrue(item.getKeywords().contains("laptop"));
    }
}
