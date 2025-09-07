/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author ntu-user
 */




public class ChangePasswordController extends PrimaryController{

    /**
     * Initializes the controller class.
     */
    
    @FXML 
    private Button UpdatePasswordButton;
   

    @FXML
    private Button returnToUserPageButton;
    
    @FXML
    private TextField oldPasswordTextBox;
    
    @FXML
    private TextField newPasswordTextBox;
    

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
    private void UpdatePassword()
    {
        
        
        
        try {
            DB myObj = new DB();
            String userName = myObj.GetDataFromTemporaryTable();
            
            String[] credentials = {userName, oldPasswordTextBox.getText()};
            if(myObj.validateUser(userName, oldPasswordTextBox.getText()))
            {
                myObj.ChangePassword(userName, newPasswordTextBox.getText());
                
            }else
            {
                dialogue("Password is incorrect","Please try again!");               
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ReturnToUserPage()
    {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) returnToUserPageButton.getScene().getWindow();
        try {
            
        
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Show users");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
