package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface DocumentProperties Declaration
public interface DocumentProperties extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x2DF8D04D,(short)0x5BFA,(short)0x101B,new char[]{0xBD,0xE5,0x00,0xAA,0x00,0x44,0xDE,0x52});
  public void getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent,com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Value,com.inzoom.comjni.Variant LinkSource) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent,com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Value) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent,com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public int getCreator() throws com.inzoom.comjni.ComJniException;
}
