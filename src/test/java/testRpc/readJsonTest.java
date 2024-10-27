package testRpc;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import rpc.RpcHelper;

public class readJsonTest {

    @Test
    public void testReadJSONObject() throws Exception {
        // 模拟 HttpServletRequest，准备输入数据
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String json = "{\"item_id\":\"001\",\"name\":\"Laptop\"}";

        // 当调用 request.getReader() 时，返回 BufferedReader
        BufferedReader reader = new BufferedReader(new StringReader(json));
        when(request.getReader()).thenReturn(reader);

        // 调用被测试的方法
        JSONObject jsonObject = RpcHelper.readJSONObject(request);

        // 验证解析结果
        assertEquals("001", jsonObject.getString("item_id"));
        assertEquals("Laptop", jsonObject.getString("name"));
    }
}
