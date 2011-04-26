package net.sourceforge.waters.external.promela;


import java.util.*;

public class ChanInfo{
    private int send_count;
    private int rec_count;
    private final int datalength;
    private final int typelength;
    private ArrayList<String> type;
    private final ArrayList<ArrayList<String>> msg = new ArrayList<ArrayList<String>>();
    private final Hashtable<String,ArrayList<ArrayList<String>>> message= new Hashtable<String,ArrayList<ArrayList<String>>>();
    private final String name;
    public ChanInfo(final String n, final int type,final int data){
        send_count = 0;
        rec_count = 0;
        name = n;
        datalength = data;
        typelength = type;
    }

    public void storeMsg(final ArrayList<String> list){
        msg.add(list);

        send_count++;
    }

    public ArrayList getValue(){
        return msg;
    }
    public int getdataLength(){
        return datalength;
    }

    public int getsendnumber(){
        return send_count;
    }
    public void incsendnumber(){
        send_count++;
    }
    public void increcnumber(){
        rec_count++;
    }
    public int getrecnumber(){
        return rec_count;
    }
}