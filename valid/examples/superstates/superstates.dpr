@project superstates {
@events {
stop_VR {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_ZS {
  @controllable
  @generateCodeFragment
}
stop_ZS {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
start_ER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
reset {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
done_ER {
  @controllable
  @generateCodeFragment
}
async {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
sync {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_SER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_CS {
  @controllable
  @generateCodeFragment
}
start_VR {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_ER {
  @controllable
  @generateCodeFragment
}
time_W {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
done_VR {
  @controllable
  @generateCodeFragment
}
stop_ER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
start_ZS {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
done_ZS {
  @controllable
  @generateCodeFragment
}
timeout {
  @uncontrollable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
soft_VR {
  @controllable
  @generateCodeFragment
}
CS_ok {
  @controllable
  @generateCodeFragment
}
done_SER {
  @controllable
  @signature {
    {_0 @any}
  }
  @generateCodeFragment
}
}
@use {
  door
  door_no_superstates
}
@spec door {
  @cloning {door}
}
@spec door_no_superstates {
  @cloning {door_no_superstates}
}
}

