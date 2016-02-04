package com.liferay.docs.command.unix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by carlos on 05/02/16.
 */
public class AbstractCommand {

    public static final String newLine = "\n";
    protected String readStdIn(){
        StringBuffer result=new StringBuffer();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while((input=br.readLine())!=null){
                result.append(input);
                result.append(newLine);
            }
        }catch(IOException io){
            io.printStackTrace();
        }
        return result.toString();
    }

    protected Map<String, List<String>> getParams(String... args){
        Map<String, List<String>> params = new HashMap<>();

        List<String> options = null;
        for (int i = 0; i < args.length; i++) {
            final String a = args[i];

            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return params;
                }
                options = new ArrayList<>();
                params.put(a.substring(1), options);
            }
            else if (options != null) {
                options.add(a);
            }
            else {
                System.err.println("Illegal parameter usage");
                return params;
            }
        }
        return params;
    }
}
