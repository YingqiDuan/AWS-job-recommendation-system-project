package testRpc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

import rpc.RpcHelper;

public class writeJsonTest {

    @Test
    public void testWriteJsonObject() throws Exception {
        // 准备模拟 HttpServletResponse 和 PrintWriter
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        // 当调用 response.getWriter() 时，返回 writer
        when(response.getWriter()).thenReturn(writer);

        // 创建要返回的 JSON 对象
        JSONObject obj = new JSONObject();
        obj.put("result", "success");

        // 调用被测试的方法
        RpcHelper.writeJsonObject(response, obj);

        // 验证内容是否写入响应
        writer.flush(); // 确保内容已被写入
        assert(stringWriter.toString().contains("\"result\":\"success\""));
    }

    @Test
    public void testWriteJsonArray() throws Exception {
        // 准备模拟 HttpServletResponse 和 PrintWriter
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        // 当调用 response.getWriter() 时，返回 writer
        when(response.getWriter()).thenReturn(writer);

        // 创建要返回的 JSON 数组
        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "001"));
        array.put(new JSONObject().put("id", "002"));

        // 调用被测试的方法
        RpcHelper.writeJsonArray(response, array);

        // 验证内容是否写入响应
        writer.flush();
        assert(stringWriter.toString().contains("\"id\":\"001\""));
        assert(stringWriter.toString().contains("\"id\":\"002\""));
    }
}
