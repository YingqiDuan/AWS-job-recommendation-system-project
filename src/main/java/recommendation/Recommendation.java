package recommendation;

import java.util.*;
import java.util.Map.Entry;

import db.MySQLConnection;
import entity.Item;
import external.AdzunaClient;

public class Recommendation {

    public List<Item> recommendItems(String userId, String position, String location) {
        // 初始化推荐列表
        List<Item> recommendedItems = new ArrayList<>();

        // Step 1: 获取所有已收藏的职位 ID
        Set<String> favoritedItemIds;
        Map<String, Integer> allKeywords = new HashMap<>();

        try (MySQLConnection connection = new MySQLConnection()) {
            // 获取用户收藏的职位ID
            favoritedItemIds = connection.getFavoriteItemIds(userId);

            // Step 2: 获取所有关键词并统计其出现次数
            for (String itemId : favoritedItemIds) {
                Set<String> keywords = connection.getKeywords(itemId);
                for (String keyword : keywords) {
                    allKeywords.put(keyword, allKeywords.getOrDefault(keyword, 0) + 1);
                }
            }
        }

        // Step 2.1: 按关键词出现次数排序，并选取最多的三个关键词
        List<Entry<String, Integer>> keywordList = new ArrayList<>(allKeywords.entrySet());
        keywordList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

        if (keywordList.size() > 3) {
            keywordList = keywordList.subList(0, 3);
        }

        // Step 3: 基于关键词搜索职位，并过滤掉已收藏的职位
        Set<String> visitedItemIds = new HashSet<>();
        AdzunaClient client = new AdzunaClient();

        for (Entry<String, Integer> keyword : keywordList) {
            List<Item> items = client.search(AdzunaClient.APP_ID, AdzunaClient.APP_KEY, position, location);

            for (Item item : items) {
                // 如果职位尚未被收藏且未访问过，则将其添加到推荐列表中
                if (!favoritedItemIds.contains(item.getItemId()) && visitedItemIds.add(item.getItemId())) {
                    recommendedItems.add(item);
                }
            }
        }

        return recommendedItems;
    }
}
