package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// interface ICommandBarComboBox Declaration
public interface ICommandBarComboBox extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xDA13E9E5,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void addItem(String Text,int Index) throws com.inzoom.comjni.ComJniException;
  public void addItem(String Text) throws com.inzoom.comjni.ComJniException;
  public void removeItem(int Index) throws com.inzoom.comjni.ComJniException;
  public void clear() throws com.inzoom.comjni.ComJniException;
  public int getListCount() throws com.inzoom.comjni.ComJniException;
  public int getListIndex() throws com.inzoom.comjni.ComJniException;
  public void setListIndex(int pIndex) throws com.inzoom.comjni.ComJniException;
  public String getList(int Index) throws com.inzoom.comjni.ComJniException;
}
