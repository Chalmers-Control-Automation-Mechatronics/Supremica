package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface _CommandBarComboBox Declaration
public interface _CommandBarComboBox extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C030C,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public void addItem(String Text,com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public void addItem(String Text) throws com.inzoom.comjni.ComJniException;
  public void clear() throws com.inzoom.comjni.ComJniException;
  public int getDropDownLines() throws com.inzoom.comjni.ComJniException;
  public void setDropDownLines(int pcLines) throws com.inzoom.comjni.ComJniException;
  public int getDropDownWidth() throws com.inzoom.comjni.ComJniException;
  public void setDropDownWidth(int pdx) throws com.inzoom.comjni.ComJniException;
  public String getList(int Index) throws com.inzoom.comjni.ComJniException;
  public void setList(int Index,String pbstrItem) throws com.inzoom.comjni.ComJniException;
  public int getListCount() throws com.inzoom.comjni.ComJniException;
  public int getListHeaderCount() throws com.inzoom.comjni.ComJniException;
  public void setListHeaderCount(int pcItems) throws com.inzoom.comjni.ComJniException;
  public int getListIndex() throws com.inzoom.comjni.ComJniException;
  public void setListIndex(int pi) throws com.inzoom.comjni.ComJniException;
  public void removeItem(int Index) throws com.inzoom.comjni.ComJniException;
  public int getStyle() throws com.inzoom.comjni.ComJniException;
  public void setStyle(int pstyle) throws com.inzoom.comjni.ComJniException;
  public String getText() throws com.inzoom.comjni.ComJniException;
  public void setText(String pbstrText) throws com.inzoom.comjni.ComJniException;
}
