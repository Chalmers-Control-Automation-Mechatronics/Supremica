package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IFace Declaration
public interface IFace extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x5061DEA8,(short)0x87AB,(short)0x11D3,new char[]{0x8B,0xA0,0x00,0xC0,0x4F,0x68,0xDF,0x58});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public String getUniqueName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 getEntity() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IShell getShell() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundaries getBoundaries() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdges getEdges() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IVertices getVertices() throws com.inzoom.comjni.ComJniException;
  public boolean extrude(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire Wire,boolean Forward,double Draft,boolean Round) throws com.inzoom.comjni.ComJniException;
  public boolean extrude(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire Wire,boolean Forward,double Draft) throws com.inzoom.comjni.ComJniException;
  public boolean extrude(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire Wire,boolean Forward) throws com.inzoom.comjni.ComJniException;
  public boolean extrude(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire Wire) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException;
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getNormalAtPoint(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Position) throws com.inzoom.comjni.ComJniException;
  public double getMin_u() throws com.inzoom.comjni.ComJniException;
  public double getMax_u() throws com.inzoom.comjni.ComJniException;
  public double getMin_v() throws com.inzoom.comjni.ComJniException;
  public double getMax_v() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 curveAlongU(double v,String resName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 curveAlongU(double v) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 curveAlongV(double u,String resName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 curveAlongV(double u) throws com.inzoom.comjni.ComJniException;
}
