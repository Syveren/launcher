/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxuptater;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static javafxuptater.Main.updater;

/**
 *
 * @author kirio
 */
public class UpdateDialog extends  Stage{

    
 
    public UpdateDialog( Updater updater) {
      
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_CENTER);
        StackPane root = new StackPane(vbox );
        initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(root, 600, 450);
       
        setTitle("Обновление");
        final Label lab_status = new Label();
        lab_status.setTextAlignment(TextAlignment.CENTER);
     
        String about = updater.getAboutUpdate();
      
        if(!about.isEmpty())
        {    
       
            final TextArea area_about = new TextArea();
              
            area_about.setText(about);
            area_about.setEditable(false);
             TitledPane aboutPane = new TitledPane("Информация об обновлении",area_about);
          
 
             vbox.getChildren().addAll( aboutPane);
        }
      
        vbox.getChildren().addAll( lab_status);
       
        setScene(scene);
       
        final Task<Integer> task_updater = new Task<Integer>() {

            @Override
            protected Integer call() throws Exception {
                if(!updater.checkCompletLastUpdate())
                {
                    updater.completeLastUpdate();
                   
                }
                return updater.update(true);
                
            }
        };
        
        task_updater.setOnSucceeded(new EventHandler() {

            @Override
            public void handle(Event event) {
               final int result  = task_updater.getValue();
              

               new Thread(new Task() {
                   @Override
                   protected Object call() throws Exception {
                         Platform.runLater(() -> {
                          if(result==Updater.RESULT_NEED_REBOOT)
                              lab_status.setText("Обновление завершено."
                                      + "\nСейчас будет перезагрузка компьютера!");
                          else 
                              lab_status.setText("Обновление завершено."
                                      + "\nСейчас будет запущена основная программа!");

                      }); 
                       
                       try {
                                Thread.sleep(10_000);
                            } catch (InterruptedException ex) {
                               
                            }
                        Platform.runLater(() -> {
                            
                              hide();

                      }); 
                      return null;
                       
                   }
               }).start();
              
              
            }
        });
        
        task_updater.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                 System.out.println("Error: "+task_updater.getException().getLocalizedMessage());
                final  String error = task_updater.getException().getLocalizedMessage();
            
                  
                  
                  new Thread(new Task() {
                   @Override
                   protected Object call() throws Exception {
                       
                           Platform.runLater(() -> {
                              lab_status.setText("Ошибка обновления."
                                      +"\n"+error
                                    + "\nСейчас будет запущена основная программа!(10 сек)");
                            }); 
                            try {
                                 Thread.sleep(10_000);
                             } catch (InterruptedException ex) {      }
                           
                           Platform.runLater(() -> {
                            
                             
                                hide();
                            
                        }); 
                      
                       
                        //Platform.exit();
                       return null;
                       
                   }
               }).start();
            }
        });
        
        updater.handler = (String state) -> {
                  Platform.runLater(() -> {
                        //System.out.println("progress: "+(state*100/6));
                        lab_status.setText(state);
                    
                  });
        };
           
        
       
          thread = new Thread(task_updater);
       
    
    }
Thread thread;
    @Override
    public void showAndWait() {
         thread.start();
        super.showAndWait(); //To change body of generated methods, choose Tools | Templates.
    }
    

 
  
    
}
