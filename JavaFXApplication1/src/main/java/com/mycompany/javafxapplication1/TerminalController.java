package com.mycompany.javafxapplication1;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class TerminalController {

    @FXML
    private TextArea terminalOutput;

    @FXML
    private TextField commandInput;
    
    @FXML
    private Button backButton;

    // The current working directory
    private File currentDir;

    public void initialize() {
        // Initialize current working directory to the directory from which the app was launched
        currentDir = new File(System.getProperty("user.dir"));
        printPrompt();

        // Process command when the user presses Enter in the text field
        commandInput.setOnAction(e -> {
            String commandLine = commandInput.getText();
            terminalOutput.appendText(commandLine + "\n");
            processCommand(commandLine);
            commandInput.clear();
            printPrompt();
        });
    }

    // Print a simple prompt showing username and current directory
    private void printPrompt() {
        String prompt = System.getProperty("user.name") + "@terminal:" + currentDir.getAbsolutePath() + "$ ";
        terminalOutput.appendText(prompt);
    }

    // Parse and execute the command
    private void processCommand(String commandLine) {
        if (commandLine.trim().isEmpty()) return;
        String[] parts = commandLine.split("\\s+");
        String command = parts[0];

        try {
            switch (command) {
                case "ls":
                    ls();
                    break;
                case "mkdir":
                    if (parts.length < 2) {
                        terminalOutput.appendText("Usage: mkdir <directory_name>\n");
                    } else {
                        mkdir(parts[1]);
                    }
                    break;
                case "mv":
                    if (parts.length < 3) {
                        terminalOutput.appendText("Usage: mv <source> <destination>\n");
                    } else {
                        mv(parts[1], parts[2]);
                    }
                    break;
                case "cp":
                    if (parts.length < 3) {
                        terminalOutput.appendText("Usage: cp <source> <destination>\n");
                    } else {
                        cp(parts[1], parts[2]);
                    }
                    break;
                case "ps":
                    ps();
                    break;
                case "whoami":
                    whoami();
                    break;
                case "tree":
                    tree();
                    break;
                case "nano":
                    if (parts.length < 2) {
                        terminalOutput.appendText("Usage: nano <filename>\n");
                    } else {
                        nano(parts[1]);
                    }
                    break;
                default:
                    terminalOutput.appendText("Command not found: " + command + "\n");
                    break;
            }
        } catch (Exception e) {
            terminalOutput.appendText("Error: " + e.getMessage() + "\n");
        }
    }

    // List the files and directories in the current directory
    private void ls() {
        File[] files = currentDir.listFiles();
        if (files != null) {
            String output = Arrays.stream(files)
                    .map(file -> file.getName() + (file.isDirectory() ? "/" : ""))
                    .collect(Collectors.joining("\n"));
            terminalOutput.appendText(output + "\n");
        }
    }

    // Create a new directory in the current directory
    private void mkdir(String dirName) {
        File newDir = new File(currentDir, dirName);
        if (newDir.mkdir()) {
            terminalOutput.appendText("Directory created: " + newDir.getAbsolutePath() + "\n");
        } else {
            terminalOutput.appendText("Failed to create directory: " + dirName + "\n");
        }
    }

    // Move (rename) a file or directory
    private void mv(String sourceName, String destName) {
        File source = new File(currentDir, sourceName);
        File dest = new File(currentDir, destName);
        if (!source.exists()) {
            terminalOutput.appendText("Source file/directory does not exist: " + sourceName + "\n");
            return;
        }
        if (source.renameTo(dest)) {
            terminalOutput.appendText("Moved " + sourceName + " to " + destName + "\n");
        } else {
            terminalOutput.appendText("Failed to move " + sourceName + "\n");
        }
    }

    // Copy a file from source to destination (directories not supported in this simple example)
    private void cp(String sourceName, String destName) throws IOException {
        File source = new File(currentDir, sourceName);
        File dest = new File(currentDir, destName);
        if (!source.exists()) {
            terminalOutput.appendText("Source file/directory does not exist: " + sourceName + "\n");
            return;
        }
        if (source.isDirectory()) {
            terminalOutput.appendText("Copying directories is not supported.\n");
            return;
        }
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        terminalOutput.appendText("Copied " + sourceName + " to " + destName + "\n");
    }

    // Simulate the process list command with dummy data
    private void ps() {
        terminalOutput.appendText("PID\tCMD\n");
        terminalOutput.appendText("1\tinit\n");
        terminalOutput.appendText("1234\tjava\n");
        terminalOutput.appendText("5678\tbash\n");
    }

    // Display the current user name
    private void whoami() {
        terminalOutput.appendText(System.getProperty("user.name") + "\n");
    }

    // Recursively display the directory tree starting at the current directory
    private void tree() {
        String treeStr = buildTree(currentDir, "");
        terminalOutput.appendText(treeStr + "\n");
    }

    // Helper method to build a tree string
    private String buildTree(File dir, String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(dir.getName()).append("/\n");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    sb.append(buildTree(file, prefix + "    "));
                } else {
                    sb.append(prefix).append("    ").append(file.getName()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    // Emulate a simple nano text editor by opening a new window with a TextArea
    private void nano(String filename) {
        File file = new File(currentDir, filename);
        String content = "";
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                terminalOutput.appendText("Error reading file: " + e.getMessage() + "\n");
                return;
            }
        }
        Stage editorStage = new Stage();
        editorStage.setTitle("nano - " + filename);

        TextArea editorArea = new TextArea(content);
        Button saveButton = new Button("Save");
        Label statusLabel = new Label();

        saveButton.setOnAction(e -> {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(editorArea.getText());
                statusLabel.setText("File saved successfully.");
            } catch (IOException ex) {
                statusLabel.setText("Error saving file: " + ex.getMessage());
            }
        });

        VBox vbox = new VBox(10, editorArea, saveButton, statusLabel);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 600, 400);
        editorStage.setScene(scene);
        editorStage.show();
        

    }
    @FXML
    private void Back()
    {
        Stage secondaryStage = new Stage();
        Stage fileManagementStage = (Stage) backButton.getScene().getWindow();
        try {
        
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("secondary");
            secondaryStage.show();
            fileManagementStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
