package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// interface ICommandBarControls Declaration
public interface ICommandBarControls extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xDA13E9EB,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl add(int Type,String Name,com.inzoom.comjni.IDispatch Icon,com.inzoom.comjni.Variant Before) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl add(int Type,String Name,com.inzoom.comjni.IDispatch Icon) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl add(int Type,String Name) throws com.inzoom.comjni.ComJniException;
  public void setIsFindControls(boolean bFindCtrls) throws com.inzoom.comjni.ComJniException;
  public void getFindControls(com.inzoom.comjni.IUnknown[] ppCtrls) throws com.inzoom.comjni.ComJniException;
}
