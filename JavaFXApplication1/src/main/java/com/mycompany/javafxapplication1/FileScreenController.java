/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author ntu-user
 */


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import java.nio.file.*;




public class FileScreenController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    @FXML
    public TextArea fileTextArea;

    
    @FXML
    private Button saveFileButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button deleteFileButton;
    
    

    @FXML
    private void handleOpenFile() throws ClassNotFoundException {
        DB myObj = new DB();
        String filePath = myObj.GetFilePath(myObj.GetOwnerId(myObj.GetLoggedInUsername()), FileManagementController.GetNameOfFile());  // Change this to your file path

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) { // Read line by line
                fileTextArea.setText(line);
                
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    
    @FXML
    private void SaveFile()
    {
                
        FileManagementController.GetNameOfFile();
        String filePath = FileManagementController.GetNameOfFile();
        System.out.println(filePath);
        if (filePath == ""){
            filePath = FileManagementController.GetNameOfOpenFile();
        }
        String newContent = fileTextArea.getText();
        try (FileWriter writer = new FileWriter(filePath, false)) { // 'false' overwrites
            writer.write(newContent);
            System.out.println("File updated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }       

    }
    @FXML
    private void Back()
    {
        Stage secondaryStage = new Stage();
        Stage fileManagementStage = (Stage) backButton.getScene().getWindow();
        try {
        
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FileManagement.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("open file");
            secondaryStage.show();
            fileManagementStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void DeleteFile()
    {
        File file = new File(FileManagementController.GetNameOfFile());

        if (file.delete()) {
            System.out.println("File deleted successfully!");
        } else {
            System.out.println("File not found or could not be deleted.");
        }      
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        DB myObj = new DB();
        System.out.println();
        
        FileReader fileReader;
        try {
            
            fileReader = new FileReader(FileManagementController.GetNameOfOpenFile());
            System.out.println(FileManagementController.GetNameOfFile());
            var bufferReader = new BufferedReader(fileReader);

            String fileData = null;

            while ((fileData = bufferReader.readLine()) != null) {
                System.out.println(fileData);
                fileTextArea.setText(fileData);
            }
            // closing the BufferedReader object  
            bufferReader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileScreenController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileScreenController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        
 

        

 
        
        
    }    
    
}
