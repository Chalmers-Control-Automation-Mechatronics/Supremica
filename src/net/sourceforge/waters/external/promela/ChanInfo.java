package net.sourceforge.waters.external.promela;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public class ChanInfo {

  // TODO
  // Each channel has got only one event declaration, so it must be
  // determined at channel level whether sender and receiver arguments
  // are present or not.
  // A sender argument is present if at least one message is sent by more than
  // one proctype.
  // A receiver argument is present if at least one message is received by
  // more than one proctype.
  // Once determined whether or not senders and recipients are present, this
  // information should be stored on the channel.
  // private boolean mHasMultipleSenders;
  // private boolean mHasMultipleRecipients;

  // TODO
  // Using the above information, the event declarations can be created
  // as follows.
  //
  // IF mChanLength == 0
  //   exch_<name>[data]..[data] if neither sender nor recipient present;
  //   exch_<name>[sender][data]..[data] if only sender present;
  //   exch_<name>[rcpt][data]..[data] if only recipient present;
  //   exch_<name>[sender][rcpt][data]..[data] if sender and recipient present.
  // IF mChanLength > 0
  //   send_<name>[data]..[data] if sender not present
  //   send_<name>[sender][data]..[data] if sender present
  //   plus
  //   recv_<name>[data]..[data] if recipient not present
  //   recv_<name>[rcpt][data]..[data] if recipient present

  // TODO
  // When compiling send and receive statements, look up in the channel
  // whether or not senders and recipients are used, and use this information
  // to generate the correct event labels (identifiers). This decision must
  // be based on the channel, not on the individual messages.

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
              mMessages.get((mMessages.indexOf(msg))).addRecipient(s);
            }
          }
        }
        if(msg.hasSenders()){
          for(final String s: msg.getSenders()){
            if(!mMessages.get((mMessages.indexOf(msg))).getSenders().contains(s)){
              mMessages.get((mMessages.indexOf(msg))).addSender(s);
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