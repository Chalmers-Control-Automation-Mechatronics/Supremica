@project debounce {
@type BITS @int(1..1)
@events {
chg_value {
  @controllable
  @signature {
    {_0 @int}
  }
  @generateCodeFragment
}
reset {
  @controllable
  @signature {
    {_0 @int}
  }
  @generateCodeFragment
}
timer {
  @controllable
  @signature {
    {_0 @int}
  }
  @generateCodeFragment
}
timeout {
  @uncontrollable
  @signature {
    {_0 @int}
  }
  @generateCodeFragment
}
new_value {
  @uncontrollable
  @signature {
    {_0 BITS}
  }
  @noCodeFragment
}
}
@use {
  sensor_and_filter
  debouncing
  timer
}
@instantiation _sensor_and_filter__sensor_and_filter {
  @parameters {
    { _0 @any }
  }
  @rules {
    { I $_0 }
  }
}
<@plant _0 BITS
  sensor_and_filter {
    @cloning {sensor_and_filter}
    @instantiating {_sensor_and_filter__sensor_and_filter $_0}
  }
>
@instantiation _debouncing__debouncing {
  @parameters {
    { _0 @any }
  }
  @rules {
    { I $_0 }
  }
}
<@spec _0 BITS
  debouncing {
    @cloning {debouncing}
    @instantiating {_debouncing__debouncing $_0}
  }
>
@instantiation _timer__timer {
  @parameters {
    { _0 @any }
  }
  @rules {
    { I $_0 }
  }
}
<@plant _0 BITS
  timer {
    @cloning {timer}
    @instantiating {_timer__timer $_0}
  }
>
}

