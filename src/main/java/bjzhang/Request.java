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
    String name;
    int calcs;
    int sleep;
    int loops;
    String base;
    
    public String getBase()
    {
        return base;
    }
    public void setBase(String Base64)
    {
        this.base = Base64;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public int getCalcs()
    {
        return calcs;
    }
    public void setCalcs(int calcs)
    {
        this.calcs = calcs;
    }
    public int getSleep()
    {
        return sleep;
    }
    public void setSleep(int sleep)
    {
        this.sleep = sleep;
    }
    public int getLoops()
    {
        return loops;
    }
    public void setLoops(int loops)
    {
        this.loops = loops;
    }
    public Request(String name, String Base64)
    {
        this.name = name;
        this.base = Base64;
    }
    public Request()
    {
        
    }
}