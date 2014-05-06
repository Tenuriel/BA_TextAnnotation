/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tim Pontzen
 */
public class DBpediaHandler {

    public DBpediaHandler() {
    }

    /**
     * creates a csv file from the files created ind createTF_IDF(). id;entity;word;tf*idf
     */
    public static void createTF_CSV() {
        try {
            PrintWriter pw = new PrintWriter("idf.csv", "UTF-8");
            Scanner scan = new Scanner(Paths.get("termCount_allDoc"));
            Scanner scan2 = new Scanner(Paths.get("doc_TermAndCount"));
            HashMap<String, Integer> tfALL = new HashMap<>();
            String[] line;
            String[] words;
            int id = 0;
            String tmp;
            int linecount = Integer.valueOf(scan.nextLine());
            while (scan.hasNextLine()) {
                tmp = scan.nextLine();
                line = tmp.split(";");
//                System.out.println(id);
//                id++;
                if (line[1].contains("\\")) {
                    id++;
                }
                tfALL.put(line[0], Integer.valueOf(line[1]));
            }
            while (scan2.hasNextLine()) {
                line = scan2.nextLine().split("\\|");
                words = line[1].split(";");
                for (int i = 0; i < words.length; i = i + 2) {
                    double score = Integer.valueOf(words[i + 1]) * Math.log(linecount / tfALL.get(words[i]));
                    pw.println(id + ";" + line[0] + ";" + words[i] + ";" + score);
                    id++;
                }
            }
            pw.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void createTF_IDF() {
        try (PrintWriter pw = new PrintWriter("termCount_allDoc", "UTF-8")) {
            PrintWriter pw2 = new PrintWriter("doc_TermAndCount", "UTF-8");
            Scanner scan = new Scanner(Paths.get("abstract_clean.txt"));
            String[] words;
            String[] line;
            TreeMap<String, Integer> tf = new TreeMap<>();
            TreeMap<String, Integer> wordDoc = new TreeMap<>();
            int counter = 0;
            int i = 0;
            while (scan.hasNextLine()) {
                counter++;
                line = scan.nextLine().split("\\|");
                words = line[1].split("[\"\\.@,; ]");
                for (String s : words) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    if (wordDoc.containsKey(s)) {
                        wordDoc.put(s, wordDoc.get(s) + 1);
                    } else {
                        wordDoc.put(s, 1);
                        if (tf.containsKey(s)) {
                            tf.put(s, tf.get(s) + 1);
                        } else {
                            tf.put(s, 1);
                        }
                    }
                }
                if (wordDoc.isEmpty()) {
                    continue;
                }
                pw2.print(line[0] + "|");
                for (Map.Entry<String, Integer> e : wordDoc.entrySet()) {
                    pw2.print(e.getKey() + ";" + e.getValue() + ";");
                }
                pw2.println();
                i++;
                if (i > 500_000) {
                    pw2.flush();
                    i = 0;
                }
                wordDoc.clear();
            }
            pw.println(counter);
            for (Map.Entry<String, Integer> e : tf.entrySet()) {
                pw.println(e.getKey() + ";" + e.getValue());
                i++;
                if (i > 500_000) {
                    pw.flush();
                    i = 0;
                }
            }
            pw.close();
            pw2.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static String cleanMappingProb(String filename) {
        PrintWriter pw = null;
        PrintWriter pw2 = null;
        TreeSet<String> set = new TreeSet<>();
        String[] line;
        try {
            Scanner scan = new Scanner(Paths.get(filename));
            pw = new PrintWriter("cleaned_properties.txt", "UTF-8");
            pw2 = new PrintWriter("cleaned_properties_neigborToEntity.txt", "UTF-8");
            while (true) {
                line = scan.nextLine().split(" ");
                if (line[2].contains("dbpedia.org/resource")) {
                    set.add(line[0] + "|" + line[2]);

                }
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Found eof");
            int counter = 0;
            for (String s : set) {
                counter++;
                pw.println(s);
                line = s.split("\\|");
                pw2.println(line[1] + "|" + line[0]);
                if (counter > 500_000) {
                    pw.flush();
                    pw2.flush();
                    counter = 0;
                }
            }
            pw.close();
            pw2.close();
            createEntityFile();
        } catch (IOException ex) {
            System.out.println("Error while cleaning mapping based proberties:" + ex.getMessage());
            return "Error while cleaning mapping based proberties:" + ex.getMessage();
        }
        return "done";
    }
    public static void createEntityFile(){
        try {
            Scanner scanner= new Scanner(Paths.get("./cleaned_properties.txt"));
            PrintWriter pw=new PrintWriter("entities.txt");
            TreeSet<String> set=new TreeSet<>();
            String[] line;
            while(scanner.hasNext()){
                line=scanner.nextLine().split("\\|");
                if(!set.contains(line[0])){
                    set.add(line[0]);
                }
                if(!set.contains(line[1])){
                    set.add(line[1]);
                }               
            }
            int counter=0;
            for(String s:set){
                pw.println(s);
                counter++;
                if(counter>500_000){
                    pw.flush();
                    counter=0;
                }
            }
            pw.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
    }
    /**
     * Convert a dbpedia abstracts file of type uri;category;abstract to uri;absctract. it is
     * ensured that only uris from entities.txt are in the outputfile.
     *
     * @param filename the name of the abstract file
     * @return status of operation
     */
    public static String cleanAbstracts(String filename) {
        try (PrintWriter pw = new PrintWriter("abstract_clean.txt", "UTF-8")) {
            Scanner scan = new Scanner(Paths.get("entities.txt"));
            Scanner scan2 = new Scanner(Paths.get(filename));
            scan2.nextLine();
            String[] tmp;
            HashMap<String, String> map = new HashMap<>();
            while (scan.hasNextLine()) {
                map.put(scan.nextLine(), "");
            }
            while (scan2.hasNextLine()) {
                tmp = scan2.nextLine().split(">");
                if (map.containsKey(tmp[0] + ">")) {
                    map.put(tmp[0] + ">", tmp[2]);
                }

            }
            TreeMap<String, String> set = new TreeMap<>(map);
            int i = 0;
            for (Map.Entry<String, String> e : set.entrySet()) {
                String value = e.getValue().trim();
                if (value.isEmpty()) {
                    continue;
                }
                pw.println(e.getKey() + "|" + value);
                i++;
                if (i > 500000) {
                    i = 0;
                    pw.flush();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "Error while cleaning Abstracts:"+ex.getMessage();
        }
        return "done";
    }

    /**
     * creates a csv with id;entity;anchor;depth
     */
    public static void createCSV() {
        try {
            PrintWriter pw = new PrintWriter("data.csv", "UTF-8");
            Scanner scan = new Scanner(Paths.get("combined.txt"));
            Scanner scan2 = new Scanner(Paths.get("entity_anchors.txt"));
            String[] line;//=scan.nextLine().split(" ");
            Map<String, String> anchorMap = new HashMap<>();
            int idCounter = 0;
            int counter = 0;
            String[] anchors;
            //load anchors into ram
            while (scan2.hasNext()) {
                line = scan2.nextLine().split("\\|");
                anchorMap.put(line[0], line[1]);
            }
            line = scan.nextLine().split("\\|");
            String previous = line[0];
            anchors = line[2].split(";");
            for (String s : anchors) {
                pw.println(idCounter + ";" + line[0] + ";" + s + ";1");
                idCounter++;
                counter++;
            }
            while (scan.hasNext()) {
                if (counter > 500_000) {
                    pw.flush();
                    counter = 0;
                }
                line = scan.nextLine().split("\\|");
                if (line.length == 0) {
                    System.out.println("found eof");
                    anchors = anchorMap.get(line[0]).split(";");
                    for (String s : anchors) {
                        pw.println(idCounter + ";" + line[0] + ";" + s + ";0");
                        idCounter++;
                        counter++;
                    }
                    break;
                }
                if (!line[0].equals(previous)) {
                    anchors = anchorMap.get(line[0]).split(";");
                    for (String s : anchors) {
                        pw.println(idCounter + ";" + line[0] + ";" + s + ";0");
                        idCounter++;
                        counter++;
                    }
                    previous = line[0];
                }
                anchors = line[2].split(";");
                for (String s : anchors) {
                    pw.println(idCounter + ";" + line[0] + ";" + s + ";1");
                    counter++;
                    idCounter++;
                }
            }
            pw.close();
        } catch (IOException ex) {
        }
    }

    /**
     * creates 2 files with entits;neighbor;anchor of neighbor. One with entity to neighbor and one
     * with neighbor to entity.
     * @return Status of operation
     */
    public static String createENAN() {
        try {
            Scanner scan = new Scanner(Paths.get("cleaned_properties.txt"));
            Scanner scan2 = new Scanner(Paths.get("cleaned_properties_neigborToEntity.txt"));
            Scanner anchors = new Scanner(Paths.get("entity_anchors.txt"));
            String[] split;
            Map<String, String> anchorMap = new HashMap<>();
            while (anchors.hasNext()) {
                split = anchors.nextLine().split("\\|");
                anchorMap.put(split[0], split[1]);
            }
            TreeSet<String> set = new TreeSet<>();
            int counter = 0;
            PrintWriter pw = new PrintWriter("entity_neighbor_anchorsN.txt", "UTF-8");
            while (scan.hasNext()) {
                split = scan.nextLine().split("\\|");
                if (anchorMap.containsKey(split[0]) && anchorMap.containsKey(split[1])) {
                    set.add(split[1] + "|" + split[0] + "|" + anchorMap.get(split[0]));
                }
            }
            for (String s : set) {
                if (counter > 500_000) {
                    counter = 0;
                    pw.flush();
                }
                pw.println(s);
                counter++;
            }
            pw.close();
            set.clear();
            pw = new PrintWriter("neighbor_entity_anchorsE.txt", "UTF-8");
            while (scan2.hasNext()) {
                split = scan2.nextLine().split("\\|");
                if (anchorMap.containsKey(split[0]) && anchorMap.containsKey(split[1])) {
                    set.add(split[1] + "|" + split[0] + "|" + anchorMap.get(split[0]));
                }
            }
            for (String s : set) {
                if (counter > 500_000) {
                    counter = 0;
                    pw.flush();
                }
                pw.println(s);
                counter++;
            }
            pw.close();
            set.clear();

            ArrayList<Scanner> scanners = new ArrayList<>();
            scanners.add(new Scanner(Paths.get("entity_neighbor_anchorsN.txt")));
            scanners.add(new Scanner(Paths.get("neighbor_entity_anchorsE.txt")));
            merge(scanners);
        } catch (IOException ex) {
            System.out.println("Error while creating ENAN:" + ex.getMessage());
            return "Error while creating ENAN:" + ex.getMessage();
        }
        return "done";
    }

    /**
     * creates a file with ID;entity;anchors fields.
     */
    public static void setIDs() {
        Scanner entities;
        Scanner anchorScan;
        try {
            entities = new Scanner(Paths.get("entities.txt"));

            anchorScan = new Scanner(Paths.get("anchors.txt"));
            ArrayList<String> anchors;
            StringBuilder builder;
            String[] anchor;
            String tmp;
            Map<String, ArrayList<String>> anchorMap = new HashMap<>();
            while (anchorScan.hasNext()) {
                anchor = anchorScan.nextLine().split("\\|");
                if (anchorMap.containsKey(anchor[0])) {
                    anchorMap.get(anchor[0]).add(anchor[1]);
                } else {
                    anchors = new ArrayList<>();
                    anchors.add(anchor[1]);
                    anchorMap.put(anchor[0], anchors);
                }
            }
            PrintWriter pw = new PrintWriter("entity_anchors.txt", "UTF-8");
            int counter = 0;
            int i = 0;
            while (entities.hasNext()) {
                tmp = entities.nextLine();
                if (anchorMap.containsKey(tmp)) {
                    anchors = anchorMap.get(tmp);
                    builder = new StringBuilder(tmp);
                    builder.append("|");
                    for (String s : anchors) {
                        builder.append(s);
                        builder.append(";");
                    }
                    pw.println(builder.substring(0, builder.length() - 1));
                    if (counter++ > 500_000) {
                        pw.flush();
                        counter = 0;
                    }
                }
            }
            pw.close();
        } catch (IOException ex) {
            System.out.println("Error while setting ids;");
        }
    }

    /**
     * merges files with the same seperator for values.
     *
     * @param scanners The scanners with to be merged files as input
     * @throws IOException
     */
    public static void merge(ArrayList<Scanner> scanners) throws IOException {
        if (scanners.size() < 2) {
            System.out.println("no need for merging, to few files");
            return;
        }
        PrintWriter pw = new PrintWriter("combined.txt", "UTF-8");
        int counter = 0;
        boolean finished = false;
        ArrayList<Scanner> toBeRemoved = new ArrayList<>();
        ArrayList<String> input = new ArrayList<>();
        for (Scanner scan : scanners) {
            input.add(scan.nextLine());
        }
        String tmp = input.get(0);
        int indi = 0;
        while (!finished) {
            for (Scanner scan : scanners) {
                if (!scan.hasNext()) {
                    String s = input.remove(scanners.indexOf(scan));
                    input.add(s);
                    if (s.equals(tmp)) {
                        indi = input.size() - 1;
                    }
                    scan.close();
                    toBeRemoved.add(scan);
                }
            }
            if (!toBeRemoved.isEmpty()) {
                scanners.removeAll(toBeRemoved);
                if (scanners.isEmpty()) {
                    break;
                }
                toBeRemoved.clear();
            }
            for (String s : input) {
                if (s.compareTo(tmp) < 0) {
                    tmp = s;
                    indi = input.indexOf(s);
                }
            }
            pw.println(tmp);
            counter++;
            if (counter > 500_000) {
                pw.flush();
                counter = 0;
            }
            if (indi < scanners.size()) {
                input.set(indi, scanners.get(indi).nextLine());
                tmp = input.get(indi);
            } else {
                input.remove(tmp);
                tmp = input.get(input.size() - 1);
                indi = input.size() - 1;
            }
        }
        pw.close();
    }

}
