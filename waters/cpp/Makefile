# -*- Makefile -*-
#############################################################################
#
# Makefile for libwaters.so
#
#############################################################################

#############################################################################
# List of sources

export PROJECT  = waters
export SRCDIR   = src
export JNIDIR   = $(SRCDIR)/jni
export TESDIR   = $(SRCDIR)/test
export CPPDIR   = $(SRCDIR)/waters
export OBJDIR   = objects
export LIBDIR   = lib
export BINDIR   = bin

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
export CPP_TEST = $(TESDIR)/HashTest.cpp

export OBJ      = $(CPP:$(SRCDIR)%.cpp=$(OBJDIR)%.o)
export TESTOBJ  = $(CPP_TEST:$(SRCDIR)%.cpp=$(OBJDIR)%.o)

export LIB      = $(LIBDIR)/lib$(PROJECT).so
export TESTAPP  = $(BINDIR)/testapp
export DEP      = dependencies

# commands

export CXX      = g++

# flags

ifdef JAVA_HOME
  JNI_INC       = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
endif

export CFLAGS   = -O2 -g -fPIC -DDEBUG -Wall
export INCDIRS	= -I$(SRCDIR) $(JNI_INC)


#############################################################################
# Main Rules

all::
			@$(MAKE) -f Makefile.dep

test::
			@$(MAKE) -f Makefile.dep test

clean::
			rm -rf $(DEP) $(OBJDIR) $(BINDIR) $(LIBDIR)

