package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


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
    /**
     * Collection of messages going to be received on the channel
     */
    private final Collection<IdentifierProxy> mRecData =
      new THashSet<IdentifierProxy>();
    private final Collection<Collection<SimpleExpressionProxy>> mSendData =
      new ArrayList<Collection<SimpleExpressionProxy>>();
    private final Collection<Collection<SimpleExpressionProxy>> mReceiveData =
      new ArrayList<Collection<SimpleExpressionProxy>>();

    /*
     *
     */
    private final List<Message> mMessages;
    public ChanInfo(final String n, final int typeL,final int dataL,final List<String> ty){
        mSendCount = 0;
        mRecCount = 0;
        mName = n;
        mDataLength = dataL;
        mChanLength = typeL;
        for(final String value: ty){
           type.add(value);
        }
        mMessages = new ArrayList<Message>();
    }

    public ChanInfo()
    {
      mChanLength=0;
      mName="";
      mSendCount=0;
      mRecCount=0;
      mDataLength=0;
      mMessages = new ArrayList<Message>();
    }
    public void addMessages(final Message msg){
      if(!mMessages.contains(msg)){
        mMessages.add(msg);
      }else{
        if(msg.hasRecipients()){
          for(final String s: msg.getRecipients()){
            if(!mMessages.get((mMessages.indexOf(msg))).getRecipients().contains(s)){
              mMessages.get((mMessages.indexOf(msg))).addRecipients(s);
            }
          }
        }
        if(msg.hasSenders()){
          for(final String s: msg.getSenders()){
            if(!mMessages.get((mMessages.indexOf(msg))).getSenders().contains(s)){
              mMessages.get((mMessages.indexOf(msg))).addSenders(s);
            }
           }
        }
      }
    }
    public List<Message> getMessages(){
      return mMessages;
    }
    public void send(final Collection<SimpleExpressionProxy> ident){
       mSendData.add(ident);
    }
    public void saveReceive(final Collection<SimpleExpressionProxy> c){
      mReceiveData.add(c);
    }
    //store receive event
    public void addReceiveData(final IdentifierProxy ident){
      mRecData.add(ident);
    }
    //store send event
    public void addChannelData(final IdentifierProxy ident){
      mChannelData.add(ident);
    }
    public Collection<IdentifierProxy> getRecData(){
      return mRecData;
    }
    public Collection<IdentifierProxy> getChannelData(){
      return mChannelData;
    }
    public Collection<Collection<SimpleExpressionProxy>> getSendData(){
      return mSendData;
    }
    public Collection<Collection<SimpleExpressionProxy>> getReceiveData(){
      return mReceiveData;
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