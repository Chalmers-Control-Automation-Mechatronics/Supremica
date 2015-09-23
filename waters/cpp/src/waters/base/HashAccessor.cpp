//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include "waters/base/HashAccessor.h"


namespace waters {


//############################################################################
//# Elementary Arithmetic
//############################################################################

int log2(hashindex_t x)
{
  int result = 0;
  if (x > 1) {
    x--;
    do {
      x >>= 1;
      result++;
    } while (x);
  }
  return result;
}


//############################################################################
//# Some Hash Functions
//############################################################################

const uint64_t GOLD64 = 0x9e3779b97f4a7c13LL;

uint64_t* FACTORS = 0;
uint32_t NUM_FACTORS = 0;

void initHashFactors32(uint32_t size)
{
  initHashFactors64((size / 2) + 1);
}

void initHashFactors64(uint32_t size)
{
  if (size > NUM_FACTORS) {
    delete [] FACTORS;
    NUM_FACTORS = size;
    FACTORS = new uint64_t[NUM_FACTORS];
    uint64_t factor = GOLD64;
    for (uint32_t i = 0; i < NUM_FACTORS; i++) {
      FACTORS[i] = factor;
      factor *= GOLD64;
    }
  }
}


uint64_t hashInt(uint64_t key)
{
  return key * GOLD64;
}


uint64_t hashInt32Array(const uint32_t* array, uint32_t size, uint32_t mask0)
{
  register int rest = size;
  register int factor = 0;
  register uint64_t a;
  register uint64_t b;
  register uint64_t result = 0;
  // Handle most of the key ...
  while (rest >= 4) {
    a = (array[0] & mask0) + ((uint64_t) array[1] << 32) + FACTORS[factor++];
    b = array[2] + ((uint64_t) array[3] << 32) + FACTORS[factor++];
    result += a * b;
    array += 4;
    rest -= 4;
    mask0 = ~0;
  }
  // Handle the last 0-3 words ...
  switch (rest) {
  case 3: 
    a = (array[0] & mask0) + ((uint64_t) array[1] << 32) + FACTORS[factor++];
    b = array[2] + FACTORS[factor];
    result += a * b;
    break;
  case 2:
    result +=
      ((array[0] & mask0) + ((uint64_t) array[1] << 32)) * FACTORS[factor];
    break;
  case 1:
    result += (array[0] & mask0) * FACTORS[factor];
    break;
  default:
    break;
  }
  return result;
}


uint64_t hashInt64Array(const uint64_t* array, uint32_t size, uint64_t mask0)
{
  register int rest = size;
  register int factor = 0;
  register uint64_t a;
  register uint64_t b;
  register uint64_t result = 0;
  // Handle most of the key ...
  while (rest >= 2) {
    a = (array[0] & mask0) + FACTORS[factor++];
    b = array[1] + FACTORS[factor++];
    result += a * b;
    array += 2;
    rest -= 2;
    mask0 = ~0;
  }
  // Handle the last 0-1 words ...
  if (rest) {
    result += (array[0] & mask0) * FACTORS[factor];
  }
  return result;
}


uint64_t hashString(const char* key)
{
  const int len = strlen(key);
  register int rest = len;
  register uint64_t result = GOLD64 * len;
  // Handle most of the key ...
  while (rest >= 8) {
    result +=
      (uint64_t) key[0] + ((uint64_t) key[1] << 8) +
      ((uint64_t) key[2] << 16) + ((uint64_t) key[3] << 24) +
      ((uint64_t) key[4] << 32) + ((uint64_t) key[5] << 40) +
      ((uint64_t) key[6] << 48) + ((uint64_t) key[7] << 56);
    result *= GOLD64;
    key += 8;
    rest -= 8;
  }
  // Handle the last 0-7 bytes ...
  switch (rest) {
    // all the case statements fall through ...
  case 7 : result += (uint64_t) key[6] << 48;
  case 6 : result += (uint64_t) key[5] << 40;
  case 5 : result += (uint64_t) key[4] << 32;
  case 4 : result += (uint64_t) key[3] << 24;
  case 3 : result += (uint64_t) key[2] << 16;
  case 2 : result += (uint64_t) key[1] << 8;
  case 1 : result += (uint64_t) key[0];
    // case 0: nothing left to add
  }
  return result;
}


}  /* namespace waters */
