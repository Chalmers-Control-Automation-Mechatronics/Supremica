package net.sourceforge.waters.external.promela;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;

/**
 * A class to store the information about a channel in
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
  private final HashMap<Message,Message> mMessages;

  /**
   * The constructor for this class
   * @param name The name of this channel
   * @param length How many messages this channel can store.
   * Zero indicates that this is an exchange channel
   * @param types The type of each item in messages that pass through this channel
   */
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
  }

  /**
   * A method to add a send statement to the channel
   */
  public void addSend(final Message m)
  {
    if(storeMessage(m))
    {
      //The message was just added
      //The message has new content, so need to keep record of the message items
      //TODO
    }
    else
    {
      //The message already existed, so the receivers were updated
    }
  }

  /**
   * A method to add a receive statement to the channel.
   */
  public void addReceive(final Message m)
  {
    if(storeMessage(m))
    {
      //The message was just added
      //The message has new content, so need to keep record of the message items
      //TODO
    }
    else
    {
      //The message already existed, so the receivers were updated
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
  public Message getMatch(final Message m)
  {
    return mMessages.get(m);
  }

  /**
   * A method to check if a given message is sendable on this channel
   * @param m The message to check
   * @return True if the message is sendable, false otherwise
   */
  public boolean isSendable(final Message m)
  {
    //TODO
    //Also replace any constants with their corresponding variable type
    //and check if that matches a message with senders

    final Message match = mMessages.get(m);
    if(match == null)
    {
      return false;
    }
    else
    {
      return match.hasSenders();
    }
  }

  /**
   * A method to check if a given message is receivable on this channel.
   * If the channel is asynchronous, then will not look to see if anyone can receive this message.
   * @param m The message to check
   * @return True if the message is receivable, false otherwise
   */
  public boolean isReceivable(final Message m)
  {
  //TODO
    //Also replace any constants with their corresponding variable type
    //and check if that matches a message with receivers

    final Message match = mMessages.get(m);
    if(match == null)
    {
      return false;
    }
    else if(mLength > 0)
    {
      return true;
    }
    else
    {
      return match.hasRecipients();
    }
  }

  /**
   * A method to get all of the messages that can be sent on this channel.
   * @return A linked list containing the messages
   */
  public List<Message> getSendableMessages()
  {
    final LinkedList<Message> sendableMessages = new LinkedList<Message>();
    for(final Message m : mMessages.values())
    {
      if(m.hasSenders())
      {
        sendableMessages.add(m);
      }
    }

    return sendableMessages;
  }

  /**
   * A method to get all of the messages that can be received on this channel
   * @return A linked list containing the messages
   */
  public List<Message> getReceivableMessages()
  {
    final LinkedList<Message> receivableMessages = new LinkedList<Message>();
    for(final Message m : mMessages.values())
    {
      if(m.hasRecipients())
      {
        receivableMessages.add(m);
      }
    }

    return receivableMessages;
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
   * A method to get the length of this channel
   * @return An integer indicating the channel length
   */
  public int getLength()
  {
    return mLength;
  }

  /**
   * A method to get the name of this channel
   * @return A string containing the name of this channel
   */
  public String getName()
  {
    return mName;
  }

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
