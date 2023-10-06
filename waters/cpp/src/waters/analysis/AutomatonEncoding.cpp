//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

#include <new>

#ifdef DEBUG
#include <iostream>
#endif /* DEBUG */

#include <jni.h>
#include <stdlib.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/JavaString.h"
#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/MapGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"

#include "waters/analysis/AutomatonEncoding.h"


namespace waters {

//############################################################################
//# class AutomatonRecordHashAccessor
//############################################################################

//############################################################################
//# AutomatonRecordHashAccessor: Hash Methods

uint64_t AutomatonRecordHashAccessor::
hash(intptr_t key)
  const
{
  const jni::AutomatonGlue* aut = (const jni::AutomatonGlue*) key;
  return aut->hashCode();
}


bool AutomatonRecordHashAccessor::
equals(intptr_t key1, intptr_t key2)
  const
{
  const jni::AutomatonGlue* aut1 = (const jni::AutomatonGlue*) key1;
  const jni::AutomatonGlue* aut2 = (const jni::AutomatonGlue*) key2;
  return aut1->equals(aut2);
}


intptr_t AutomatonRecordHashAccessor::
getKey(intptr_t value)
  const
{
  const AutomatonRecord* record = (const AutomatonRecord*) value;
  return (intptr_t) &record->getJavaAutomaton();
}



//############################################################################
//# class AutomatonRecord
//############################################################################

//############################################################################
//# AutomatonRecord: Class Variables

const AutomatonRecordHashAccessor AutomatonRecord::theHashAccessor;


//############################################################################
//# AutomatonRecord: Constructors & Destructors

AutomatonRecord::
AutomatonRecord(const jni::AutomatonGlue& aut,
                bool plant,
                const jni::EventGlue& alpha,
                const jni::EventGlue& omega,
                jni::ClassCache* cache)
  : mJavaAutomaton(aut),
    mIsPlant(plant),
    mWordIndex(0),
    mShift(0),
    mBitMask(0),
    mDumpStates(0)
{
  const jni::SetGlue states = aut.getStatesGlue(cache);
  mNumStates = states.size();
  mNumBits = log2(mNumStates);
  mJavaStates = (jni::StateGlue*) malloc(mNumStates * sizeof(jni::StateGlue));
  const jni::CollectionGlue events = aut.getEventsGlue(cache);
  if (omega.isNull()) {
    initNonMarking(cache, false);
  } else if (events.contains(&omega)) {
    if (alpha.isNull() || !events.contains(&alpha)) {
      mFirstPreMarkedState = 0;
      mEndPreMarkedStates = mNumStates;
      initMarking(omega, mFirstMarkedState, cache);
    } else {
      initMarking(alpha, omega, cache);
    }
  } else {
    if (alpha.isNull() || !events.contains(&alpha)) {
      initNonMarking(cache, true);
    } else {
      mFirstMarkedState = 0;
      mEndPreMarkedStates = mNumStates;
      initMarking(alpha, mFirstPreMarkedState, cache);
    }
  }
}

AutomatonRecord::
~AutomatonRecord()
{
  for (uint32_t code = 0; code < mNumStates; code++) {
    mJavaStates[code].jni::StateGlue::~StateGlue();
  }
  free(mJavaStates);
  delete [] mDumpStates;
}


//############################################################################
//# AutomatonRecord: Simple Access

uint32_t AutomatonRecord::
getNumberOfInitialStates()
  const
{
  return
    mEndInitialStates1 - mFirstInitialState1 +
    mEndInitialStates2 - mFirstInitialState2;
}

jni::JavaString AutomatonRecord::
getName()
  const
{
  const jni::ClassGlue* cls = mJavaAutomaton.getClass();
  JNIEnv* env = cls->getEnvironment();
  jstring jname = mJavaAutomaton.getName();
  return jni::JavaString(env, jname);
}

jni::JavaString AutomatonRecord::
getStateName(uint32_t code)
  const
{
  const jni::StateGlue& state = mJavaStates[code];
  const jni::ClassGlue* cls = state.getClass();
  JNIEnv* env = cls->getEnvironment();
  jstring jname = state.getName();
  return jni::JavaString(env, jname);
}

const jni::StateGlue& AutomatonRecord::
getJavaState(uint32_t code)
  const
{
  return mJavaStates[code];
}


//############################################################################
//# AutomatonRecord: Comparing

int AutomatonRecord::
compareTo(const AutomatonRecord* partner)
  const
{
  const int result = partner->mNumStates - mNumStates;
  if (result != 0) {
    return result;
  } else {
    return mJavaAutomaton.compareTo(&partner->mJavaAutomaton);
  }
}

int AutomatonRecord::
compare(const void* elem1, const void* elem2)
{
  const AutomatonRecord* val1 = *((const AutomatonRecord**) elem1);
  const AutomatonRecord* val2 = *((const AutomatonRecord**) elem2);
  return val1->compareTo(val2);
}

int AutomatonRecord::
compareToByMarking(const AutomatonRecord* partner)
  const
{
  float prob1 = (float) getNumberOfMarkedStates() / (float) mNumStates;
  float prob2 = (float) partner->getNumberOfMarkedStates() /
                (float) partner->mNumStates;
  if (prob1 < prob2) {
    return -1;
  } else if (prob1 > prob2) {
    return 1;
  } else {
    return compareTo(partner);
  }
}

int AutomatonRecord::
compareByMarking(const void* elem1, const void* elem2)
{
  const AutomatonRecord* val1 = *((const AutomatonRecord**) elem1);
  const AutomatonRecord* val2 = *((const AutomatonRecord**) elem2);
  return val1->compareToByMarking(val2);
}

int AutomatonRecord::
compareToByPreMarking(const AutomatonRecord* partner)
  const
{
  float prob1 = (float) getNumberOfPreMarkedStates() / (float) mNumStates;
  float prob2 = (float) partner->getNumberOfPreMarkedStates() /
                (float) partner->mNumStates;
  if (prob1 < prob2) {
    return -1;
  } else if (prob1 > prob2) {
    return 1;
  } else {
    return compareTo(partner);
  }
}

int AutomatonRecord::
compareByPreMarking(const void* elem1, const void* elem2)
{
  const AutomatonRecord* val1 = *((const AutomatonRecord**) elem1);
  const AutomatonRecord* val2 = *((const AutomatonRecord**) elem2);
  return val1->compareToByPreMarking(val2);
}


//############################################################################
//# AutomatonRecord: Setting up

void AutomatonRecord::
allocate(int wordindex, int shift)
{
  mWordIndex = wordindex;
  mShift = shift;
  mBitMask = ((1 << mNumBits) - 1) << shift;
}

Int32PtrHashTable<const jni::StateGlue*,uint32_t>* AutomatonRecord::
createStateMap()
{
  Int32PtrHashTable<const jni::StateGlue*,uint32_t>* statemap =
    new Int32PtrHashTable<const jni::StateGlue*,uint32_t>(this, mNumStates);
  for (uint32_t code = 0; code < mNumStates; code++) {
    statemap->add(code);
  }
  return statemap;
}

void AutomatonRecord::
deleteStateMap(Int32PtrHashTable<const jni::StateGlue*,uint32_t>* statemap)
{
  delete statemap;
}


//############################################################################
//# AutomatonRecord: Auxiliary Methods

void AutomatonRecord::
initNonMarking(jni::ClassCache* cache, bool allmarked)
{
  const jni::SetGlue states = mJavaAutomaton.getStatesGlue(cache);
  const jni::IteratorGlue iter = states.iteratorGlue(cache);
  uint32_t nextinit = 0;
  uint32_t nextnoninit = mNumStates - 1;
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::StateGlue state(javaobject, cache);
    uint32_t code;
    if (state.isInitial()) {
      code = nextinit++;
    } else {
      code = nextnoninit--;
    }
    new (&mJavaStates[code]) jni::StateGlue(state);
  }
  mFirstInitialState1 = 0;
  mEndInitialStates1 = mFirstInitialState2 = mEndInitialStates2 = nextinit;
  mFirstMarkedState = allmarked ? 0 : mNumStates;
  mFirstPreMarkedState = 0;
  mEndPreMarkedStates = mNumStates;
}

void AutomatonRecord::
initMarking(const jni::EventGlue& marking,
            uint32_t& firstmarkedref,
            jni::ClassCache* cache)
{
  static const int CAT_COUNT = 4;
  int cat;
  uint32_t catindex[CAT_COUNT];
  for (cat = 0; cat < CAT_COUNT; cat++) {
    catindex[cat] = 0;
  }
  const jni::SetGlue states = mJavaAutomaton.getStatesGlue(cache);
  const jni::IteratorGlue iter1 = states.iteratorGlue(cache);
  while (iter1.hasNext()) {
    jobject javaobject = iter1.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, marking, cache);
    catindex[cat]++;
  }
  uint32_t start = 0;
  for (cat = 0; cat < CAT_COUNT; cat++) {
    uint32_t next = start + catindex[cat];
    catindex[cat] = start;
    start = next;
  }
  const jni::IteratorGlue iter2 = states.iteratorGlue(cache);
  while (iter2.hasNext()) {
    jobject javaobject = iter2.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, marking, cache);
    const uint32_t code = catindex[cat]++;
    new (&mJavaStates[code]) jni::StateGlue(state);
  }
  mFirstInitialState1 = catindex[0];
  mEndInitialStates1 = mFirstInitialState2 = mEndInitialStates2 = catindex[2];
  firstmarkedref = catindex[1];
}

void AutomatonRecord::
initMarking(const jni::EventGlue& alpha,
            const jni::EventGlue& omega,
            jni::ClassCache* cache)
{
  static const int CAT_COUNT = 8;
  int cat;
  uint32_t catindex[CAT_COUNT];
  for (cat = 0; cat < CAT_COUNT; cat++) {
    catindex[cat] = 0;
  }
  const jni::SetGlue states = mJavaAutomaton.getStatesGlue(cache);
  const jni::IteratorGlue iter1 = states.iteratorGlue(cache);
  while (iter1.hasNext()) {
    jobject javaobject = iter1.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, alpha, omega, cache);
    catindex[cat]++;
  }
  uint32_t start = 0;
  for (cat = 0; cat < CAT_COUNT; cat++) {
    uint32_t next = start + catindex[cat];
    catindex[cat] = start;
    start = next;
  }
  const jni::IteratorGlue iter2 = states.iteratorGlue(cache);
  while (iter2.hasNext()) {
    jobject javaobject = iter2.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, alpha, omega, cache);
    const uint32_t code = catindex[cat]++;
    new (&mJavaStates[code]) jni::StateGlue(state);
  }
  if (catindex[0] == catindex[2]) {
    mFirstInitialState1 = catindex[4];
    mEndInitialStates1 = mFirstInitialState2 = mEndInitialStates2 =
      catindex[6];
  } else {
    mFirstInitialState1 = catindex[0];
    mEndInitialStates1 = catindex[2]; 
    mFirstInitialState2 = catindex[4];
    mEndInitialStates2 = catindex[6];
  }
  mFirstPreMarkedState = catindex[1];
  mEndPreMarkedStates = catindex[5];
  mFirstMarkedState = catindex[3];
}

int AutomatonRecord::
getCategory(const jni::StateGlue& state,
            const jni::EventGlue& marking,
            jni::ClassCache* cache)
{
  const int init = state.isInitial() ? 1 : 0;
  jni::CollectionGlue props = state.getPropositionsGlue(cache);
  const int marked = props.contains(&marking) ? 3 : 0;
  return init ^ marked;
}

int AutomatonRecord::
getCategory(const jni::StateGlue& state,
            const jni::EventGlue& alpha,
            const jni::EventGlue& omega,
            jni::ClassCache* cache)
{
  const int init = state.isInitial() ? 1 : 0;
  jni::CollectionGlue props = state.getPropositionsGlue(cache);
  const int hasalpha = props.contains(&alpha) ? 3 : 0;
  const int lowers = init ^ hasalpha;
  return props.contains(&omega) ? 7 - lowers : lowers;
}

uint32_t AutomatonRecord::
setupDumpStates(const bool* dumpStatus)
{
  delete [] mDumpStates;
  uint32_t count = 0;
  for (uint32_t s = 0; s < mNumStates; s++) {
    if (dumpStatus[s]) {
      count++;
    }
  }
  if (count == 0) {
    mDumpStates = 0;
  } else {
    mDumpStates = new uint32_t[count + 1];
    mDumpStates[0] = count;
    int d = 1;
    for (uint32_t s = 0; s < mNumStates; s++) {
      if (dumpStatus[s]) {
        mDumpStates[d++] = s;
      }
    }
  }
  return count;
}


//############################################################################
//# AutomatonRecord: Hash Methods (for states!!!)

uint64_t AutomatonRecord::
hash(intptr_t key)
  const
{
  const jni::StateGlue* state = (const jni::StateGlue*) key;
  const int javahash = state->hashCode();
  return waters::hashInt(javahash);
}


bool AutomatonRecord::
equals(intptr_t key1, intptr_t key2)
  const
{
  const jni::StateGlue* state1 = (const jni::StateGlue*) key1;
  const jni::StateGlue* state2 = (const jni::StateGlue*) key2;
  return state1->equals(state2);
}


intptr_t AutomatonRecord::
getKey(int32_t value)
  const
{
  return (intptr_t) &mJavaStates[value];
}


//############################################################################
//# class AutomatonEncoding
//############################################################################

//############################################################################
//# AutomatonEncoding: Constructors & Destructors

AutomatonEncoding::
AutomatonEncoding(const jni::ProductDESGlue& des,
                  const jni::KindTranslatorGlue& translator,
                  const jni::EventGlue& alpha,
                  const jni::EventGlue& omega,
                  jni::ClassCache* cache,
                  int numtags)
  : mNumTags(numtags),
    mIsTriviallyNonblocking(true),
    mIsTriviallyBlocking(false),
    mMarkingTestRecords(0),
    mNumMarkingTestRecords(0),
    mPreMarkingTestRecords(0),
    mNumPreMarkingTestRecords(0)
{
  bool hasinit = true;
  bool allmarked = true;
  bool allpremarked = true;
  bool neverpre = false;
  int totalbits = numtags;
  int a, w;

  // create records ...
  const jni::SetGlue automata = des.getAutomataGlue(cache);
  const jni::IteratorGlue iter = automata.iteratorGlue(cache);
  const int numautomata = automata.size();
  AutomatonRecord** records = new AutomatonRecord*[numautomata];
  a = 0;
  while(iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::AutomatonGlue aut(javaobject, cache);
    bool plant;
    switch (translator.getComponentKindGlue(&aut, cache)) {
    case jni::ComponentKind_PLANT:
      plant = true;
      break;
    case jni::ComponentKind_SPEC:
      plant = false;
      break;
    default:
      continue;
    }
    AutomatonRecord* record =
      new AutomatonRecord(aut, plant, alpha, omega, cache);
    totalbits += record->getNumberOfBits();
    records[a++] = record;
    hasinit &= record->getNumberOfInitialStates() > 0;
    allmarked &= record->isAllMarked();
    allpremarked &= record->isAllPreMarked();
    neverpre |= record->getNumberOfPreMarkedStates() == 0;
    mIsTriviallyBlocking |= record->getNumberOfMarkedStates() == 0;
  }
  mIsTriviallyBlocking &= allpremarked && hasinit;
  mIsTriviallyNonblocking &= allmarked || neverpre;
  mNumRecords = a;

  // sort records ...
  qsort(records, mNumRecords, sizeof(AutomatonRecord*),
        AutomatonRecord::compare);

  // allocate bits ...
  int maxwords = totalbits / 16 + 1;
  int* used = new int[maxwords];
  used[0] = numtags;
  for (w = 1; w < maxwords; w++) {
    used[w] = 0;
  }
  mEncodingSize = 0;
  for (a = 0; a < mNumRecords; a++) {
    AutomatonRecord* record = records[a];
    int numbits = record->getNumberOfBits();
    for (w = 0; used[w] + numbits > 32; w++);
    int shift = used[w];
    record->allocate(w, shift);
    used[w] += numbits;
    if (w == mEncodingSize) {
      mEncodingSize++;
    }
  }

  // rearrange and store records ...
  mWordStop = new int[mEncodingSize];
  for (w = 0; w < mEncodingSize; w++) {
    mWordStop[w] = 0;
  }
  for (a = 0; a < mNumRecords; a++) {
    w = records[a]->getWordIndex();
    mWordStop[w]++;
  }
  int prev = 0;
  for (w = 0; w < mEncodingSize; w++) {
    used[w] = prev;
    prev = (mWordStop[w] += prev);
  }
  mAutomatonRecords = new AutomatonRecord*[mNumRecords];
  for (a = 0; a < mNumRecords; a++) {
    AutomatonRecord* record = records[a];
    w = record->getWordIndex();
    const int index = used[w]++;
    mAutomatonRecords[index] = record;
    record->setAutomatonIndex(index);
  }

  // setup marking test ...
  if (!omega.isNull()) {
    setupMarkingTest();
  }

  // clean up ...
  delete[] records;
  delete[] used;
}

AutomatonEncoding::
~AutomatonEncoding()
{
  for (int a = 0; a < mNumRecords; a++) {
    delete mAutomatonRecords[a];
  }
  delete [] mAutomatonRecords;
  delete [] mWordStop;
  delete [] mMarkingTestRecords;
  delete [] mPreMarkingTestRecords;
}


//############################################################################
//# AutomatonEncoding: Simple Access

uint32_t AutomatonEncoding::
getInverseTagMask()
  const
{
  return ~((1 << mNumTags) - 1);
}

bool AutomatonEncoding::
hasSpecs()
  const
{
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    if (!record->isPlant()) {
      return true;
    }
  }
  return false;
}

int AutomatonEncoding::
getNumberOfNondeterministicInitialAutomata()
  const
{
  int ndcount = 0;
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    if (record->getNumberOfInitialStates() > 1) {
      ndcount++;
    }
  }
  return ndcount;
}

int AutomatonEncoding::
getNumberOfEncodedBits()
  const
{
  int bits = 0;
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    bits += record->getNumberOfBits();
  }
  return bits;
}


//############################################################################
//# AutomatonEncoding: Encoding and Decoding

void AutomatonEncoding::
encode(const uint32_t* decoded, uint32_t* encoded)
  const
{
  int a = 0;
  int w = 0;
  for (; w < mEncodingSize; w++) {
    const int end = mWordStop[w];
    uint32_t word = 0;
    for (; a < end; a++) {
      const AutomatonRecord* record = mAutomatonRecords[a];
      const int shift = record->getShift();
      const uint32_t code = decoded[a];
      word |= (code << shift);
    }
    encoded[w] = word;
  }
  for (; w < mEncodingSize; w++) {
    encoded[w] = 0;
  }
}

void AutomatonEncoding::
decode(const uint32_t* encoded, uint32_t* decoded)
  const
{
  int a = 0;
  for (int w = 0; w < mEncodingSize; w++) {
    const int end = mWordStop[w];
    const uint32_t word = encoded[w];
    for (; a < end; a++) {
      const AutomatonRecord* record = mAutomatonRecords[a];
      const int shift = record->getShift();
      const uint32_t mask = record->getBitMask();
      decoded[a] = (word & mask) >> shift;
    }
  }
}

uint32_t AutomatonEncoding::
get(const uint32_t* encoded, int index)
  const
{
  const AutomatonRecord* record = mAutomatonRecords[index];
  const int w = record->getWordIndex();
  const uint32_t word = encoded[w];
  const int shift = record->getShift();
  const uint32_t mask = record->getBitMask();
  return (word & mask) >> shift;
}

void AutomatonEncoding::
set(uint32_t* encoded, int index, uint32_t code)
  const
{
  const AutomatonRecord* record = mAutomatonRecords[index];
  const int w = record->getWordIndex();
  const int shift = record->getShift();
  const uint32_t mask = record->getBitMask();
  encoded[w] = (encoded[w] & ~mask) | (code << shift);
}

void AutomatonEncoding::
shift(uint32_t* decoded)
  const
{
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    const int shift = record->getShift();
    decoded[a] <<= shift;
  }
}


//############################################################################
//# AutomatonEncoding: Marking

bool AutomatonEncoding::
isMarkedStateTuplePacked(const uint32_t* encoded)
  const
{
  for (int a = 0; a < mNumMarkingTestRecords; a++) {
    const AutomatonRecord* record = mMarkingTestRecords[a];
    const uint32_t index = record->getAutomatonIndex();
    const uint32_t state = get(encoded, index);
    if (!record->isMarkedState(state)) {
      return false;
    }
  }
  return true;
}

bool AutomatonEncoding::
isMarkedStateTuple(const uint32_t* decoded)
  const
{
  for (int a = 0; a < mNumMarkingTestRecords; a++) {
    const AutomatonRecord* record = mMarkingTestRecords[a];
    const uint32_t index = record->getAutomatonIndex();
    if (!record->isMarkedState(decoded[index])) {
      return false;
    }
  }
  return true;
}

bool AutomatonEncoding::
isPreMarkedStateTuplePacked(const uint32_t* encoded)
  const
{
  for (int a = 0; a < mNumPreMarkingTestRecords; a++) {
    const AutomatonRecord* record = mPreMarkingTestRecords[a];
    const uint32_t index = record->getAutomatonIndex();
    const uint32_t state = get(encoded, index);
    if (!record->isPreMarkedState(state)) {
      return false;
    }
  }
  return true;
}

bool AutomatonEncoding::
isPreMarkedStateTuple(const uint32_t* decoded)
  const
{
  for (int a = 0; a < mNumPreMarkingTestRecords; a++) {
    const AutomatonRecord* record = mPreMarkingTestRecords[a];
    const uint32_t index = record->getAutomatonIndex();
    if (!record->isPreMarkedState(decoded[index])) {
      return false;
    }
  }
  return true;
}


//############################################################################
//# AutomatonEncoding: Masking

void AutomatonEncoding::
initMask(uint32_t* mask)
  const
{
  for (int w = 0; w < mEncodingSize; w++) {
    mask[w] = 0;
  }
}

void AutomatonEncoding::
addToMask(uint32_t* mask, int index)
  const
{
  const AutomatonRecord* record = mAutomatonRecords[index];
  const int w = record->getWordIndex();
  const uint32_t imask = record->getBitMask();
  mask[w] |= imask;
}

bool AutomatonEncoding::
equals(const uint32_t* encoded1, const uint32_t* encoded2, const uint32_t* nmask)
  const
{
  for (int w = 0; w < mEncodingSize; w++) {
    if ((encoded1[w] ^ encoded2[w]) & ~nmask[w]) {
      return false;
    }
  }
  return true;
}


//############################################################################
//# AutomatonEncoding: Trace Computation

void AutomatonEncoding::
storeNondeterministicInitialStates(const uint32_t* tuple,
                                   const jni::MapGlue& statemap)
const
{
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* aut = mAutomatonRecords[a];
    if (aut->getNumberOfInitialStates() > 1) {
      const uint32_t code = tuple[a];
      const jni::StateGlue& stateglue = aut->getJavaState(code);
      const jni::AutomatonGlue& autglue = aut->getJavaAutomaton();
      statemap.put(&autglue, &stateglue);
    }
  }
}


//############################################################################
//# AutomatonEncoding: Debug Output

#ifdef DEBUG

void AutomatonEncoding::
dump()
  const
{
  std::cerr << "ENCODING DUMP:" << std::endl;
  std::cerr << "  Number of automata: " << mNumRecords << std::endl;
  std::cerr << "  Number of words: " << mEncodingSize << std::endl;
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    const jni::JavaString name = record->getName();
    const int numstates = record->getNumberOfStates();
    const int numbits = record->getNumberOfBits();
    const int shift = record->getShift();
    const int w = record->getWordIndex();
    std::cerr << "  #" << a << ": " << (const char*) name << " (" << numstates
              << " states) : " << w << ":" << shift << ":" << numbits
              << std::endl;
  }
  std::cerr << "END OF ENCODING DUMP" << std::endl;
}

void AutomatonEncoding::
dumpEncodedState(const uint32_t* encoded)
  const
{
  std::cerr << '(';
  for (int w = 0; w < mEncodingSize; w++) {
    if (w > 0) {
      std::cerr << ',';
    }
    std::cerr << encoded[w];
  }
  std::cerr << ") = ";
  uint32_t* decoded = new uint32_t[mNumRecords];
  decode(encoded, decoded);
  dumpDecodedState(decoded);
  delete [] decoded;
}

void AutomatonEncoding::
dumpDecodedState(const uint32_t* decoded)
  const
{
  std::cerr << '(';
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    const uint32_t code = decoded[a];
    const jni::JavaString name = record->getStateName(code);
    if (a > 0) {
      std::cerr << ',';
    }
    std::cerr << (const char*) name;
  }
  std::cerr << ')' << std::endl;
}

#endif /* DEBUG */


//############################################################################
//# AutomatonEncoding: Auxiliary Methods

void AutomatonEncoding::
setupMarkingTest()
{
  if (mMarkingTestRecords == 0) {
    int a;
    for (a = 0; a < mNumRecords; a++) {
      AutomatonRecord* record = mAutomatonRecords[a];
      if (!record->isAllMarked()) {
        mNumMarkingTestRecords++;
      }
    }
    mMarkingTestRecords =
      new const AutomatonRecord*[mNumMarkingTestRecords];
    mNumMarkingTestRecords = 0;
    for (a = 0; a < mNumRecords; a++) {
      const AutomatonRecord* record = mAutomatonRecords[a];
      if (!record->isAllMarked()) {
        mMarkingTestRecords[mNumMarkingTestRecords++] = record;
      }
    }
    qsort(mMarkingTestRecords, mNumMarkingTestRecords,
          sizeof(AutomatonRecord*), AutomatonRecord::compareByMarking);
  }
  if (mPreMarkingTestRecords == 0) {
    int a;
    for (a = 0; a < mNumRecords; a++) {
      AutomatonRecord* record = mAutomatonRecords[a];
      if (!record->isAllPreMarked()) {
        mNumPreMarkingTestRecords++;
      }
    }
    mPreMarkingTestRecords =
      new const AutomatonRecord*[mNumPreMarkingTestRecords];
    mNumPreMarkingTestRecords = 0;
    for (a = 0; a < mNumRecords; a++) {
      const AutomatonRecord* record = mAutomatonRecords[a];
      if (!record->isAllPreMarked()) {
        mPreMarkingTestRecords[mNumPreMarkingTestRecords++] = record;
      }
    }
    qsort(mPreMarkingTestRecords, mNumPreMarkingTestRecords,
          sizeof(AutomatonRecord*), AutomatonRecord::compareByPreMarking);
  }
}


}  /* namespace waters */
