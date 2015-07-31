/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxuptater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kirio
 */
public interface FilesHelper {
          final static org.slf4j.Logger logger = LoggerFactory.getLogger(FilesHelper.class);
    public static void remove(File f)
    {
        logger.debug("remove file {}",f.getAbsoluteFile());
        if(!f.exists())
            return;
        if(f.isDirectory())
        {
            for(File ff:f.listFiles())
            {
                remove(ff);
            }
        }
        if(!f.delete()) {
            System.out.println("cant delete FILE :"+f.getAbsolutePath());
        }
     

    }
    static public  void cleanDir(File dir,boolean create_if_unexists){
        if(!dir.exists())
        {   
            if(create_if_unexists)
                dir.mkdir();
            return;
        }
        for(File file:dir.listFiles())
            remove(file);
    }
   
    static public  void copy(String srFile, String dtFile) throws FileNotFoundException, IOException{
        copy(new File(srFile), new File(dtFile));

     }
    static public void copy(File srFile, File dtFile) throws IOException {

 
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(srFile));
            out = new BufferedOutputStream(new FileOutputStream(dtFile));
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0){
              out.write(buf, 0, len);
            }
        }
        finally{
            if(in!=null)
               in.close();
            if(out!=null)
                out.close();
        }
     

     }
   
    /*static public void copyChildFiles(File parent,String dir) throws IOException 
    {

    
         File[] files = parent.listFiles();
         for(File file:files)
         {

             if(file.isDirectory())
             {
                 new File(dir+"/"+file.getName()).mkdir();
                 copyChildFiles(file,dir+"/"+file.getName());
             }
             else
             {
                 copy(file.getAbsolutePath(),dir+"/"+file.getName());
             }
         }

    }*/
    static public void  copyChildFiles(File parent,File dstdir) throws IOException 
    {
        
         File[] files = parent.listFiles();
         for(File file:files)
         {

             logger.debug("copy file {} to dir {}",file.getAbsolutePath(),dstdir.getAbsolutePath());
             if(file.isDirectory())
             {
                 File newdir = new File(dstdir,file.getName());
                 newdir.mkdir();
                 copyChildFiles(file,newdir);
             }
             else
             {
                 copy(file,new File(dstdir,file.getName()));
             }
         }

    }
    
  
     static public void copyFiles(File[] files ,File dir) throws IOException
    {

         if(!dir.exists())
            dir.mkdirs();
         
         for(File file:files)
         {  
             logger.debug("copy file {} to dir {}",file.getAbsolutePath(),dir.getAbsolutePath());
             if(file.isDirectory())
             {
                 File newdir = new File(dir,file.getName());
                 newdir.mkdir();
                 copyChildFiles(file, newdir);
             }
             else
             {
                 copy(file,new File(dir,file.getName()));
             }
         }

     }

  static public void unzip(File sorce,File destDir) throws IOException 
  {
       final int BUFFER = 4096;
       BufferedOutputStream dest = null;
       BufferedInputStream is  = null;
       ZipFile zipfile = null;
       try {

        zipfile = new ZipFile(sorce);
       
        Enumeration e = zipfile.entries();
        while(e.hasMoreElements()) {

           ZipEntry entry = (ZipEntry) e.nextElement();

           if(entry.isDirectory()){
               
                (new File(destDir,entry.getName())).mkdir();
           }
           else{

                File file = new File(destDir,entry.getName());
                file.createNewFile();

                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new  FileOutputStream(file);
                dest = new  BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER))!= -1) {
                  dest.write(data, 0, count);
                }
               
                dest.flush();
                is.close();
                dest.close(); 
           }
 
        }
       }
       finally{
           if(zipfile!=null)
                zipfile.close(); 
   
       }
 
 
    }
}
