package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface CommandBarControls Declaration
public interface CommandBarControls extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0306,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl add(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Parameter,com.inzoom.comjni.Variant Before,com.inzoom.comjni.Variant Temporary) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl add(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Parameter,com.inzoom.comjni.Variant Before) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl add(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Parameter) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl add(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl add(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl add() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getParent() throws com.inzoom.comjni.ComJniException;
}
