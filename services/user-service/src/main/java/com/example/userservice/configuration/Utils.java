package com.example.userservice.configuration;

import java.util.List;

public class Utils {
    public static boolean isNullOrEmpty(List<Object> list){
        return list == null || list.isEmpty();
    }
}
