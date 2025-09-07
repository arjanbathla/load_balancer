package com.mycompany.javafxapplication1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Stage secondaryStage = new Stage();
        DB myObj = new DB();
        myObj.log("-------- Simple Tutorial on how to make JDBC connection to SQLite DB ------------");
        myObj.log("\n---------- Drop table ----------");
        try {
            myObj.delTable(myObj.getTableName());
            myObj.delTable(myObj.getTemporaryTableName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        myObj.log("\n---------- Create table ----------");
        try {
            myObj.createTable(myObj.getTableName());
            myObj.CreateTemporaryDatabase(myObj.getTemporaryTableName());
            DB.makeJDBCConnection();
            List<String> storageContainers = List.of("comp20081-files-container4", "comp20081-files-container3", "comp20081-files-container2", "comp20081-files-container1");
            LoadBalancer loadbalancer = new LoadBalancer(storageContainers);
            for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i + 1) + " directed to: " + loadbalancer.getNextStorageContainer());
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Primary View");
            secondaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}