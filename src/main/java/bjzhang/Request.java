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
public class Request {

    String uuid;
    
    public String getUuid(){
        return uuid;
    }
    public void setUuid(String uuid){
        this.uuid = uuid;
    }
    public Request(String uuid){
        this.uuid = uuid;
    }
    public Request()
    {
        
    }
}