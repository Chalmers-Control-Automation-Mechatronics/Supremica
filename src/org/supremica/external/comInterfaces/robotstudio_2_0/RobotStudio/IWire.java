package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IWire Declaration
public interface IWire extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xA2AEEB6A,(short)0x87AB,(short)0x11D3,new char[]{0x8B,0xA0,0x00,0xC0,0x4F,0x68,0xDF,0x58});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public String getUniqueName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 getEntity() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICoedges getCoedges() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdges getEdges() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IVertices getVertices() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection split(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pos,boolean KeepOriginalEntity,String resName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection split(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pos,boolean KeepOriginalEntity) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire reverse() throws com.inzoom.comjni.ComJniException;
  public double getLength() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant evalIntersections(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire Wire) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire extend(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire Wire,boolean KeepOriginalEntity,String resName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire extend(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire Wire,boolean KeepOriginalEntity) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire offset(double OffsetDistance,boolean KeepOriginalEntity,String resName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire offset(double OffsetDistance,boolean KeepOriginalEntity) throws com.inzoom.comjni.ComJniException;
}
