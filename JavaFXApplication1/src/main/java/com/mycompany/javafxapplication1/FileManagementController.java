/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.*;
import java.util.Optional;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;



/**
 * FXML Controller class
 *
 * @author ntu-user
 */


public class FileManagementController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    @FXML
    private Button createFile;
    
    @FXML
    private Button secondaryButton;
    
    @FXML 
    private TextField fileName;
    
    @FXML
    private TextField fileNameTextField;
    
    @FXML 
    private Button openFileButton;
    
    public static FileManagementController instance;
    public FileManagementController() {
        instance = this;
    }
    
    public static String GetNameOfFile()
    {
        
        return instance.fileName.getText();
    }
    
    public static String GetNameOfOpenFile()
    {
        return instance.fileNameTextField.getText();
        
    }
    private void dialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait();
    }
    @FXML
    private void OpenFile() throws ClassNotFoundException
    {
        DB myObj = new DB();
        myObj.OpenUserFile(myObj.GetOwnerId(myObj.GetLoggedInUsername()), fileNameTextField.getText());
        if (myObj.OpenUserFile(myObj.GetOwnerId(myObj.GetLoggedInUsername()), fileNameTextField.getText()))
        {
                    
            Stage secondaryStage = new Stage();
            Stage fileManagementStage = (Stage) createFile.getScene().getWindow();
            try {

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("FileScreen.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 480);
                secondaryStage.setScene(scene);
                secondaryStage.setTitle("open file");
                secondaryStage.show();
                fileManagementStage.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else
        {
            return;
        }
        

    }
    


    
    @FXML
    private void CreateFile() throws ClassNotFoundException
    {
        Stage secondaryStage = new Stage();
        Stage fileManagementStage = (Stage) createFile.getScene().getWindow();
        try {
        
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FileScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("open file");
            secondaryStage.show();
            fileManagementStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
           
        
        String FileName = fileName.getText();
        if (FileName.isEmpty()) {
            System.out.println("file is empty name it something");
            return;
        }       
        
        File newFile = new File(FileName);
        try {
            if (newFile.createNewFile()) {
                
                System.out.println("File created: " + newFile.getName());
                GetNameOfFile();
                DB myObj = new DB();
                myObj.StoreFileInRemoteDatabase(fileName.getText(), newFile.getAbsolutePath(), myObj.GetOwnerId(myObj.GetLoggedInUsername()));
                
            } else {
                System.out.println("file already exists");
            }
        } catch (IOException e) {
            System.out.println("Error creating file: " + e.getMessage());
        }
        
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
