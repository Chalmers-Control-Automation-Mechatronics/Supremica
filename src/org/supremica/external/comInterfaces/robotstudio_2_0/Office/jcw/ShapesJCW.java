package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface Shapes Implementation
public class ShapesJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes getShapesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShapesJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes getShapesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShapesJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes getShapesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ShapesJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes convertComPtrToShapes(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShapesJCW(comPtr,true,releaseComPtr); }
  protected ShapesJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ShapesJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID); }
  protected ShapesJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ShapesJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID); }
  protected ShapesJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ShapesJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID,releaseComPtr);}
  protected ShapesJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addCallout(int Type,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant(Width,false),
      new com.inzoom.comjni.Variant(Height,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addConnector(int Type,float BeginX,float BeginY,float EndX,float EndY) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false),
      new com.inzoom.comjni.Variant(BeginX,false),
      new com.inzoom.comjni.Variant(BeginY,false),
      new com.inzoom.comjni.Variant(EndX,false),
      new com.inzoom.comjni.Variant(EndY,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addCurve(com.inzoom.comjni.Variant SafeArrayOfPoints) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(SafeArrayOfPoints),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addLabel(int Orientation,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Orientation,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant(Width,false),
      new com.inzoom.comjni.Variant(Height,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addLine(float BeginX,float BeginY,float EndX,float EndY) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(BeginX,false),
      new com.inzoom.comjni.Variant(BeginY,false),
      new com.inzoom.comjni.Variant(EndX,false),
      new com.inzoom.comjni.Variant(EndY,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPicture(String FileName,int LinkToFile,int SaveWithDocument,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(LinkToFile,false),
      new com.inzoom.comjni.Variant(SaveWithDocument,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant(Width,false),
      new com.inzoom.comjni.Variant(Height,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[7].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPicture(String FileName,int LinkToFile,int SaveWithDocument,float Left,float Top,float Width) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(LinkToFile,false),
      new com.inzoom.comjni.Variant(SaveWithDocument,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant(Width,false),
      new com.inzoom.comjni.Variant((float)-1.0,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[7].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPicture(String FileName,int LinkToFile,int SaveWithDocument,float Left,float Top) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(LinkToFile,false),
      new com.inzoom.comjni.Variant(SaveWithDocument,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant((float)-1.0,false),
      new com.inzoom.comjni.Variant((float)-1.0,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[7].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addPolyline(com.inzoom.comjni.Variant SafeArrayOfPoints) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(SafeArrayOfPoints),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addShape(int Type,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant(Width,false),
      new com.inzoom.comjni.Variant(Height,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addTextEffect(int PresetTextEffect,String Text,String FontName,float FontSize,int FontBold,int FontItalic,float Left,float Top) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(PresetTextEffect,false),
      new com.inzoom.comjni.Variant(Text,false),
      new com.inzoom.comjni.Variant(FontName,false),
      new com.inzoom.comjni.Variant(FontSize,false),
      new com.inzoom.comjni.Variant(FontBold,false),
      new com.inzoom.comjni.Variant(FontItalic,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[8].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape addTextbox(int Orientation,float Left,float Top,float Width,float Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Orientation,false),
      new com.inzoom.comjni.Variant(Left,false),
      new com.inzoom.comjni.Variant(Top,false),
      new com.inzoom.comjni.Variant(Width,false),
      new com.inzoom.comjni.Variant(Height,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder buildFreeform(int EditingType,float X1,float Y1) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.FreeformBuilderJCW.getFreeformBuilderFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeRange range(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeRange rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeRangeJCW.getShapeRangeFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void selectAll() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getBackground() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getDefault() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shapes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
