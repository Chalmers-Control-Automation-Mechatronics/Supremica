package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface PropertyTests Declaration
public interface PropertyTests extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0334,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTest getItem(int Index) throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public void add(String Name,int Condition,com.inzoom.comjni.Variant Value,com.inzoom.comjni.Variant SecondValue,int Connector) throws com.inzoom.comjni.ComJniException;
  public void add(String Name,int Condition,com.inzoom.comjni.Variant Value,com.inzoom.comjni.Variant SecondValue) throws com.inzoom.comjni.ComJniException;
  public void add(String Name,int Condition,com.inzoom.comjni.Variant Value) throws com.inzoom.comjni.ComJniException;
  public void add(String Name,int Condition) throws com.inzoom.comjni.ComJniException;
  public void remove(int Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
}
