package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IEdge Declaration
public interface IEdge extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x092DBA3F,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFaces getFaces() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICoedges getCoedges() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IVertex getStartVertex() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IVertex getEndVertex() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition evalPosition(double ParameterValue) throws com.inzoom.comjni.ComJniException;
  public void evalDirection(double ParameterValue,double[] tx,double[] ty,double[] tz) throws com.inzoom.comjni.ComJniException;
  public double evalParam(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Position) throws com.inzoom.comjni.ComJniException;
  public void evalParamRange(double[] StartPar,double[] EndPar) throws com.inzoom.comjni.ComJniException;
  public double evalLengthParam(double DatumParameter,double ArcLength) throws com.inzoom.comjni.ComJniException;
  public double evalLength(double StartParameter,double EndParameter) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant evalFacetCurve(double cordafault) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant evalIntersections(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdge Edge) throws com.inzoom.comjni.ComJniException;
}
