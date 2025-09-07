/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


import java.sql.PreparedStatement;


/**
 *
 * @author ntu-user
 */
public class DB {
    private String fileName = "jdbc:sqlite:comp20081.db";
    private int timeout = 30;
    private String dataBaseName = "COMP20081";
    private String dataBaseTableName = "Users";
    Connection connection = null;
    private Random random = new SecureRandom();
    private String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private int iterations = 10000;
    private int keylength = 256;
    private String saltValue;
    public String temporaryTableName = "temporaryDatabase";
    
    // remote database
    static public String RemoteDataBaseName="UserProfiles";
    static public String RemoteDataBaseTableName="users";
    static public String FileDatabaseTableName = "file_metadata";
    static public String userName="admin";
    static public String userPassword="7yR5W2WdTgxh";
    static Connection conn = null;
    static PreparedStatement prepareStat = null;
    
    

    /**
     * @brief constructor - generates the salt if it doesn't exists or load it from the file .salt
     */
    DB() {
        try {
            
            File fp = new File(".salt");
            if (!fp.exists()) {
                saltValue = this.getSaltvalue(30);
                FileWriter myWriter = new FileWriter(fp);
                myWriter.write(saltValue);
                myWriter.close();
            } else {
                Scanner myReader = new Scanner(fp);
                while (myReader.hasNextLine()) {
                    saltValue = myReader.nextLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
    /**
     * @brief create a new table
     * @param tableName name of type String
     */
    public void createTable(String tableName) throws ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("create table if not exists " + tableName + "(id integer primary key autoincrement, name string, password string)");

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        
    }
    
    public void CreateTemporaryDatabase(String tableName) throws ClassNotFoundException
    {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            
            statement.executeUpdate("create table if not exists " + tableName + "(id integer primary key autoincrement, name string)");
            

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
    
    public void AddToTemporaryDatabase(String user) throws ClassNotFoundException
    {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);

            statement.executeUpdate("insert into " + temporaryTableName + " (name) values('" + user + "')");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * @brief delete table
     * @param tableName of type String
     */
    public void delTable(String tableName) throws ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("drop table if exists " + tableName);
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * @brief add data to the database method
     * @param user name of type String
     * @param password of type String
     */
    public void addDataToDB(String user, String password) throws InvalidKeySpecException, ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
//            System.out.println("Adding User: " + user + ", Password: " + password);
            statement.executeUpdate("insert into " + dataBaseTableName + " (name, password) values('" + user + "','" + generateSecurePassword(password) + "')");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
        }
    }
    public String GetDataFromTemporaryTable()
    {
        String userName = "";
        try {
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select * from " + temporaryTableName);
            while (rs.next()) {
                // read the result set
                System.out.println("User name = " + rs.getString("name"));
                userName = rs.getString("name");
                
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return userName;
        
    }

    /**
     * @brief get data from the Database method
     * @retunr results as ResultSet
     */
    public ObservableList<User> getDataFromTable() throws ClassNotFoundException {
        ObservableList<User> result = FXCollections.observableArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select * from " + this.dataBaseTableName);
            while (rs.next()) {
                // read the result set
                result.add(new User(rs.getString("name"),rs.getString("password")));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return result;
    }
    
    public void ChangePassword(String user, String password) throws InvalidKeySpecException, ClassNotFoundException
    {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("Update " + dataBaseTableName + " Set password = '"+ generateSecurePassword(password) +"'Where id = 1");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
        }


     

    }

    /**
     * @brief decode password method
     * @param user name as type String
     * @param pass plain password of type String
     * @return true if the credentials are valid, otherwise false
     */
    public boolean validateUser(String user, String pass) throws InvalidKeySpecException, ClassNotFoundException {
        Boolean flag = false;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select name, password from " + this.dataBaseTableName);
            String inPass = generateSecurePassword(pass);
            // Let's iterate through the java ResultSet
            while (rs.next()) {
                if (user.equals(rs.getString("name")) && rs.getString("password").equals(inPass)) {
                    flag = true;
                    break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return flag;
    }

    public boolean validateRemoteUser(String user, String pass) throws InvalidKeySpecException, ClassNotFoundException {
        Boolean flag = false;
        try {
            // MySQL Select Query Tutorial
            String getQueryStatement = "SELECT * FROM "+RemoteDataBaseTableName;

            prepareStat = conn.prepareStatement(getQueryStatement);

            // Execute the Query, and get a java ResultSet
            ResultSet rs = prepareStat.executeQuery();
            String inPass = generateSecurePassword(pass);

            // Let's iterate through the java ResultSet
            while (rs.next()) {
                if (user.equals(rs.getString("username")) && rs.getString("password").equals(inPass)) {
                    flag = true;
                    break;
                }
            }
     

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return flag;
    }
    
    public String getSaltvalue(int length) {
        StringBuilder finalval = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            finalval.append(characters.charAt(random.nextInt(characters.length())));
        }

        return new String(finalval);
    }

    /* Method to generate the hash value */
    public byte[] hash(char[] password, byte[] salt) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keylength);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public String generateSecurePassword(String password) throws InvalidKeySpecException {
        String finalval = null;

        byte[] securePassword = hash(password.toCharArray(), saltValue.getBytes());

        finalval = Base64.getEncoder().encodeToString(securePassword);

        return finalval;
    }

    /**
     * @brief get table name
     * @return table name as String
     */
    public String getTableName() {
        return this.dataBaseTableName;
    }
    public String getTemporaryTableName() {
        return this.temporaryTableName;
    }
    
    //remote database functionality
    
    public static void makeJDBCConnection() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            log("Congrats - Seems your MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            log("Sorry, couldn't found JDBC driver. Make sure you have added JDBC Maven Dependency Correctly");
            e.printStackTrace();
            return;
        }

        try {
            // DriverManager: The basic service for managing a set of JDBC drivers.
            conn = DriverManager.getConnection("jdbc:mysql://lamp-server:3306/"+RemoteDataBaseName+"?useSSL=false", userName, userPassword);
            if (conn != null) {
                log("Connection Successful! Enjoy. Now it's time to push data");
            } else {
                log("Failed to make connection!");
            }
        } catch (SQLException e) {
            log("MySQL Connection Failed!");
            e.printStackTrace();
            return;
        }

    }
    
    public void addDataToRemoteDB(String username, String password, String typeOfUser) throws InvalidKeySpecException {
        try {
            // Specify the column names in the insert statement
            String insertQueryStatement = "INSERT INTO " + RemoteDataBaseTableName + " (username, password, typeOfUser) VALUES (?, ?, ?)";

            prepareStat = conn.prepareStatement(insertQueryStatement);
            prepareStat.setString(1, username);
            prepareStat.setString(2, generateSecurePassword(password));
            prepareStat.setString(3, typeOfUser);

            // Execute the insert SQL statement
            prepareStat.executeUpdate();
            log(username + " added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void getDataFromRemoteDB() {

        try {
            // MySQL Select Query Tutorial
            String getQueryStatement = "SELECT * FROM "+RemoteDataBaseTableName;

            prepareStat = conn.prepareStatement(getQueryStatement);

            // Execute the Query, and get a java ResultSet
            ResultSet rs = prepareStat.executeQuery();

            // Let's iterate through the java ResultSet
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                
                String typeOfUser = rs.getString("typeOfUser");

                // Simply Print the results
                System.out.format("%s, %s, %s\n", username, password, typeOfUser);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
       
    }


    

    /**
     * @brief print a message on screen method
     * @param message of type String
     */
    public static void log(String message) {
        System.out.println(message);

    }
    
    public void StoreFileInRemoteDatabase(String fileName, String filePath, int ownerId)
    {
        try {
            // Specify the column names in the insert statement
            String insertQueryStatement = "INSERT INTO " + FileDatabaseTableName + " (file_name, file_path, owner_id) VALUES (?, ?, ?)";

            prepareStat = conn.prepareStatement(insertQueryStatement);
            prepareStat.setString(1, fileName);
            prepareStat.setString(2, filePath);
            prepareStat.setInt(3, ownerId);

            // Execute the insert SQL statement
            prepareStat.executeUpdate();
            log(fileName + " added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String GetFileName(int userId, String fileName)
    {
        String query = "SELECT file_name FROM file_metadata WHERE file_name = ? AND owner_id = ?";
        String filePath = ""; 

        try (
            PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                filePath = resultSet.getString("file_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filePath;   
    }
    
    
    public int GetOwnerId(String username)
    {
        String query = "SELECT id FROM " + RemoteDataBaseTableName + " WHERE username = ?";
        int userId = -1; // Default value if user not found

        try (
            PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                userId = resultSet.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }
    
    public String GetLoggedInUsername() throws ClassNotFoundException
    {
        String username = "";
        try {
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select * from " + this.dataBaseTableName);
            while (rs.next()) {
                // read the result set
                username = rs.getString("name");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return username;
    }
    
    
    public static boolean OpenUserFile(int userId, String fileName)
    {
        boolean openfile = false;
        String query = "SELECT file_path FROM file_metadata WHERE file_name = ? AND owner_id = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                String filePath = resultSet.getString("file_path");
                openfile = true;
                
            } else {
                System.out.println("Error: File not found or you do not have permission to open it.");
                openfile = false;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return openfile;
        
    }
    
    public static String GetFilePath(int userId, String fileName){
        String query = "SELECT file_path FROM file_metadata WHERE file_name = ? AND owner_id = ?";;
        String filePath = ""; 

        try (
            PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fileName);
            pstmt.setInt(2, userId);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                filePath = resultSet.getString("file_path");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filePath;
    }
    
    public static void openFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) { // Read line by line
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

//    public static void main(String[] args) throws InvalidKeySpecException {
//        DB myObj = new DB();
//        myObj.log("-------- Simple Tutorial on how to make JDBC connection to SQLite DB ------------");
//        myObj.log("\n---------- Drop table ----------");
//        myObj.delTable(myObj.getTableName());
//        myObj.log("\n---------- Create table ----------");
//        myObj.createTable(myObj.getTableName());
//        myObj.log("\n---------- Adding Users ----------");
//        myObj.addDataToDB("ntu-user", "12z34");
//        myObj.addDataToDB("ntu-user2", "12yx4");
//        myObj.addDataToDB("ntu-user3", "a1234");
//        myObj.log("\n---------- get Data from the Table ----------");
//        myObj.getDataFromTable(myObj.getTableName());
//        myObj.log("\n---------- Validate users ----------");
//        String[] users = new String[]{"ntu-user", "ntu-user", "ntu-user1"};
//        String[] passwords = new String[]{"12z34", "1235", "1234"};
//        String[] messages = new String[]{"VALID user and password",
//            "VALID user and INVALID password", "INVALID user and VALID password"};
//
//        for (int i = 0; i < 3; i++) {
//            System.out.println("Testing " + messages[i]);
//            if (myObj.validateUser(users[i], passwords[i], myObj.getTableName())) {
//                myObj.log("++++++++++VALID credentials!++++++++++++");
//            } else {
//                myObj.log("----------INVALID credentials!----------");
//            }
//        }
//    }
}
