package com.ultima.settings.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
    
    public static Boolean doesPropExist(String propName, String propValue) {
        boolean valid = false;

        try {
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) 
            {
                if(line.contains("[" + propName +"]: [" + propValue + "]")){
                    valid = true;
                }
            }
        } 
        catch (IOException e){
            e.printStackTrace();
        }
        return valid;
    }

}
