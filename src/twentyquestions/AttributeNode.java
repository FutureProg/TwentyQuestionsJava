/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package twentyquestions;

import java.util.ArrayList;

/**
 *
 * @author Nick
 */
public class AttributeNode implements Comparable<AttributeNode>{
    public static int count = 0;
    public String name;
    public int applicable; //-1 = not set, 0 false, 1 true
    public static final int TRUE = 1, FALSE = 0, VOID = -1;
    public ArrayList<ObjectNode> owners;
    
    public AttributeNode(String name, ObjectNode owner){               
        owners = new ArrayList<>();
        owners.add(owner);
        this.name = name;
        this.applicable = VOID;
        count++;
    }

    @Override
    public int compareTo(AttributeNode o) {
        return name.compareTo(o.name);        
    }
    
    public void addOwner(ObjectNode owner){
        owners.add(owner);
    }
    
    public int getOwners(){
        return owners.size();
    }
    
}
