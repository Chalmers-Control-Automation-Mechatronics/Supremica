package org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn;

//class to describe a process
public class Pro extends PPNspec{
    
    protected String name = "";   
    protected String exp = "";
    
    protected static String[] op = null;     
    
    //start and end
    protected String start = "";
    protected String end = "";
    
    //**************************************************************************
    //constructor
    //**************************************************************************
    public Pro() {
        start = "Start";
        end = "End";
    }
    
    public Pro(String exp) {
        this();
        this.exp = exp;
    }
    //**************************************************************************
    //get
    //**************************************************************************
    public String getName() {
        return name;
    }
    public String getExp() {
        return exp;
    }
    public String[] getOp() {
        return op;
    }
    //**************************************************************************
    //set
    //**************************************************************************
    public void setName(String name){
        this.name = name;
    }
    public void setExp(String exp){
        this.exp = exp;
    }
    public void setOp(String[] op){
        this.op = op;
    }
    //**************************************************************************
    //booleans
    //**************************************************************************
    public boolean isOp(String test){
        for(int i =0; i < op.length; i++){
            if(op[i].equals(test)){
                return true;
            }
        }
        return false;
    }
}
