@project TwoMachinesOneBuffer {
@type MACHINES @int(1..2)
@events {
Repair {
  @controllable
  @signature {
    {_0 MACHINES}
  }
  @generateCodeFragment
}
Start {
  @controllable
  @signature {
    {_0 MACHINES}
  }
  @generateCodeFragment
}
Finish {
  @uncontrollable
  @signature {
    {_0 MACHINES}
  }
  @generateCodeFragment
}
Break {
  @uncontrollable
  @signature {
    {_0 MACHINES}
  }
  @generateCodeFragment
}
}
@use {
  machine
  modifiedBufferSpec
}
@instantiation _machine__machine {
  @parameters {
    { _0 @any }
  }
  @rules {
    { mach_num $_0 }
  }
}
<@plant _0 MACHINES
  machine {
    @cloning {machine}
    @instantiating {_machine__machine $_0}
  }
>
@spec modifiedBufferSpec {
  @cloning {modifiedBufferSpec}
}
}

