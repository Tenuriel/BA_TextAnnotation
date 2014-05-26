/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author Tim Pontzen
 */
public class KORE_Formater {

    public KORE_Formater() {
        try {
            Scanner scan = new Scanner(Paths.get("./AIDA.tsv"));
            PrintWriter pw = new PrintWriter("kore.txt");
            PrintWriter pw2 = new PrintWriter("entity.txt");
            String[] tmp;
            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            int entity=0;
            int line=1;
            while (scan.hasNext()) {
                tmp = scan.nextLine().split("\t");
                if (line==50){
                    line=line;
                }
                if(tmp[0].isEmpty()){
                    pw.println(sb.toString());
                    sb=new StringBuilder();
                    pw2.println(line);
                    pw2.println(sb2.toString());
                    line++;
                    sb2=new StringBuilder();
                }else if(!tmp[0].startsWith("-")){
                    sb.append(tmp[0]);
                    sb.append(" ");
                    
                    if(tmp.length>=4 && tmp[1].equals("B")){
                        sb2.append(tmp[2]);
                        sb2.append(" ");
                        sb2.append(tmp[3]);
                        sb2.append("\n");
                        entity++;
                    }
                }
            }
            pw.print(entity);
            pw.close();
            pw2.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
