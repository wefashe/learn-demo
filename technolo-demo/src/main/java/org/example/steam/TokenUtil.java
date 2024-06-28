package org.example.steam;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.Base64;

public class TokenUtil {

    public static JSONObject decodeToken(String token){
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid TOKEN");
        }
        String standardBase64 = parts[1].replace('-', '+').replace('_', '/');
        return JSONUtil.parseObj(new String(Base64.getDecoder().decode(standardBase64)));
    }

}
