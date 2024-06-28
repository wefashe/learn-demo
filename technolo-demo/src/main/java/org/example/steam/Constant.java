package org.example.steam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constant {
    public static Map<String, Object> map = new HashMap<>();

    public static List<Long> getJobs(){
        List<Long> jobs = (List<Long>) map.get("job");
        if (jobs == null) {
            jobs = new ArrayList<>();
            map.put("job", jobs);
        }
        return jobs;
    }

    public static void setJobs(long id){
        List<Long> jobs = (List<Long>) map.get("job");
        if (jobs == null) {
            jobs = new ArrayList<>();
            map.put("job", jobs);
        }
        jobs.add(id);
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
