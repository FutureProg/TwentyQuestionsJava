/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package twentyquestions;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Nick
 */

class ObjectNode implements Comparable<ObjectNode>{
    
    public int applicable; //-1 = not set, 0 false, 1 true
    public static final int TRUE = 1, FALSE = 0, VOID = -1;
    public ArrayList<AttributeNode> nodes;
    public String name;
    
    public ObjectNode(String name){
        this.name = name;
        nodes = new ArrayList<>();
        applicable = VOID;
    }   

    @Override
    public int compareTo(ObjectNode o) {
        if(o.name.equals(this.name))
            return 0;
        else
            return 1;
    }
    
    public String printAttribs(){
        ArrayList<String> re = new ArrayList<>();
        for(int i = 0; i < nodes.size();i++){
            re.add(nodes.get(i).name);
        }
        System.out.println(re.toString());
        return re.toString();
    }
    
    public void sort(){
        Collections.sort(nodes);
    }
    
    public float truthFalseRatio(){
        float right = 0;
        float total = nodes.size();
        for(AttributeNode node : nodes){
            if(node.applicable == AttributeNode.TRUE)
                right++;
        }
        return (float)(right/total);
    }
    
    public boolean containsAttrib(AttributeNode attrib){
        return nodes.contains(attrib);
    }
    
    public void update(){
        for(AttributeNode attrib: nodes){
            if(attrib.applicable == AttributeNode.FALSE)
                applicable = FALSE;
        }
    }
    
}