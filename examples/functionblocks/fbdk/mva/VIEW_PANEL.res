<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ResourceType SYSTEM "http://www.holobloc.com/xml/LibraryElement.dtd" >
<ResourceType xmlns="http://www.holobloc.com/xml/LibraryElement" Name="VIEW_PANEL" Comment="Container for View elements" >
  <Identification Standard="61499-1" Classification="HMI" />
  <VersionInfo Organization="Rockwell Automation" Version="0.0" Author="JHC" Date="2002-04-04" Remarks="Derived from PANEL_RESOURCE." />
  <CompilerInfo header="package fb.rt.hmi; import java.awt.*;" >
    <Compiler Language="Java" Vendor="IBM" Product="VisualAge" Version="3.0" />
  </CompilerInfo>
  <VarDeclaration Name="TRACE" Type="BOOL" Comment="True=trace enabled,default is false" />
  <VarDeclaration Name="BKGD" Type="COLOR" InitialValue="COLOR#white" Comment="Background color" />
  <VarDeclaration Name="DIA" Type="UINT" InitialValue="25" Comment="Characteristic dimension,Overrides component DIAs" />
  <VarDeclaration Name="SIZE" Type="UINT" ArraySize="2" InitialValue="[2,2]" Comment="[width,height]in DIA units" />
  <VarDeclaration Name="VERTICAL" Type="BOOL" Comment="1=Column-first,0=Row-first" />
  <FBNetwork >
    <FB Name="START" Type="E_RESTART" x="11.7647" y="11.7647" />
  </FBNetwork>
</ResourceType>
