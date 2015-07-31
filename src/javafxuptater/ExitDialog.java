/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxuptater;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author kirio
 */
public class ExitDialog extends Stage {

    public ExitDialog(String text,boolean error) {
         createGui(text, error);
    }
   
 
    
     final void createGui(String text,boolean error){
        VBox vbox = new VBox();
        StackPane root = new StackPane(vbox);
        
       
        Scene scene = new Scene(root, 300, 250);
        setResizable(false);
        setScene(scene);
        Region top_space    = new Region();
        Region bot_space    = new Region();
        
        
    
        Label lab_text = new Label(text);
    
        Button but_ok = new Button("Ок");
        vbox.setAlignment(Pos.CENTER);
        
        VBox.setVgrow(top_space, Priority.ALWAYS);
        VBox.setVgrow(bot_space, Priority.ALWAYS);
        
        ToolBar bar = new ToolBar(but_ok);
       
        vbox.getChildren().addAll(top_space,lab_text,bot_space,bar);
    
        but_ok.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               hide();
            }
        });
    }
}
