package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IRsFilter Declaration
public interface IRsFilter extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x78A4FEAA,(short)0xA30E,(short)0x11D3,new char[]{0xAD,0x05,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public int getObjectType() throws com.inzoom.comjni.ComJniException;
  public void setObjectType(int pVal) throws com.inzoom.comjni.ComJniException;
  public void checkType(int Node) throws com.inzoom.comjni.ComJniException;
}
