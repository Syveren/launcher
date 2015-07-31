/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxuptater;

 
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author kirio
 */
public class Updater {
      final static org.slf4j.Logger logger = LoggerFactory.getLogger(Updater.class);
  
    static final String STATE_CHECK        = "Проверка версий";
    static final String STATE_DOWNLOAD     = "Загрузка файлов ";
    static final String STATE_BACKUP       = "Создание резервной копии";
    static final String STATE_UNZIP        = "Извлечение из архива";
    static final String STATE_COPY         = "Обновление файлов";
    static final String STATE_CLEAN        = "Очистка временных файлов";
    static final String STATE_DONE          = "Обновлено";
   
    
 
  
    
    static final int RESULT_SUCCESS  = 0;
    static final int RESULT_NEED_REBOOT  = 10;
    static final int RESULT_NEED_RELOAD  = 20;
    static final int RESULT_FAIL = 3;
    static final int RESULT_NONE = 4;
    public void completeLastUpdate() throws IOException{
    
        File update_dir = new File(Settings.base_dir+"/update/");

        fireStateChange(STATE_COPY);
        FilesHelper.copyChildFiles(update_dir, update_dir.getParentFile());
        fireStateChange(STATE_CLEAN);
        FilesHelper.remove(update_dir);
    
    }
    
    public boolean checkCompletLastUpdate(){
        //Если папка update существует и не пуста, значит обновление не было завершено.
        File updateDir = new File(Settings.base_dir,"/update/");
        if(updateDir.exists()==false || updateDir.listFiles().length==0)
        {
            if(updateDir.exists())
                FilesHelper.remove(updateDir);
            return true;
        
        }
        return false;
        
    
    }
    public boolean checkNeedUpdate() throws IOException
    {
            String current_version =  getCurrentVersion(); 
 
            logger.info("Current version:'{}'",current_version);
            String last_version    = getLatestVersion();
            logger.info("Last version:'{}'",last_version);
            if(current_version==null){
               logger.info("versions equal:'{}'",false);
                return true;
            }
            logger.info("versions equal:'{}'",current_version.equals(last_version));
 
            return !last_version.equals(current_version);
    
    }
    void fireStateChange(String state){
        if(handler!=null)
            handler.onStateChanged(state);
    }
    
    
    
    interface StateChangeHandler{
        void onStateChanged(String state);
    
    }
    
    StateChangeHandler handler = null;
    
    
    
    
    
    private final static String versionURL = "http://"+PrivateInfo.SERVER_DNS+":8080/AdminServer/clientversion";
   private static final String aboutURL = "http://"+PrivateInfo.SERVER_DNS+":8080/AdminServer/clientversion?about=true";
    private static final String fileUrl = "http://"+PrivateInfo.SERVER_DNS+":8080/AdminServer/clientupdate";
    public String getLatestVersion() throws IOException
    {   
         
        return getData(versionURL);
    }
 
    
    public String getAboutUpdate(){
          try {
              return getData(aboutURL);
          } catch (IOException ex) {
              logger.error("cant get about info",ex);
              return "";
          }
    
    }
    public String getCurrentVersion() {
        ProcessBuilder processBuilder = new ProcessBuilder(System.getProperty("java.home")+"/bin/java","-cp"
                ,Settings.base_dir+"client_app.jar","info.Version");
    	//processBuilder.redirectErrorStream(true);
    	Process process;
        try {
            process = processBuilder.start();
            	InputStream in = process.getInputStream();
                Properties prp = new Properties();
                prp.load(in);
               
                String version = prp.getProperty("version");//ProccessHelper.getProccessOutput(in).trim();
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                logger.error("InterruptedException",ex);
             
            }
              return version;
                
        } catch (IOException ex) {
            logger.error("cant get current version",ex);
            return "";
        }
 
        
      
        
    }

   
    
    
    
    private boolean updateContainsParrentScript(File dir){
          return (dir.listFiles((File dir1, String name) -> (name.equals("launcher.sh") )).length==1);
    
    }
    private  boolean updateContainsUpdaterApp(File dir){
          return (dir.listFiles((File dir1, String name) -> (name.equals("launcher.jar") )).length==1);
    
    }
    
    
    
   int last_result = RESULT_NONE;
    
   public Integer  update(boolean force_update)  {

        logger.info("run update: force={}",force_update);
        last_result = RESULT_NONE;
        setLastError(null);
        //Если папка update существует, значит обновление не было завершено.
        if (Files.exists(Paths.get(Settings.base_dir+"/update/"))) {
            logger.warn("ERROR: if update dir exists, then last update was interrupted! Use function 'completeLastUpdate'!!!");
            System.out.println("ERROR: if update dir exists, then last update was interrupted! Use function 'completeLastUpdate'!!!");
            throw new IllegalStateException("if update dir exists, then last update was interrupted! Use function 'completeLastUpdate'!!!");
             
        }
        File base_dir_file = new File(Settings.base_dir);
        fireStateChange(STATE_CHECK);
            
        if(force_update==false) {
           
            // если форс апдет равен false, то игнорируем апдейт при равных версиях
              logger.debug("get current version");
            String current_version = getCurrentVersion();//String current_version = get current version
               logger.info("current version:{}",current_version);
             
            String last_version;
            try {
                logger.debug("get last version");
                last_version = getLatestVersion();
                logger.info("last version->{}",last_version);
            }
            catch(IOException ex)
            {
                logger.error("cant get last version from server",ex);
                setLastError("Не удалось получить версию с сервера: "+ex.getLocalizedMessage());
                last_result = RESULT_FAIL;
                 fireStateChange(STATE_DONE);
                return last_result;
            }

            if(current_version.equalsIgnoreCase(last_version)){
                System.out.println("versions equel");
                fireStateChange(STATE_DONE);
                last_result = RESULT_SUCCESS;
                return last_result;

            }
        }

       
        fireStateChange(STATE_DOWNLOAD);
        File downloadFile;
        try {
            logger.info("download file from server");
            downloadFile =  downloadFile(fileUrl);
            logger.debug("download complete");
        }
        catch(IOException ex)
        {
            logger.error("cant download file from server",ex);
            setLastError("Не удалось скачать программу с сервера: "+ex.getLocalizedMessage());
            last_result = RESULT_FAIL;
            return last_result;
        
        }
        
        fireStateChange(STATE_BACKUP);
        logger.info("backup files");
        backupCurrentFiles();
        
        
        fireStateChange(STATE_UNZIP);
        
     
        File update_dir = new File(base_dir_file,"update");
        update_dir.mkdir();
         logger.info("unzip downloaded file to {}",update_dir.getAbsolutePath());
        
        unzipNewFiles(downloadFile, update_dir);


        fireStateChange(STATE_COPY);
        logger.info("replace files");
        updateFiles(update_dir, base_dir_file);
       
        
        
        
        fireStateChange(STATE_CLEAN);
        
         logger.info("clean tmp files");
      
      
    
           
        
          if(updateContainsParrentScript(update_dir)){
            last_result = RESULT_NEED_REBOOT;
              logger.info("updated need reboot");
        }
        else if(updateContainsUpdaterApp(update_dir)){
            last_result = RESULT_NEED_RELOAD;
                logger.info("updated need reload");
        }
        else {
            last_result = RESULT_SUCCESS;
                  logger.info("updated.");
        }
        FilesHelper.remove(update_dir);
        FilesHelper.remove(downloadFile);
        fireStateChange(STATE_DONE); 
        
        return last_result;
    }
    
    String last_error = null;

    public String getLastError() {
        return last_error;
    }

    private void setLastError(String last_error) {
        this.last_error = last_error;
    }
    
   
    
    
    
    
    private static String getData(String address) throws IOException
    {
        logger.debug("connect with {}",PrivateInfo.SERVER_DNS);
        URL url = new URL(address);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))){
            StringBuilder builder = new StringBuilder(10000);
            char[] buff = new char[65536];
            int len = 0;
            while ((len = br.read(buff,0,buff.length)) != -1) {  
               builder.append(buff,0,len);  
            }

            return builder.toString().trim(); 
        }

    }

    private  File downloadFile(String link) throws IOException
    {
 
        
        InputStream in = null;
        OutputStream out = null;
        try {
              final NumberFormat nf = NumberFormat.getNumberInstance();
             nf.setMaximumFractionDigits(2);
             nf.setMinimumFractionDigits(2);
            URLConnection openConnection = new URL(link).openConnection();
            long contentLength = openConnection.getContentLengthLong();
            in = new BufferedInputStream(openConnection.getInputStream());
            
            File downloadFile = File.createTempFile("update", "zip");
            
            
            logger.debug("download {} bytes in -> {}",contentLength,downloadFile.getAbsolutePath());
            out = new FileOutputStream( downloadFile);
            
            int buffer_size = 4096;
            final byte data[] = new byte[buffer_size];
            int count;
            long loaded_count = 0;
            while ((count = in.read(data, 0, buffer_size)) != -1) {
                loaded_count+=count;
              
 
                fireStateChange(STATE_DOWNLOAD+" ("+nf.format(((double)loaded_count/contentLength)*100)+" %)"
                +"\n"+Settings.bytesToHumanReadableString(loaded_count)+"/"+Settings.bytesToHumanReadableString(contentLength));
                out.write(data, 0, count);
            }
            fireStateChange(STATE_DOWNLOAD);
            return downloadFile;
        }
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        
        
    }
     
    
 
   private void updateFiles(File update_dir, File exe_dir){
        try {
          
            FilesHelper.copyChildFiles(update_dir, exe_dir);
        } catch (IOException ex) {
            
            // try to restore from backup
            RuntimeException ex2 = new RuntimeException("Ошибка при  обновлении файлов: "+ex.getLocalizedMessage());
            ex2.addSuppressed(ex);
            logger.error("cant update files",ex);
            throw ex2;
        }
   
   }

 
 
 
  private void unzipNewFiles(File sorce,File destDir) 
  {
      
       try{
           
           FilesHelper.unzip(sorce, destDir);
       }
       catch(IOException ex)
       {
           logger.error("cant unzip file",ex);
            FilesHelper.remove(sorce);
            FilesHelper.remove(destDir);
            
            RuntimeException ex2 = new RuntimeException("Ошибка при разархивировании файлов новой версии: "+ex.getLocalizedMessage());
            ex2.addSuppressed(ex);
            throw ex2;
      }

    }
 
    private void backupCurrentFiles()  {
         
        final  File backupdir =  new File(Settings.base_dir,"backup");
        FilesHelper.cleanDir(backupdir,true);

        File exedir = backupdir.getParentFile();
        File[] all_files = exedir.listFiles((File pathname) -> (!pathname.equals(backupdir)));
        if(all_files==null){
            logger.warn("backup dir not found");
            return;
        }
        try {
            FilesHelper.copyFiles(all_files, backupdir);
        } catch (IOException ex) {
              logger.error("cant backup files",ex);
            RuntimeException ex2 = new RuntimeException("Ошибка при создании резервной копии: "+ex.getLocalizedMessage());
            ex2.addSuppressed(ex);
            throw ex2;
        }
         
    }


}
