package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface HTMLProjectItem Declaration
public interface HTMLProjectItem extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0358,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public boolean getIsOpen() throws com.inzoom.comjni.ComJniException;
  public void loadFromFile(String FileName) throws com.inzoom.comjni.ComJniException;
  public void open(int OpenKind) throws com.inzoom.comjni.ComJniException;
  public void open() throws com.inzoom.comjni.ComJniException;
  public void saveCopyAs(String FileName) throws com.inzoom.comjni.ComJniException;
  public String getText() throws com.inzoom.comjni.ComJniException;
  public void setText(String Text) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
}
