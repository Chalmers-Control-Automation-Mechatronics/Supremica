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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class Message implements Comparable<Message>
{
  private final List<SimpleExpressionProxy> mMsg;
  private final List<String> mSenders;
  private final List<String> mRecipients;


  public Message(final List<SimpleExpressionProxy> msg)
  {
    mMsg = new ArrayList<SimpleExpressionProxy>(msg);
    mSenders = new ArrayList<String>();
    mRecipients = new ArrayList<String>();
  }

  public Message(final List<SimpleExpressionProxy> msg, final List<String> senders, final List<String> receivers)
  {
    mMsg = new ArrayList<SimpleExpressionProxy>(msg);
    mSenders = new ArrayList<String>(senders);
    mRecipients = new ArrayList<String>(receivers);
  }

  public void addSender(final String sender)
  {
    if (!mSenders.contains(sender)) {
      mSenders.add(sender);
    }
  }

  public void addRecipient(final String recipient)
  {
    if (!mRecipients.contains(recipient)) {
      mRecipients.add(recipient);
    }
  }

  public boolean hasSenders()
  {
    return mSenders.size() != 0;
  }

  public boolean hasRecipients()
  {
    return mRecipients.size() != 0;
  }

  @Override
  public boolean equals(final Object other)
  {
    if (getClass() == other.getClass()) {
      final Message msg = (Message) other;
      final ModuleEqualityVisitor eqVisitor =
        new ModuleEqualityVisitor(false);
      return eqVisitor.isEqualList(getMsg(), msg.getMsg());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    final ModuleEqualityVisitor eqVisitor = new ModuleEqualityVisitor(false);
    final ModuleHashCodeVisitor hashVisitor = eqVisitor.getHashCodeVisitor();
    return hashVisitor.getListHashCode(this.getMsg());
  }

  @Override
  public String toString()
  {
    return mSenders.toString() + " -> " + mRecipients.toString() + " : " + mMsg.toString();
  }

  public List<SimpleExpressionProxy> getMsg()
  {
    return mMsg;
  }

  public List<String> getSenders()
  {
    return mSenders;
  }

  public List<String> getRecipients()
  {
    return mRecipients;
  }

  /**
   * A method to combine another message with this message
   * @param m The message to combine into this message
   */
  public void combine(final Message m)
  {
    for(final String sender : m.getSenders())
    {
      if(!mSenders.contains(sender))
        mSenders.add(sender);
    }

    for(final String receiver : m.getRecipients())
    {
      if(!mRecipients.contains(receiver))
        mRecipients.add(receiver);
    }
  }

  public Message clone(final ModuleProxyCloner cloner)
  {
    final List<SimpleExpressionProxy> msg = cloner.getClonedList(this.mMsg);
    return new Message(msg, getSenders(), getRecipients());
  }

  /**
   * A method to compare this message with another message. <br>
   * Only the message content is checked in the comparison
   * @param other The message to compare this message to.
   */
  @Override
  public int compareTo(final Message other)
  {
    final List<SimpleExpressionProxy> content = other.getMsg();
    for(int i = 0; i < mMsg.size(); i++)
    {
      final String s = mMsg.get(i).toString();
      final String s2 = content.get(i).toString();
      final int value = s.compareTo(s2);
      if(value > 0)
        return 1;
      else if(value < 0)
        return -1;
    }
    return 0;
  }
}








