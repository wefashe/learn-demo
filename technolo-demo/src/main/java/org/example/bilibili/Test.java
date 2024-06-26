package org.example.bilibili;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 参考项目
 * https://github.com/OrdinaryRoad-Project/ordinaryroad-live-chat-client.git
 */
@Slf4j
public class Test {
    public static void main(String[] args) throws Exception {
        // https://api.live.bilibili.com/room/v3/area/getRoomList
        String userId = "630900036";
        String api = StrUtil.format("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid={}", userId);
        String jsonStr = HttpUtil.get(api, CharsetUtil.CHARSET_UTF_8);
        JSONObject dataObj = JSONUtil.parseObj(jsonStr).getJSONObject("data");
        log.info("直播间标题：{}", dataObj.getStr("title"));
        log.info("直播间地址：{}", dataObj.getStr("url"));
        LiveStatusEnum status = LiveStatusEnum.getByCode(dataObj.getInt("liveStatus"));
        if (status == LiveStatusEnum.STOPPED) {
            log.info("直播间状态：未开播");
            return;
        } else {
            log.info("直播间状态：已开播");
        }
        int roomId = dataObj.getInt("roomid");
        api = StrUtil.format("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo?id={}&type=0",roomId);
        jsonStr = HttpUtil.get(api, CharsetUtil.CHARSET_UTF_8);
        dataObj = JSONUtil.parseObj(jsonStr).getJSONObject("data");
        String token = dataObj.getStr("token");
        JSONArray hostArr = dataObj.getJSONArray("host_list");
        JSONObject hostObj = (JSONObject) hostArr.get(RandomUtil.randomInt(0, hostArr.size()));

        api = "https://api.bilibili.com/x/frontend/finger/spi";
        jsonStr = HttpUtil.get(api, CharsetUtil.CHARSET_UTF_8);
        String buvid3 = JSONUtil.parseObj(jsonStr).getJSONObject("data").getStr("b_3");

        String wss = StrUtil.format("wss://{}:{}/sub",hostObj.getStr("host") , hostObj.getInt("wss_port"));
        String ws = StrUtil.format("ws://{}:{}/sub",hostObj.getStr("host") , hostObj.getInt("ws_port"));

        JSONObject authObj = JSONUtil.createObj()
                .set("uid", 0)
                .set("roomid", roomId)
                .set("protover", 3)
                .set("buvid", buvid3)
                .set("platform", "web")
                .set("type", 2)
                .set("key", token);
        // System.out.println(authObj.toStringPretty());
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        WebSocketClient client = new WebSocketClient(wss, authObj.toString());
        client.connect();
    }
}
