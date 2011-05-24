package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sourceforge.waters.model.module.IdentifierProxy;


public class ChanInfo{
    private int mSendCount;
    private int mRecCount;
    private final int mDataLength;
    private final int mChanLength;


    @SuppressWarnings("unused")
    private final String mName;
    private final ArrayList<String> type = new ArrayList<String>();
    /**
     * Collection of messages sent on the channel.
     */
    private final Collection<IdentifierProxy> mChannelData =
      new THashSet<IdentifierProxy>();

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
       mChannelData.add(ident);
    }
    public Collection<IdentifierProxy> receive(){
      return mChannelData;
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