package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface DocumentProperty Declaration
public interface DocumentProperty extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x2DF8D04E,(short)0x5BFA,(short)0x101B,new char[]{0xBD,0xE5,0x00,0xAA,0x00,0x44,0xDE,0x52});
  public void getParent() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pbstrRetVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getValue() throws com.inzoom.comjni.ComJniException;
  public void setValue(com.inzoom.comjni.Variant pvargRetVal) throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void setType(int ptypeRetVal) throws com.inzoom.comjni.ComJniException;
  public boolean getLinkToContent() throws com.inzoom.comjni.ComJniException;
  public void setLinkToContent(boolean pfLinkRetVal) throws com.inzoom.comjni.ComJniException;
  public String getLinkSource() throws com.inzoom.comjni.ComJniException;
  public void setLinkSource(String pbstrSourceRetVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public int getCreator() throws com.inzoom.comjni.ComJniException;
}
