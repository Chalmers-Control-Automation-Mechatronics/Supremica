package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IEntity Declaration
public interface IEntity extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x82273A10,(short)0x59FA,(short)0x11D3,new char[]{0xAC,0xB2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getTransform() throws com.inzoom.comjni.ComJniException;
  public void setTransform(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform pVal) throws com.inzoom.comjni.ComJniException;
  public String getUniqueName() throws com.inzoom.comjni.ComJniException;
  public boolean getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public double getArea() throws com.inzoom.comjni.ComJniException;
  public double getVolume() throws com.inzoom.comjni.ComJniException;
  public boolean getSelected() throws com.inzoom.comjni.ComJniException;
  public boolean getHasSelectedWire() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 getParent() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getCenterOfGravity() throws com.inzoom.comjni.ComJniException;
  public float getRelativeTransparency() throws com.inzoom.comjni.ComJniException;
  public void setRelativeTransparency(float RelTransp) throws com.inzoom.comjni.ComJniException;
  public void examine() throws com.inzoom.comjni.ComJniException;
  public void unexamine() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IShells getShells() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFaces getFaces() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdges getEdges() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException;
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities intersect(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal,String BaseName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities intersect(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 join(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal,String BaseName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 join(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities cut(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal,String BaseName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities cut(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox getBoundingBox() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWires getWires() throws com.inzoom.comjni.ComJniException;
  public boolean getBackFaceCulling() throws com.inzoom.comjni.ComJniException;
  public void setBackFaceCulling(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 coverWire(boolean KeepOriginal,String resName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 coverWire(boolean KeepOriginal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 reverse() throws com.inzoom.comjni.ComJniException;
}
