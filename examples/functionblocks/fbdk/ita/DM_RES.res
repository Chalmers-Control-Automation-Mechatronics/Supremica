<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ResourceType SYSTEM "../LibraryElement.dtd" >
<ResourceType xmlns="http://www.holobloc.com/xml/LibraryElement" Name="DM_RES" Comment="Device Management Resource Type" >
  <Identification Standard="61499-2" />
  <VersionInfo Organization="Rockwell Automation" Version="0.0" Author="JHC" Date="2000-05-27" />
  <VarDeclaration Name="ID" Type="WSTRING" />
  <FBNetwork >
    <FB Name="START" Type="E_RESTART" x="88.2353" y="11.7647" />
    <FB Name="MGR_FF" Type="E_SR" x="641.1765" y="11.7647" />
    <FB Name="KERNEL" Type="DM_KRNL" x="1041.1764" y="11.7647" />
    <EventConnections>
      <Connection Source="START.COLD" Destination="MGR_FF.S" dx1="23.5294" dx2="47.0588" dy="-70.5882" />
      <Connection Source="START.WARM" Destination="MGR_FF.S" dx1="41.1765" dx2="76.4706" dy="-188.2353" />
      <Connection Source="START.STOP" Destination="MGR_FF.R" dx1="94.1176" dx2="100" dy="-311.7647" />
      <Connection Source="MGR_FF.EO" Destination="KERNEL.INIT" dx1="11.7647" />
    </EventConnections>
    <DataConnections>
      <Connection Source="MGR_FF.Q" Destination="KERNEL.QI" dx1="52.9412" />
      <Connection Source="ID" Destination="KERNEL.ID" dx1="964.7059" />
    </DataConnections>
  </FBNetwork>
</ResourceType>
