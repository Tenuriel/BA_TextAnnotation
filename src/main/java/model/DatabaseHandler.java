/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Pontzen
 */
public class DatabaseHandler {
    private static DatabaseHandler database;
    private Connection con;
    private DatabaseHandler(){
        connect();
    }
    /**
     * 
     * @return the instance of Databasehandler 
     */
    public static DatabaseHandler getInstance(){
        if(database==null){
            database=new DatabaseHandler();
        }
        return database;
    }
    /**
     * establish a connection.
     */
    public void connect(){
        String user = "Thargor";
        String pwd = "Magic1";
        String database = "anotator";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + "localhost" + ":"
                    + "3306" + "/" + database + "?" + "user=" + user + "&"
                    + "password=" + pwd);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    public void getUri(){
        try {
            Statement st=con.createStatement();
            ResultSet res= st.executeQuery("Insert into c");
        } catch (SQLException ex) {
            System.out.println("Retrieving URIs failed: "+ex.getMessage());
        }
    }
}
