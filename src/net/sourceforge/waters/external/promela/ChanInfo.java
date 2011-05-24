package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.module.IdentifierProxy;


public class ChanInfo{
    private int mSendCount;
    private int mRecCount;
    private final int mDataLength;
    @SuppressWarnings("unused")
    private final int mChanLength;
    private final List<List<String>> mMsg = new ArrayList<List<String>>();
    @SuppressWarnings("unused")
    private final Map<String,List<List<String>>> message =
      new Hashtable<String,List<List<String>>>();
    @SuppressWarnings("unused")
    private final String mName;
    private final ArrayList<String> type = new ArrayList<String>();
    private final Collection<IdentifierProxy> chanData = new THashSet<IdentifierProxy>();
    public ChanInfo(final String n, final int typeL,final int dataL,final List<String> ty){
        mSendCount = 0;
        mRecCount = 0;
        mName = n;
        mDataLength = dataL;
        mChanLength = typeL;
        for(final String value: ty){
           type.add(value);
        }
    }

    public ChanInfo()
    {
      mChanLength=0;
      mName="";
      mSendCount=0;
      mRecCount=0;
      mDataLength=0;
    }
    public void send(final IdentifierProxy ident){
       chanData.add(ident);
    }
    public Collection<IdentifierProxy> receive(){
      return chanData;
    }
    public void storeMsg(final ArrayList<String> list){
        mMsg.add(list);
        mSendCount++;
    }

    public List<List<String>> getValue(){
        return mMsg;
    }
    public int getChanLength(){
      return mChanLength;
    }
    public int getDataLength(){
        return mDataLength;
    }
    public ArrayList<String> getType(){
        return type;
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