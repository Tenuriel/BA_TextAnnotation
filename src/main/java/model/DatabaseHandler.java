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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Pontzen
 */
public class DatabaseHandler {

    private static DatabaseHandler database;
    public Connection con;

    private DatabaseHandler() {
        connect();
    }

    /**
     *
     * @return the instance of Databasehandler
     */
    public static DatabaseHandler getInstance() {
        if (database == null) {
            database = new DatabaseHandler();
        }
        return database;
    }

    /**
     * establish a connection.
     */
    public void connect() {
        String user = "Thargor";
        String pwd = "Magic1";
        String database = "anotator";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + "localhost" + ":"
                    + "3306" + "/" + database + "?" + "user=" + user + "&"
                    + "password=" + pwd);
            createTmpTables();
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * creates 2 temporary tables. 1 is for the entitys and the other to store results.
     */
    public void createTmpTables() {
        try {
            Statement st = con.createStatement();
            st.executeUpdate("Create Temporary Table a("
                    + "entity varchar(100) PRIMARY KEY);");
            st.executeUpdate("Create Temporary Table c(id INTEGER not NULL auto_increment PRIMARY KEY,"
                    + "uri VARCHAR(255));");
        } catch (SQLException ex) {
            System.out.println("creating temporary tables failed: " + ex.getMessage());
        }
    }
    public void fillA(ArrayList<String> words){
        try {
            Statement st = con.createStatement();
//            st.executeUpdate("TRUNCATE TABLE a");
            for(String s:words){
                st.executeUpdate("insert into a(entity) Values(\""+s+"\");");
            }
        } catch (SQLException ex) {
            System.out.println("filling table a failed: " + ex.getMessage());
        }
    }
    public ResultSet getUri() {
        ResultSet res=null;
        try {
            Statement st = con.createStatement();
            res = st.executeQuery("Select * from a");
            res.next();
            String s =res.getString(1);
            st.executeUpdate("Insert into c(uri) select maindata.uri "
                    + "From maindata use Index(one_col),a WHERE maindata.aTexte=a.entity AND maindata.tiefe=0;");
        } catch (SQLException ex) {
            System.out.println("Retrieving URIs failed: " + ex.getMessage());
        }
        return res;
    }
}
