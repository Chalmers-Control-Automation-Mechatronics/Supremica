package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface Property Declaration
public interface Property extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E18C,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.Variant getValue() throws com.inzoom.comjni.ComJniException;
  public void setValue(com.inzoom.comjni.Variant lppvReturn) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2,com.inzoom.comjni.Variant Index3,com.inzoom.comjni.Variant Index4) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2,com.inzoom.comjni.Variant Index3) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getIndexedValue(com.inzoom.comjni.Variant Index1) throws com.inzoom.comjni.ComJniException;
  public void setIndexedValue(com.inzoom.comjni.Variant Index1,com.inzoom.comjni.Variant Index2,com.inzoom.comjni.Variant Index3,com.inzoom.comjni.Variant Index4,com.inzoom.comjni.Variant lppvReturn) throws com.inzoom.comjni.ComJniException;
  public short getNumIndices() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties getParent() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties getCollection() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown getObject() throws com.inzoom.comjni.ComJniException;
  public void setObject(com.inzoom.comjni.IUnknown lppunk) throws com.inzoom.comjni.ComJniException;
}
