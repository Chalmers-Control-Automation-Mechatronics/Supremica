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
        ModuleEqualityVisitor.getInstance(false);
      return eqVisitor.isEqualList(getMsg(), msg.getMsg());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    final ModuleEqualityVisitor eqVisitor =
      ModuleEqualityVisitor.getInstance(false);
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
