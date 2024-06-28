package org.example.steam;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws Exception {
        // https://github.com/Longi94/JavaSteam.git
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        // String wss = getWssUrl();
        String wss = "wss://ext1-atl3.steamserver.net:27019/cmsocket/";
        WebSocketClient client = new WebSocketClient(wss);
        client.connect();
    }

    public static String getWssUrl(){
                String url = "https://api.steampowered.com/ISteamDirectory/GetCMListForConnect/v0001/?cellid=0&format=js";
        String result = HttpRequest.get(url)
                .header(Header.USER_AGENT, "user-agent")
                .header(Header.ACCEPT_CHARSET,"ISO-8859-1,utf-8,*;q=0.7")
                .header(Header.ACCEPT,"text/html,*/*;q=0.9")
                .timeout(20000)
                .execute().body();
        JSONObject resultObj = JSONUtil.parseObj(result);
        JSONArray serverList = resultObj.getJSONObject("response").getJSONArray("serverlist");
        List<JSONObject> cmList = serverList.stream().map(obj->(JSONObject)obj)
                .sorted(Comparator.comparing(obj -> ((JSONObject) obj).getFloat("wtd_load")))
                .filter(obj -> StrUtil.equals("websockets", obj.getStr("type")) && StrUtil.equals("steamglobal", obj.getStr("realm"))
                ).collect(Collectors.toList());
        int randUpperBound = Math.min(20, cmList.size());
        JSONObject cm = cmList.get(new Random().nextInt(randUpperBound));
        return StrUtil.format("wss://{}/cmsocket/", cm.getStr("endpoint"));
    }
}
