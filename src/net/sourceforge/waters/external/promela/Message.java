package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class Message
{
  private final List<SimpleExpressionProxy> mMsg;
  final ModuleEqualityVisitor eqVisitor = ModuleEqualityVisitor.getInstance(false);
  private final List<String> mSenders = new ArrayList<String>();
  private final List<String> mRecipients = new ArrayList<String>();

  public Message(final List<SimpleExpressionProxy> msg){
    mMsg = new ArrayList<SimpleExpressionProxy>(msg);
  }

  public void addSenders(final String sender){
    mSenders.add(sender);
  }
  public void addRecipients(final String recipient){
    mRecipients.add(recipient);
  }

  @Override
  public boolean equals(final Object msg){
    return eqVisitor.isEqualList(this.getMsg(), ((Message)msg).getMsg());
  }
  @Override
  public int hashCode(){
    final ModuleHashCodeVisitor hashVisitor = eqVisitor.getHashCodeVisitor();
    return hashVisitor.getListHashCode(this.getMsg());
  }

  public List<SimpleExpressionProxy> getMsg(){
    return mMsg;
  }
  public List<String> getSenders(){
    return mSenders;
  }
  public List<String> getRecipients(){
    return mRecipients;
  }
}
