package org.example.steam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constant {
    public static Map<String, Object> map = new HashMap<>();

    public static Map<Long, String> getJobs(){
        Map<Long, String> jobs = (Map<Long, String>) map.get("job");
        if (jobs == null) {
            jobs = new HashMap<>();
            map.put("job", jobs);
        }
        return jobs;
    }

    public static void setJobs(long id, String refreshToken){
        Map<Long, String> jobs = (Map<Long, String>) map.get("job");
        if (jobs == null) {
            jobs = new HashMap<>();
            map.put("job", jobs);
        }
        jobs.put(id, refreshToken);
    }

    public static Integer getClientSessionid(){
        Integer clientSessionid = (Integer) map.get("client_sessionid");
        if (clientSessionid == null) {
            clientSessionid = 0;
        }
        return clientSessionid;
    }

    public static void setClientSessionid(Integer id){
        map.put("client_sessionid", id);
    }


}
