package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface PropertyTests Implementation
public class PropertyTestsJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests getPropertyTestsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PropertyTestsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests getPropertyTestsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PropertyTestsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests getPropertyTestsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new PropertyTestsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests convertComPtrToPropertyTests(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PropertyTestsJCW(comPtr,true,releaseComPtr); }
  protected PropertyTestsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected PropertyTestsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID); }
  protected PropertyTestsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected PropertyTestsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID); }
  protected PropertyTestsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected PropertyTestsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID,releaseComPtr);}
  protected PropertyTestsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTest getItem(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTest rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.PropertyTestJCW.getPropertyTestFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void add(String Name,int Condition,com.inzoom.comjni.Variant Value,com.inzoom.comjni.Variant SecondValue,int Connector) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(Condition,false),
      createVTblVArg(Value),
      createVTblVArg(SecondValue),
      new com.inzoom.comjni.Variant(Connector,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void add(String Name,int Condition,com.inzoom.comjni.Variant Value,com.inzoom.comjni.Variant SecondValue) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(Condition,false),
      createVTblVArg(Value),
      createVTblVArg(SecondValue),
      new com.inzoom.comjni.Variant((int)1,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void add(String Name,int Condition,com.inzoom.comjni.Variant Value) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(Condition,false),
      createVTblVArg(Value),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)1,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void add(String Name,int Condition) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(Condition,false),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)1,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void remove(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PropertyTests.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
