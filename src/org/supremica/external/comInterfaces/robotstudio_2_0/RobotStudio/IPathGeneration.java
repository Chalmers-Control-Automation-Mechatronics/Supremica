package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPathGeneration Declaration
public interface IPathGeneration extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x09531112,(short)0xB879,(short)0x11D3,new char[]{0xBF,0x75,0x00,0xC0,0x4F,0x68,0xDF,0x5A});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths distributeTargetsOnCurves(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities,double MinimumDistance,double MaximuDeviation,int ApproximationType,int pPriority) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths distributeTargetsOnCurves(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities,double MinimumDistance,double MaximuDeviation,int ApproximationType) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 createTargetOnCurve(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,int OffsetType,double Distance,int ReferencePoint) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant createCircularInfo(com.inzoom.comjni.Variant PointPos,com.inzoom.comjni.Variant TangentPos,double cordafault) throws com.inzoom.comjni.ComJniException;
}
