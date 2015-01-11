/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package twentyquestions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nick
 */
public class TwentyQuestions {
        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {         
        TwentyQuestions q = new TwentyQuestions();
        q.start();
    }
        
    Hashtable<String,ObjectNode> objects;
    Hashtable<String, AttributeNode> attributes;
    
    ArrayList<AttributeNode> goodAttrib, badAttrib;
    ArrayList<ObjectNode> goodObj, badObj;
    
    Connection con; //connection to the database
    
    
    public TwentyQuestions() throws NullPointerException{
        objects = new Hashtable<>();
        attributes = new Hashtable<>();
        goodAttrib = new ArrayList<>();
        badAttrib = new ArrayList<>();
        goodObj = new ArrayList<>();
        badObj = new ArrayList<>();
        readFromDB();
        System.out.println(getClass());       
    }
    
    public void readFromDB(){
        final String driverName = "org.gjt.mm.mysql.Driver";
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TwentyQuestions.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        final String servername = "localhost:3306";
        final String db = "20QuestionsDB";
        final String url = "jdbc:mysql://" + servername + "/" + db + "?autoReconnect=true";
        System.out.println("Attempting to open database " + db + "...");
        con = null;
        Statement stmt = null;
        try{
            con = DriverManager.getConnection(url,"root","");
            stmt = con.createStatement();            
        }catch(Exception ex){
            System.err.println("Cannot access db -- make sure Database is configured properly");
            System.err.println("Message: " + ex.getMessage());
            System.exit(1);
        }
        System.out.println("CONNECTED");
        try {
            ResultSet rs = stmt.executeQuery("select Object, Attributes from " + db + ".Questions");           
            String currentObj = null;
            while(rs.next()){
                if(currentObj != null)
                    objects.get(currentObj).sort();
                currentObj = rs.getString("Object").trim();
                objects.put(currentObj, new ObjectNode(currentObj));
                goodObj.add(objects.get(currentObj));
                String[] attribs = rs.getString("Attributes").trim().split(",");
                for(String attribname : attribs){
                    attribname = attribname.trim();
                    AttributeNode attrib = getAttrib(attribname, objects.get(currentObj));
                    objects.get(currentObj).nodes.add(attrib);           
                    if(!goodAttrib.contains(attrib))
                        goodAttrib.add(attrib);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(TwentyQuestions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
                
    }
    
    public void updateDB(String objname){
        boolean isObject = false;
        objname = objname.trim();
        objname = objname.toLowerCase();
        char toCaps = objname.charAt(0);
        toCaps = Character.toUpperCase(toCaps);
        objname = objname.substring(1);
        objname = toCaps + objname;
        
            try {
                Statement stmt = con.createStatement();
                if(!objectExists(objname))
                    stmt.execute("INSERT INTO `Questions`(`Object`,`Attributes`) VALUES ('" + objname + "','_')");                
                ResultSet res = stmt.executeQuery(
                        "select * from `Questions` where Object = '" + objname + "'");
                String newOut = "";
                while(res.next()){
                    Scanner input = new Scanner(System.in);
                    String attribname = "";                    
                    for(int i = 0; i < goodAttrib.size();i++){
                        if(goodAttrib.get(i).applicable != AttributeNode.TRUE)
                            continue;
                        if(newOut.isEmpty()){
                            newOut += goodAttrib.get(i).name;
                        }else{
                            newOut += "," + goodAttrib.get(i).name;
                        }                    
                    }                    
                    do{
                        System.out.println("(END to stop) Describe the object (mind that this will follow \"it is\"):");
                        attribname = input.nextLine().trim();
                        if(attribname.equals("END"))
                            break;
                        if(newOut.isEmpty()){
                            newOut += attribname;
                        }else{
                            newOut += "," + attribname;
                        }                        
                    }while(!attribname.equals("END"));                    
                }
                res.close();
                stmt.execute("UPDATE `Questions` SET `Attributes`= '"+newOut+"' WHERE `Object` = '" + objname + "'");
            } catch (SQLException ex) {
                Logger.getLogger(TwentyQuestions.class.getName()).log(Level.SEVERE, null, ex);
            }        
    }
    
    boolean objectExists(String objname){
        for(ObjectNode obj : goodObj)
            if(obj.name.equals(objname))
                return true;
        for(ObjectNode obj : badObj)
            if(obj.name.equals(objname))
                return true;
        return false;
    }
    
    AttributeNode getAttrib(String attribName,ObjectNode object){                
        for(AttributeNode elem : goodAttrib){
            //System.out.println("\"" + elem.name + "\" VS " + attribName);
            if(attribName.equals(elem.name)){                
                elem.addOwner(object);
                return elem;
            }
        }
        AttributeNode re = new AttributeNode(attribName,object);
        return re;
    }
    
    public void start(){        
        //System.out.println(AttributeNode.count);
        Scanner input = new Scanner(System.in);
        boolean correct = false;
        ObjectNode winner = null;
        System.out.println("Think of an object.");
        int guesses = 0;
        do{            
            guess();              
            for(ObjectNode obj : goodObj)
                obj.update();
            for(ObjectNode obj : goodObj){
                if(obj.applicable == ObjectNode.TRUE){
                    correct = true;
                    winner = obj;
                    break;
                }
            }
            guesses++;
            System.out.println("\n");
        }while(!correct);
        System.out.println("I win! It's " + winner.name + " and it's ");
        winner.printAttribs();
    }
    
    void guess(){        
        System.out.println("THinking..");
        AttributeNode guessAttrib = null;
        for(int i = 0; i < goodAttrib.size();i++){
            AttributeNode attrib = goodAttrib.get(i);
            //System.out.println("Maybe " + attrib.name);
            if(attrib.applicable == attrib.FALSE){
                badAttrib.add(attrib);
                goodAttrib.remove(attrib);
                i--;
                continue;
            }            
            if(attrib.applicable == AttributeNode.TRUE){                
                continue;
            }
                
            if(guessAttrib == null && hasApplicableObjects(attrib)){
                guessAttrib = attrib;                
            }else{
                if(guessAttrib != null && attrib.getOwners() > guessAttrib.getOwners() && hasApplicableObjects(attrib)){
                    guessAttrib = attrib;                    
                }
            }
        }
        
        System.out.println("UMMM...");
        
        ObjectNode guessObject = null;
        for(int i = 0; i < goodObj.size();i++){
            ObjectNode obj = goodObj.get(i);
            if(obj.applicable == ObjectNode.FALSE){
                badObj.add(obj);
                goodObj.remove(obj);
                i--;
                continue;
            }
            //System.out.println("RAT FOR " + obj.name + " : " + obj.truthFalseRatio());
            if(obj.truthFalseRatio() >= 0.75f && guessObject == null){
                guessObject = obj;  
                guessAttrib = null;
            }else if(guessObject != null && obj.truthFalseRatio() > guessObject.truthFalseRatio()){
                guessObject = obj;
            }         
            else if(provenAttributes().length == goodAttrib.size() && guessObject == null){
                guessObject = obj;
                break;
            }
        }
        if(goodObj.size() == 0 ||(guessObject == null && guessAttrib == null)){
            System.out.println("...actually I have no idea what you're talking about");
            newEntry();
            System.exit(0);
        }
        String guess = "";
        if(guessAttrib != null)
            guess = guessAttrib.name;
        else
            guess = guessObject.name;
        System.out.print("(Y/N) is it " + guess + "?\t");
        Scanner input = new Scanner(System.in);
        String yn = "";
        while(!yn.equalsIgnoreCase("Y") && !yn.equalsIgnoreCase("N")){
           yn = input.nextLine().trim();              
           if(!yn.equalsIgnoreCase("Y") && !yn.equalsIgnoreCase("N"))
               System.out.print("Invalid Input\t");
        }
        if(yn.equalsIgnoreCase("Y")){
            if(guessAttrib != null){
                guessAttrib.applicable = AttributeNode.TRUE;
                for(int i = 0; i < goodObj.size();i++){
                    ObjectNode obj = goodObj.get(i);
                    if(!obj.containsAttrib(guessAttrib)){
                        obj.applicable = ObjectNode.FALSE;
                        goodObj.remove(i);
                        badObj.add(obj);
                        i--;
                        continue;
                    }
                }
            }else{
                guessObject.applicable = ObjectNode.TRUE;
            }
            System.out.println("Okay, so it's " + guess + ".");
        }else{
            if(guessAttrib != null){
                guessAttrib.applicable = AttributeNode.FALSE;
            }else{
                guessObject.applicable = ObjectNode.FALSE;
            }
            System.out.println("Okay, so it's not " + guess + ".");
        }        
    }
    
    boolean hasApplicableObjects(AttributeNode attrib){
        for (ObjectNode goodObj1 : goodObj) {
            if (goodObj1.containsAttrib(attrib) && goodObj1.applicable != ObjectNode.FALSE) {
                return true;
            }
        }
        return false;
    }
    
    void newEntry(){
        System.out.println("(END to quit) What is the answer?\t");
        Scanner input = new Scanner(System.in);
        String answer = input.nextLine();
        if(answer.equals("END"))
            return;
        updateDB(answer.trim());
    }
    
    AttributeNode[] provenAttributes(){
        ArrayList<AttributeNode> re = new ArrayList<>(goodAttrib);
        for(int i =0; i < re.size();i++){
            if(re.get(i).applicable == AttributeNode.FALSE ||
                    re.get(i).applicable == AttributeNode.VOID){
                re.remove(i);
                i--;             
            }
        }
        AttributeNode[] array = new AttributeNode[re.size()];
        return (AttributeNode[])re.toArray(array);
    }
        
    
}

