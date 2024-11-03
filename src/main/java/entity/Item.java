package entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class Item {
    private String itemId;
    private String name;
    private String address;
    private Set<String> keywords;
    private String imageUrl;
    private String url;

    // 私有构造函数，使用 Builder 创建对象
    private Item(ItemBuilder builder) {
        this.itemId = builder.itemId;
        this.name = builder.name;
        this.address = builder.address;
        this.imageUrl = builder.imageUrl;
        this.url = builder.url;
        // 使用防御性拷贝，避免外部修改关键字集合
        this.keywords = builder.keywords != null ? new HashSet<>(builder.keywords) : Collections.emptySet();
    }

    // 标准的 getter 方法
    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Set<String> getKeywords() {
        return Collections.unmodifiableSet(keywords);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUrl() {
        return url;
    }

    // 将对象转换为 JSON
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("item_id", itemId);
        obj.put("name", name);
        obj.put("address", address);
        // 仅在 keywords 不为空时才添加该字段
        if (keywords != null && !keywords.isEmpty()) {
            obj.put("keywords", new JSONArray(keywords));
        }
        obj.put("image_url", imageUrl);
        obj.put("url", url);
        return obj;
    }

    // Builder 类
    public static class ItemBuilder {
        private String itemId;
        private String name;
        private String address;
        private String imageUrl;
        private String url;
        private Set<String> keywords;

        // 必需字段，通过构造函数设置
        public ItemBuilder(String itemId, String name) {
            if (itemId == null || itemId.isEmpty() || name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Item ID 和 Name 是必需的字段，不能为空！");
            }
            this.itemId = itemId;
            this.name = name;
        }

        // 可选字段设置方法
        public ItemBuilder setAddress(String address) {
            this.address = address;
            return this;
        }

        public ItemBuilder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public ItemBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public ItemBuilder setKeywords(Set<String> keywords) {
            // 防御性拷贝，避免外部引用直接修改内部集合
            this.keywords = keywords != null ? new HashSet<>(keywords) : null;
            return this;
        }

        // 构建 Item 对象
        public Item build() {
            return new Item(this);
        }
    }
}
