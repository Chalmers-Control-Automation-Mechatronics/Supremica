package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class ChanInfo{
    private int mSendCount;
    private int mRecCount;
    private final int mDataLength;
    @SuppressWarnings("unused")
    private final int mTypeLength;
    @SuppressWarnings("unused")
    private List<String> type;
    private final List<List<String>> mMsg = new ArrayList<List<String>>();
    @SuppressWarnings("unused")
    private final Map<String,List<List<String>>> message =
      new Hashtable<String,List<List<String>>>();
    @SuppressWarnings("unused")
    private final String mName;

    public ChanInfo(final String n, final int type,final int data){
        mSendCount = 0;
        mRecCount = 0;
        mName = n;
        mDataLength = data;
        mTypeLength = type;
    }

    public ChanInfo()
    {
      mTypeLength=0;
      mName="";
      mSendCount=0;
      mRecCount=0;
      mDataLength=0;
      // TODO Auto-generated constructor stub
    }

    public void storeMsg(final ArrayList<String> list){
        mMsg.add(list);

        mSendCount++;
    }

    public List<List<String>> getValue(){
        return mMsg;
    }
    public int getDataLength(){
        return mDataLength;
    }

    public int getSendnumber(){
        return mSendCount;
    }
    public void incSendnumber(){
        mSendCount++;
    }
    public void incRecnumber(){
        mRecCount++;
    }
    public int getRecnumber(){
        return mRecCount;
    }
}