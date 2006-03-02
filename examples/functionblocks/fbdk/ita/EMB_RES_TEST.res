<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ResourceType SYSTEM "http://www.holobloc.com/xml/LibraryElement.dtd" >
<ResourceType xmlns="http://www.holobloc.com/xml/LibraryElement" Name="EMB_RES_TEST" Comment="A Simple Embedded Resource Type with FB Type list" >
  <Identification Standard="61499-1" Function="Device Management" Description="A simple embedded resource" />
  <VersionInfo Organization="Rockwell Automation" Version="0.0" Author="JHC" Date="2001-01-04" />
  <CompilerInfo header="package fb.rt;" >
    <Compiler Language="Java" Vendor="IBM" Product="VisualAge" Version="3.0" />
  </CompilerInfo>
  <FBTypeName Name="E_RESTART" />
  <FBTypeName Name="E_DELAY" />
  <FBTypeName Name="E_CYCLE" />
  <FBNetwork >
    <FB Name="START" Type="E_RESTART" x="58.8235" y="11.7647" />
  </FBNetwork>
</ResourceType>
