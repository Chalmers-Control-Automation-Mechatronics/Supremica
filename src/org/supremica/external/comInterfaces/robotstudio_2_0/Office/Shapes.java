package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface Shapes Declaration
public interface Shapes extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C031E,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addCallout(int Type,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addConnector(int Type,float BeginX,float BeginY,float EndX,float EndY) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addCurve(com.inzoom.comjni.Variant SafeArrayOfPoints) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addLabel(int Orientation,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addLine(float BeginX,float BeginY,float EndX,float EndY) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPicture(String FileName,int LinkToFile,int SaveWithDocument,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPicture(String FileName,int LinkToFile,int SaveWithDocument,float Left,float Top,float Width) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPicture(String FileName,int LinkToFile,int SaveWithDocument,float Left,float Top) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPolyline(com.inzoom.comjni.Variant SafeArrayOfPoints) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addShape(int Type,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addTextEffect(int PresetTextEffect,String Text,String FontName,float FontSize,int FontBold,int FontItalic,float Left,float Top) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addTextbox(int Orientation,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder buildFreeform(int EditingType,float X1,float Y1) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeRange range(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public void selectAll() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getBackground() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getDefault() throws com.inzoom.comjni.ComJniException;
}
