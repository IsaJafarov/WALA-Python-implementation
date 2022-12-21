package com.ibm.wala.cast.python.global;

import java.util.LinkedList;
import java.util.List;

public class XmlSummaries {
    private static class SingletonHolder {
        private static XmlSummaries instance = new XmlSummaries();
    }

    private XmlSummaries() {
    }

    public static XmlSummaries getInstance() {
        return XmlSummaries.SingletonHolder.instance;
    }

    private List<String> summaries = new LinkedList<>();

    public boolean contains(String obj) {
        if (summaries.contains(obj)){
            return true;
        }
        String pkg="L"+obj;
        String classOrFuncs="/"+obj;
        for (String summary:summaries ){
            if (summary.equals(pkg)||summary.endsWith(classOrFuncs)){
                return true;
            }
        }
        return false;
    }

    public boolean add(String obj) {
        return summaries.add(obj);
    }
}
