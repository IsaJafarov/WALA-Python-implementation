package com.ibm.wala.cast.python.util;

import java.util.HashMap;
import java.util.Map;

public class CounterSystem {

    private static class CounterClsInstance {
        private static final CounterSystem instance = new CounterSystem();
    }

    private CounterSystem() {
    }

    private final Map<String, Integer> counterMap=new HashMap<>();

    public static int getCount(String counter){
        int cnt=CounterClsInstance.instance.counterMap.getOrDefault(counter,0);
        CounterClsInstance.instance.counterMap.put(counter, cnt++);
        return cnt;
    }

    public static String getCountHex(String counter){
        return Integer.toHexString(getCount(counter));
    }

//    public static CounterSystem getInstance() {
//        return CounterClsInstance.instance;
//    }


}