package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IBoundingBox Declaration
public interface IBoundingBox extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x7F15B67B,(short)0x06FE,(short)0x11D4,new char[]{0xA1,0xE6,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getMin() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getMax() throws com.inzoom.comjni.ComJniException;
}
