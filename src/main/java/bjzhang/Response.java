/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bjzhang;

/**
 *
 * @author zumboboga
 */
public class Response {
    String name;
    int age;
    String base;
    
    public void setBase64(String Base64){
        this.base = Base64;
    }
    
    public String getBase64(){
        return base;
    }
    
    public void setName(String x){
        this.name = x;
    }
    
    public String getName(){
        return name;
    }
    
    public void setAge(int y){
        this.age = y;
    }
    
    public int getAge(){
        return age;
    }
    
    public Response(String x, int y, String z){
        this.name = x; 
        this.age = y;
        this.base = z;
    }
    
    public Response(){
        
    }
}


