package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IIO Declaration
public interface IIO extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x484EA472,(short)0xEF59,(short)0x11D3,new char[]{0x80,0xF6,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void setType(int pVal) throws com.inzoom.comjni.ComJniException;
  public int getDirection() throws com.inzoom.comjni.ComJniException;
  public void setDirection(int pVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getValue() throws com.inzoom.comjni.ComJniException;
  public void setValue(com.inzoom.comjni.Variant pVal) throws com.inzoom.comjni.ComJniException;
}
