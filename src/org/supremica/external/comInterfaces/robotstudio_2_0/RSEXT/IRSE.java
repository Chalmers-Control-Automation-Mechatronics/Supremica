package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// interface IRSE Declaration
public interface IRSE extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xF8D4B88B,(short)0xE7A2,(short)0x11D3,new char[]{0x80,0xD5,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars getCommandBars() throws com.inzoom.comjni.ComJniException;
  public void executeCommand(int CommandID) throws com.inzoom.comjni.ComJniException;
  public void fireCommandBarControlClick(int CommandID,boolean[] CancelDefault) throws com.inzoom.comjni.ComJniException;
}
