//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.external.promela;

import gnu.trove.set.hash.THashSet;

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
  private boolean mHasMultipleSenders;
  private boolean mHasMultipleRecipients;

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
    List<String> senders = new ArrayList<String>();
    List<String> recipients = new ArrayList<String>();
    /*
     *
     */
    private final List<Message> mMessages;
    List<Message> mOutput;
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
        mHasMultipleSenders = false;
        mHasMultipleRecipients = false;
        mOutput = new ArrayList<Message>();
    }
    public void addMsgList(final Message m){

      boolean test = true;
      for(final Message t1: mOutput){
        if(t1.equals(m)){
          test = false;
          break;
        }
      }
      if(test){
        mOutput.add(m);
      }
    }
    public List<Message> getOutput(){
      return mOutput;
    }
    public void addSenders(final Collection<String> c){
      senders.addAll(c);
    }
    public void addRecipients(final Collection<String> c){
      recipients.addAll(c);
    }
    public void setSenders(final boolean b){
      mHasMultipleSenders = b;
    }

    public void setRecipients(final boolean b){
      mHasMultipleRecipients = b;
    }
    public boolean isSenderPresent(){
      return mHasMultipleSenders;
    }
    public boolean isRecipientPresent(){
      return mHasMultipleRecipients;
    }
    public List<String> getSenders(){
      return senders;
    }
    public List<String> getRecipients(){
      return recipients;
    }
    public ChanInfo()
    {
      mChanLength=0;
      mName="";
      mSendCount=0;
      mRecCount=0;
      mDataLength=0;
      mMessages = new ArrayList<Message>();
      mHasMultipleSenders = false;
      mHasMultipleRecipients = false;
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








