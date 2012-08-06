package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;

/**
 * A class to store the information about a channel in.
 * TODO Eventually replace ChanInfo.java with this class
 * @author Ethan Duff
 */
public class PromelaChannel
{
  //The name of this channel
  private String mName;

  //How long the channel is
  //Zero indicates a synchronous (exchange) channel
  private final int mLength;

  //Does the channel have multiple senders and receivers
  private boolean mMultipleSenders;
  private boolean mMultipleReceivers;

  //The data type of each parameter in messages passed along this channel
  private final Type[] mDataTypes;

  //All of the messages that get sent and received on the channel
  private final HashMap<Message, Message> mMessages;

  private final ProxyAccessorHashSet<SimpleExpressionProxy>[] mSentItems;
  private final ProxyAccessorHashSet<SimpleExpressionProxy>[] mReceivedItems;

  /**
   * The constructor for this class
   * @param name The name of this channel
   * @param length How many messages this channel can store.
   * Zero indicates that this is an exchange channel
   * @param types The type of each item in messages that pass through this channel
   */
  @SuppressWarnings("unchecked")
  public PromelaChannel(final String name, final int length, final List<String> types)
  {
    mLength = length;

    mDataTypes = new Type[types.size()];
    for(int i = 0; i < mDataTypes.length; i++)
    {
      final String s = types.get(i);
      if(s.equals("mtype"))
      {
        mDataTypes[i] = Type.MTYPE;
      }
      else if(s.equals("bit") || s.equals("bool"))
      {
        mDataTypes[i] = Type.BIT;
      }
      else if(s.equals("byte"))
      {
        mDataTypes[i] = Type.BYTE;
      }
      else if(s.equals("short"))
      {
        mDataTypes[i] = Type.SHORT;
      }
      else if(s.equals("int"))
      {
        mDataTypes[i] = Type.INT;
      }
    }

    mMessages = new HashMap<Message,Message>();
    mMultipleReceivers = false;
    mMultipleSenders = false;

    mSentItems = new ProxyAccessorHashSet[types.size()];
    for(int i = 0; i < types.size(); i++)
    {
      mSentItems[i] = new ProxyAccessorHashSet<SimpleExpressionProxy>(ModuleEqualityVisitor.getInstance(false));
    }

    mReceivedItems = new ProxyAccessorHashSet[types.size()];
    for(int i = 0; i < types.size(); i++)
    {
      mReceivedItems[i] = new ProxyAccessorHashSet<SimpleExpressionProxy>(ModuleEqualityVisitor.getInstance(false));
    }
  }

  /**
   * A method to get all of the messages that are sent and received.
   * Note that this method is expensive to perform, so should be used carefully
   * @param factory The ModuleProxyFactory used for creating proxys
   * @param mtypeRange The range of values a mtype can take
   * @return An ArrayList containing all of the messages
   */
  public ArrayList<Message> getAllMessages(final ModuleProxyFactory factory, final List<String> mtypeRange)
  {
    final ArrayList<Message> output = new ArrayList<Message>();
    for(final Entry<Message,Message> entry : mMessages.entrySet())
    {
      final Message current = entry.getValue();
      if(current.hasSenders())
      {
        expand(current, 0, factory, output, mtypeRange);
      }
    }

    return output;
  }

  private void expand(final Message m, final int i, final ModuleProxyFactory factory, final ArrayList<Message> storage, final List<String> mtypeRange)
  {
    if(i == getWidth())
    {
      final Message match = getMatch(m, factory);
      if(match != null)
      {
        storage.add(match.clone(factory.getCloner()));
      }
    }
    else
    {
      if(isVariable(i, m.getMsg().get(i)))
      {
        //Substitute this variable for all of its possible values
        if(mDataTypes[i] == Type.MTYPE)
        {
          for(final String s :mtypeRange)
          {
            if(isSent(i, factory.createSimpleIdentifierProxy(s)))
            {
              m.getMsg().set(i, factory.createSimpleIdentifierProxy(s));
              expand(m, i+1, factory, storage, mtypeRange);
            }
          }
        }
        else
        {
          PromelaIntRange range;
          switch(mDataTypes[i])
          {
          case BIT:
            range = PromelaIntRange.BIT;
            break;
          case BYTE:
            range = PromelaIntRange.BYTE;
            break;
          case SHORT:
            range = PromelaIntRange.SHORT;
            break;
          case INT:
            range = PromelaIntRange.INT;
            break;
          default:
            range = null;//This can never be reached, just here so that compiler thinks that it is initialised always
          }

          for(int j = range.getLower(); j < range.getUpper(); j++)
          {
            if(isSent(i, factory.createIntConstantProxy(j)))
            {
              m.getMsg().set(i, factory.createIntConstantProxy(j));
              expand(m, i+1, factory, storage, mtypeRange);
            }
          }
        }
      }
      else
      {
        expand(m, i+1, factory, storage, mtypeRange);
      }
    }
  }

  /**
   * A method to add a send statement to the channel
   */
  public void addSend(final Message m)
  {
    storeMessage(m);

    final List<SimpleExpressionProxy> values = m.getMsg();

    for(int i = 0; i < mSentItems.length; i++)
    {
      final SimpleExpressionProxy value = values.get(i);
      if(!mSentItems[i].containsProxy(value))
      {
        mSentItems[i].addProxy(value);
      }
    }
  }

  /**
   * A method to add a receive statement to the channel.
   */
  public void addReceive(final Message m)
  {
    storeMessage(m);

    final List<SimpleExpressionProxy> values = m.getMsg();

    for(int i = 0; i < mReceivedItems.length; i++)
    {
      final SimpleExpressionProxy value = values.get(i);
      if(!mReceivedItems[i].containsProxy(value))
      {
        mReceivedItems[i].addProxy(value);
      }
    }
  }

  /**
   * A method to store a given message in the channel.
   * If the message is already contained, then the senders
   * and receivers of the contained message are updated to
   * include the senders and receivers of the new message
   * @param m The message to store
   * @return True if this is a new message, false if it was already contained.
   */
  private boolean storeMessage(final Message m)
  {
    final Message value = mMessages.get(m);
    if(value == null)
    {
      //This is a new message, so store it
      mMessages.put(m, m);
      return true;
    }
    else
    {
      //This is an old message, so update its senders and receivers
      value.combine(m);
      return false;
    }
  }

  /**
   * A method to get the messages that matches the given message template.
   * The returned message will contain all of the senders and receivers of that message.
   * @param m The template to find the matching message to.
   * @return The matching message, or null if no match is found.
   */
  public Message getMatch(final Message m, final ModuleProxyFactory factory)
  {
    final ModuleProxyCloner cloner = factory.getCloner();
    final Message match = mMessages.get(m);
    if(match == null)
    {
      return null;
    }
    else
    {
      final Message retrievedMessage = mMessages.get(m).clone(cloner);

      getSendersAndReceivers(retrievedMessage, 0, factory);

      return retrievedMessage;
    }
  }

  private void getSendersAndReceivers(final Message message, final int index, final ModuleProxyFactory factory)
  {
    if(index == mDataTypes.length)
    {
      final Message m = mMessages.get(message);
      if(m != null)
      {
        for(final String s : m.getSenders())
        {
          message.addSender(s);
        }

        for(final String s : m.getRecipients())
        {
          message.addRecipient(s);
        }
      }
      return;
    }

    getSendersAndReceivers(message, index+1, factory);
    if(isVariable(index, message.getMsg().get(index)))
    {
      return;
    }
    else
    {
      final SimpleExpressionProxy value = message.getMsg().get(index);
      message.getMsg().set(index, getTypeIdentifier(index, factory));
      getSendersAndReceivers(message, index+1, factory);
      message.getMsg().set(index, value);
    }
  }

  /**
   * A method to check if a given message is sendable on this channel
   * @param m The message to check
   * @return True if the message is sendable, false otherwise
   */
  public boolean isSendable(final Message m, final ModuleProxyFactory factory)
  {
    if(mLength > 0)
    {
      return true;//This is an asynchronous channel, so any message can be sent on it
    }
    else
    {
      final ModuleProxyCloner cloner = factory.getCloner();
      return isSendable(m.clone(cloner), 0, factory);
    }
  }

  private boolean isSendable(final Message m, final int index, final ModuleProxyFactory factory)
  {
    //Check if there is more values to change
    if(index == mDataTypes.length)
    {
      //There are no more values to change, so check if the message is a match now
      final Message match = mMessages.get(m);
      if(match == null)
        return false;
      else
        return match.hasRecipients();
    }

    //Check if changing further values will get a match
    if(isSendable(m, index+1, factory))
      return true;

    if(isVariable(index, m.getMsg().get(index)))
    {
      return false;
    }
    else
    {
      //Change the value of the item to be a variable
      final SimpleExpressionProxy value = m.getMsg().get(index);
      m.getMsg().set(index, getTypeIdentifier(index, factory));
      final boolean result = isSendable(m, index+1, factory);
      m.getMsg().set(index, value);
      return result;
    }
  }

  /**
   * A method to check if a given message is receivable on this channel.
   * If the channel is asynchronous, then will not look to see if anyone can receive this message.
   * @param m The message to check
   * @return True if the message is receivable, false otherwise
   */
  public boolean isReceivable(final Message m, final ModuleProxyFactory factory)
  {
    final ModuleProxyCloner cloner = factory.getCloner();
    return isReceivable(m.clone(cloner), 0, factory);
  }

  private boolean isReceivable(final Message m, final int index, final ModuleProxyFactory factory)
  {
    //Check if there is more values to change
    if(index == mDataTypes.length)
    {
      //There are no more values to change, so check if the message is a match now
      final Message match = mMessages.get(m);
      if(match == null)
        return false;
      else
        return match.hasSenders();
    }

    //Check if changing further values will get a match
    if(isReceivable(m, index+1, factory))
      return true;

    if(isVariable(index, m.getMsg().get(index)))
    {
      return false;
    }
    else
    {
      //Change the value of the item to be a variable
      final SimpleExpressionProxy value = m.getMsg().get(index);
      m.getMsg().set(index, getTypeIdentifier(index, factory));
      final boolean result = isReceivable(m, index+1, factory);
      m.getMsg().set(index, value);
      return result;
    }
  }

  /**
   * A method to check if the given item is the variable type of the given channel index
   * @param index
   * @param item
   * @return
   */
  public boolean isVariable(final int index, final SimpleExpressionProxy item)
  {
    final String text = item.toString();

    switch(mDataTypes[index])
    {
    case BIT:
      return text.equals("Bit") || text.equals("bit") || text.equals("Bool") || text.equals("bool");
    case BYTE:
      return text.equals("Byte") || text.equals("byte");
    case INT:
      return text.equals("Int") || text.equals("int");
    case MTYPE:
      return text.equals(":Mtype") || text.equals(":mtype");
    }

    //This is unreachable, but added because the compiler doesn't know that
    return false;
  }

  /**
   * A method to check if a given item appears in any sent message at the given position
   * @param index The position that the item might appear in a message
   * @param item The item that may appear in a message
   * @return True if the item is contained in a sent message at the given index, false otherwise
   */
  public boolean isSent(final int index, final SimpleExpressionProxy item)
  {
    return mSentItems[index].containsProxy(item);
  }

  /**
   * A method to check if a given item appears in any received message at the given position
   * @param index The position that the item might appear in a message
   * @param item The item that may appear in a message
   * @return The if the item is contained in a received message at the given index, false otherwise
   */
  public boolean isReceived(final int index, final SimpleExpressionProxy item)
  {
    return mReceivedItems[index].containsProxy(item);
  }

  public ProxyAccessorHashSet<SimpleExpressionProxy>[] getSentItems()
  {
    return mSentItems;
  }

  public ProxyAccessorHashSet<SimpleExpressionProxy>[] getReceivedItems()
  {
    return mReceivedItems;
  }

  /**
   * A method to check if this channel has multiple processes sending on it
   * @return True if there are multiple senders, false otherwise
   */
  public boolean hasMultipleSenders()
  {
    return mMultipleSenders;
  }

  /**
   * A method to check if this channel has multiple processes receiving from it
   * @return True if there are multiple receivers, false otherwise
   */
  public boolean hasMultipleReceivers()
  {
    return mMultipleReceivers;
  }

  public void setMultipleSenders(final boolean value)
  {
    mMultipleSenders = value;
  }

  public void setMultipleReceivers(final boolean value)
  {
    mMultipleReceivers = value;
  }

  /**
   * A method to get the length of this channel.
   * @return An integer indicating the channel length.
   */
  public int getLength()
  {
    return mLength;
  }

  /**
   * A method to get the number of items sent on this channel at one time.
   * @return An integer indicating the width.
   */
  public int getWidth()
  {
    return mDataTypes.length;
  }

  /**
   * A method to get the name of this channel
   * @return A string containing the name of this channel
   */
  public String getName()
  {
    return mName;
  }

  /**
   * A method to get the data type at the given channel position.
   * @param position The position to get the data type of.
   * @return A Type containing the data type of items sent and received at this position.
   */
  public Type getType(final int position)
  {
    return mDataTypes[position];
  }

  /**
   * A method to create an identifier for the variable type at the given position.
   * @param position The position in the message the variable is found.
   * @param factory The ModuleProxyFactory used for creating Proxys
   * @return A SimpleIdentifierProxy containing the identifier
   */
  public SimpleIdentifierProxy getTypeIdentifier(final int position, final ModuleProxyFactory factory)
  {
    switch(mDataTypes[position])
    {
    case BIT:
    {
      return factory.createSimpleIdentifierProxy("Bit");
    }
    case BYTE:
    {
      return factory.createSimpleIdentifierProxy("Byte");
    }
    case SHORT:
    {
      return factory.createSimpleIdentifierProxy("Short");
    }
    case INT:
    {
      return factory.createSimpleIdentifierProxy("Int");
    }
    case MTYPE:
    {
      //Use :Mtype, as the colon cannot be used as a mtype character.
      //This makes sure that this cannot be the same name as one of the mtype names
      return factory.createSimpleIdentifierProxy(":Mtype");
    }
    }

    //This is just here to stop the compiler complaining about not having a return
    //statement for all possible execution paths.  It will never be reached.
    return null;
  }


  public enum Type { BIT, BYTE, SHORT, INT, MTYPE }
}
