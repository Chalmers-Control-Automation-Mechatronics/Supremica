package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface FreeformBuilder Implementation
public class FreeformBuilderJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder getFreeformBuilderFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new FreeformBuilderJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder getFreeformBuilderFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new FreeformBuilderJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder getFreeformBuilderFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new FreeformBuilderJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder convertComPtrToFreeformBuilder(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new FreeformBuilderJCW(comPtr,true,releaseComPtr); }
  protected FreeformBuilderJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected FreeformBuilderJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID); }
  protected FreeformBuilderJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected FreeformBuilderJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID); }
  protected FreeformBuilderJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected FreeformBuilderJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID,releaseComPtr);}
  protected FreeformBuilderJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3,float Y3) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant(Y2,false),
      new com.inzoom.comjni.Variant(X3,false),
      new com.inzoom.comjni.Variant(Y3,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant(Y2,false),
      new com.inzoom.comjni.Variant(X3,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant(Y2,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape convertToShape() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.FreeformBuilder.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
