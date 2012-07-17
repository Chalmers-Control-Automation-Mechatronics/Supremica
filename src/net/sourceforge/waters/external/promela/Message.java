package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class Message
{
  private final List<SimpleExpressionProxy> mMsg;
  private final List<String> mSenders = new ArrayList<String>();
  private final List<String> mRecipients = new ArrayList<String>();


  public Message(final List<SimpleExpressionProxy> msg)
  {
    mMsg = new ArrayList<SimpleExpressionProxy>(msg);
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

}
