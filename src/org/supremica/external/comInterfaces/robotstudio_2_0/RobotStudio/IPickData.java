package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPickData Declaration
public interface IPickData extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x6D336C8D,(short)0xF04C,(short)0x11D3,new char[]{0x80,0xF7,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public double getX() throws com.inzoom.comjni.ComJniException;
  public void setX(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getY() throws com.inzoom.comjni.ComJniException;
  public void setY(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getZ() throws com.inzoom.comjni.ComJniException;
  public void setZ(double pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getPickedObject() throws com.inzoom.comjni.ComJniException;
  public void setPickedObject(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject pVal) throws com.inzoom.comjni.ComJniException;
  public boolean hasObject() throws com.inzoom.comjni.ComJniException;
}
