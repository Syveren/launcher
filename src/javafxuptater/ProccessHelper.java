/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxuptater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author kirio
 */
public interface ProccessHelper {

    public static String getProccessOutput(InputStream proccessStream) throws IOException 
    {
        InputStreamReader isr = new InputStreamReader(proccessStream);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder(10000);
        char[] buff = new char[4096];
        int len = 0;
        while ((len = br.read(buff,0,buff.length)) != -1) {  
           builder.append(buff,0,len);  
        }  
        return builder.toString();
    }
    
    
}
