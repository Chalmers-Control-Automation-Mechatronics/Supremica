package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IFace2 Declaration
public interface IFace2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x3F593629,(short)0x8A84,(short)0x11D5,new char[]{0xBC,0xAD,0x00,0xD0,0xB7,0xE6,0x41,0x97});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition evalClosestPoint(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pPos) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant evalEdgeIntersections(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdge2 Edge) throws com.inzoom.comjni.ComJniException;
  public boolean applyTexture(String FileName,int XTile,int YTile,boolean SwapU,boolean SwapV) throws com.inzoom.comjni.ComJniException;
  public boolean applyTexture(String FileName,int XTile,int YTile,boolean SwapU) throws com.inzoom.comjni.ComJniException;
  public boolean applyTexture(String FileName,int XTile,int YTile) throws com.inzoom.comjni.ComJniException;
  public boolean applyTexture(String FileName,int XTile) throws com.inzoom.comjni.ComJniException;
  public boolean applyTexture(String FileName) throws com.inzoom.comjni.ComJniException;
  public boolean removeTexture() throws com.inzoom.comjni.ComJniException;
}
