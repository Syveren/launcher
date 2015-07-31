/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxuptater;

import java.text.NumberFormat;

/**
 *
 * @author kirio
 */
public class Settings {
     private static  String get_exec_dir(){
           return "/opt/client_app/";
     }  
    
    static final  String  base_dir = get_exec_dir();
    
    static public final String server_dns = System.getProperty("server.dns",/*put here dns server address. Removed for security reason*/);
    
    static  String bytesToHumanReadableString(long bytes){
                        final NumberFormat nf = NumberFormat.getNumberInstance();
                        nf.setMaximumFractionDigits(2);
                          double val;
                          String suffix;
                          if(bytes==0){
                              val = 0;
                              suffix = "";
                          }
                          else if(bytes<1024){
                              val = bytes;
                              suffix = " байт";
                          }
                          else if(bytes<(1024*1024)){
                              val = ((double)bytes)/(1024);
                              suffix = " Кбайт";
                          }
                          else {
                              val = ((double)bytes)/(1024*1024);
                              suffix = " Mбайт";
                          }
                         return (nf.format(val) +  suffix);  
    }
}
