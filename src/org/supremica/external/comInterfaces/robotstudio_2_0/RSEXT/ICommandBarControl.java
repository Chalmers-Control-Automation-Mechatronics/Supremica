package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// interface ICommandBarControl Declaration
public interface ICommandBarControl extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xDA13E9DC,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public void execute() throws com.inzoom.comjni.ComJniException;
  public int getId() throws com.inzoom.comjni.ComJniException;
  public boolean getEnabled() throws com.inzoom.comjni.ComJniException;
  public void setEnabled(boolean pEnabled) throws com.inzoom.comjni.ComJniException;
  public boolean getBeginGroup() throws com.inzoom.comjni.ComJniException;
  public void setBeginGroup(boolean pvarfBeginGroup) throws com.inzoom.comjni.ComJniException;
  public String getCaption() throws com.inzoom.comjni.ComJniException;
  public void setCaption(String pbstrCaption) throws com.inzoom.comjni.ComJniException;
}
