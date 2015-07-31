/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxuptater;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kirio
 */
public class Javafxuptater extends Application {
   static Updater updater;
   
     final static org.slf4j.Logger logger = LoggerFactory.getLogger(Javafxuptater.class);
    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        updater = new  Updater();
            
   
       boolean checkCompletLastUpdate = updater.checkCompletLastUpdate();
       logger.info("check last update complete->{}",checkCompletLastUpdate);
      
        boolean need_update = false;
        if(checkCompletLastUpdate==false)
              need_update = true;

        try{
            if(!need_update)
                need_update = updater.checkNeedUpdate();
            logger.info("Need update->{}",need_update);
            
        }catch(IOException ex){
            
            logger.error("cant get last version from server",ex);
            need_update = false;
        }
        
     
        if(need_update==true) 
        {
            UpdateDialog updateDlg = new UpdateDialog(updater);
            updateDlg.showAndWait();
            
            if(updater.last_result==Updater.RESULT_NEED_REBOOT || updater.last_result==Updater.RESULT_NEED_RELOAD){
                ExitDialog dlg = new ExitDialog("Приложение будет перезапущено.",false);
                dlg.showAndWait();
                Platform.exit();
                return;
            }
              
            
        }
        
        
        try {
           runClientApp();
           String text = "Компьютер сейчас будет выключен!";
           
         
           ExitDialog dlg = new ExitDialog(text,false);
           
           dlg.showAndWait();
        }
        catch(Throwable tw){
        
            ExitDialog dlg = new ExitDialog("Ошибка: "+tw.getLocalizedMessage(),true);
            dlg.showAndWait();
          
            
        }
          Platform.exit();

      
     
        
    }

    /**
     */
 
 
    
        
   

    public static void main(String[] args)  {
   
            try {
                launch(args);
          
                System.exit(updater.last_result);
            }catch(Throwable ex)
            {
                logger.error("unknown error",ex);
                try {
                    Thread.sleep(18000);
                } catch (InterruptedException ex1) {
                    
                }
            
            }
            
//        if("true".equals(System.getProperty("ignoreupdate")))
//        {   
//            logger.info("ignoreupdate property found. Run client_app without update");
//             runClientApp();
//             return;
//        
//        }
//       updater = new  Updater();
//            
//   
//       boolean checkCompletLastUpdate = updater.checkCompletLastUpdate();
//       logger.info("check last update complete->{}",checkCompletLastUpdate);
//      
//        if(checkCompletLastUpdate==false){
//               
//             logger.info("Run complete previous update mode");
//              
//              launch(args);
//              logger.info("exit with code for reboot");
//              System.exit(Updater.RESULT_NEED_REBOOT);
//              return;
//        }
//        
//        
//        boolean need_update;
//        try{
//            need_update = updater.checkNeedUpdate();
//            logger.info("Need update->{}",need_update);
//        }catch(IOException ex){
//            logger.error("cant get last version from server",ex);
//            need_update = false;
//        }
//        if(need_update==false)
//        {
//            runClientApp();
//        }
//        else {
//            launch(args);
//            if(updater.last_result==Updater.RESULT_NEED_REBOOT || updater.last_result==Updater.RESULT_NEED_RELOAD)
//                System.exit(updater.last_result);
//            runClientApp();
//        }
//  
  
        //String jreDirectory = System.getProperty("java.home");
    
           
        
     
    }
      
 
    static public int runClientApp() throws Exception{
       try {
            
           logger.info("execute client_app");
           String logbackproperty = System.getProperty("logback.configurationFile");
           ProcessBuilder processBuilder;
        
           if(logbackproperty!=null)
                processBuilder = new ProcessBuilder(System.getProperty("java.home")+"/bin/java","-Dlogback.configurationFile="+logbackproperty,"-jar"
                   ,Settings.base_dir+"client_app.jar");
           else 
                 processBuilder = new ProcessBuilder(System.getProperty("java.home")+"/bin/java","-jar"
                   ,Settings.base_dir+"client_app.jar");
           processBuilder.redirectErrorStream(true);
           Process process;
           
           process = processBuilder.start();
    
            process.getInputStream().close(); // because overflow buffer 
           // System.out.println(ProccessHelper.getProccessOutput(process.getInputStream()));
           int waitFor = process.waitFor();
           logger.info("client_app exited with code {}",waitFor);
      
           return waitFor;
           
       } catch (InterruptedException ex) {
             logger.error("InterruptedException",ex);
           throw ex;
         
          
 
       } catch (IOException ex) {
           logger.error("cant execute client_app",ex);
           throw ex;
            
       }catch(Throwable error)
       {
            logger.error("fatal ertor",error);
            throw error;
       }
    }
}
