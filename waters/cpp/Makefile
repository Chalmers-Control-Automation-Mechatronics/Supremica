# -*- Makefile -*-
#############################################################################
#
# Makefile for libwaters.so / waters.dll
#
#############################################################################

#############################################################################
# OS dependent

ifeq "$(findstring linux,$(JAVA_ARCH))" "linux"
  export JAVAOS        = linux
  export ARCH          = $(patsubst linux.%,%,$(JAVA_ARCH))
  export EXTRA_CFLAGS  = -fPIC
  export LIB_PREFIX    = lib
  export LIB_SUFFIX    = so
  RMDIR                = rm -rf $(strip $(1))
  RMFILE               = rm -f $(strip $(1))
else
  export JAVAOS        = win32
  export ARCH          = $(patsubst win32.%,%,$(JAVA_ARCH))
  export EXTRA_CFLAGS  = -D_JNI_IMPLEMENTATION_
  export EXTRA_LDFLAGS = -Wl,--kill-at -static-libgcc -static-libstdc++
  export LIB_SUFFIX    = dll
  RMDIR                = $(if $(wildcard $(1)),rd /s /q $(subst /,\,$(1)))
  RMFILE               = $(if $(wildcard $(1)),del $(subst /,\,$(1)))
endif

ifeq "$(ARCH)" "$(JAVA_ARCH)"
  export OS_ARCH         = $(JAVA_ARCH)
else
  export OS_ARCH         = $(JAVAOS).$(ARCH)
endif

ifeq "$(ARCH)" "x86"
  export ARCH_FLAGS      = -m32
endif

ifeq "$(ARCH)" "amd64"
  export ARCH_FLAGS      = -m64
endif

ifeq "$(DEBUG)" "1"
  export OS_ARCH_D       = $(OS_ARCH).debug
  export CFLAGS          = -g -DDEBUG -O2
else
  export OS_ARCH_D       = $(OS_ARCH)
  export CFLAGS          = -O6
endif


#############################################################################
# List of sources

export PROJECT  = waters
export SRCDIR   = src
export JNIDIR   = $(SRCDIR)/jni
export CPPDIR   = $(SRCDIR)/waters
export OBJDIR   = objects/$(OS_ARCH_D)
export LIBDIR   = lib/$(OS_ARCH_D)

export CPP	= $(wildcard $(CPPDIR)/analysis/*.cpp) \
              $(wildcard $(CPPDIR)/base/*.cpp) \
              $(wildcard $(CPPDIR)/des/*.cpp) \
              $(wildcard $(JNIDIR)/cache/*.cpp) \
              $(wildcard $(JNIDIR)/glue/*.cpp)
export HPP	= $(wildcard $(CPPDIR)/base/*.h) \
              $(wildcard $(CPPDIR)/des/*.h) \
              $(wildcard $(CPPDIR)/javah/*.h) \
              $(wildcard $(JNIDIR)/cache/*.h) \
              $(wildcard $(JNIDIR)/glue/*.h)

export OBJ      = $(CPP:$(SRCDIR)%.cpp=$(OBJDIR)%.o)
export LIB      = $(LIBDIR)/$(LIB_PREFIX)$(PROJECT).$(LIB_SUFFIX)

# commands

export CXX      = g++

# flags

ifdef JAVA_HOME
  JNI_INC       = -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/$(JAVAOS)"
endif

export CFLAGS   += -Wall -D__STDC_LIMIT_MACROS $(ARCH_FLAGS) $(EXTRA_CFLAGS)
export INCDIRS	= -I$(SRCDIR) $(JNI_INC)
export LDFLAGS	= $(ARCH_FLAGS) $(EXTRA_LDFLAGS)
export DEP      = dependencies.$(OS_ARCH)


#############################################################################
# Main Rules

all::
			@$(MAKE) -f Makefile.dep

clean::
			-$(call RMFILE,dependencies.*)
			-$(call RMDIR,objects)
			-$(call RMDIR,lib)

