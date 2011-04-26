package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChanInfo{
    private int send_count;
    private int rec_count;
    private final int datalength;
    @SuppressWarnings("unused")
    private final int typelength;
    @SuppressWarnings("unused")
    private List<String> type;
    private final List<List<String>> msg = new ArrayList<List<String>>();
    @SuppressWarnings("unused")
    private final Map<String,List<List<String>>> message =
      new HashMap<String,List<List<String>>>();
    @SuppressWarnings("unused")
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

    public List<List<String>> getValue(){
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