/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Pontzen
 */
public class main {
    public static void main( String[] args )
    {
//        try {
//            Scanner scanner=new Scanner(Paths.get("../BA_TextAnnotation/entitys.txt"));
//            Scanner scanner2=new Scanner(Paths.get("entitys.txt"));
//            int c=0;
//            String s1;
//            String s2;
//            while(scanner2.hasNext()){
//                c++;
//                s1=scanner.nextLine();
//                s2=scanner2.nextLine();
//                if(!s1.equals(s2)){
//                    System.out.println(c);
//                    break;
//                }
//            }
//        } catch (IOException ex) {
//            
//        }
//        new DBpediaHandler();
        new GUI();
    }
}
