/*
Copyright � 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/
package org.apache.mahout.jet.random.sampling;

import org.apache.mahout.jet.random.engine.RandomEngine;
/**
 * Conveniently computes a stable <i>Simple Random Sample Without Replacement (SRSWOR)</i> subsequence of <tt>n</tt> elements from a given input sequence of <tt>N</tt> elements;
 * Example: Computing a sublist of <tt>n=3</tt> random elements from a list <tt>(1,...,50)</tt> may yield the sublist <tt>(7,13,47)</tt>.
 * The subsequence is guaranteed to be <i>stable</i>, i.e. elements never change position relative to each other.
 * Each element from the <tt>N</tt> elements has the same probability to be included in the <tt>n</tt> chosen elements.
 * This class is a convenience adapter for <tt>RandomSampler</tt> using blocks.
 *
 * @see RandomSampler
 */

/** @deprecated until unit tests are in place.  Until this time, this class/interface is unsupported. */
@Deprecated
public class RandomSamplingAssistant extends org.apache.mahout.matrix.PersistentObject {
  //public class RandomSamplingAssistant extends Object implements java.io.Serializable {
  protected RandomSampler sampler;
  protected final long[] buffer;
  protected int bufferPosition;

  protected long skip;
  protected long n;

  static final int MAX_BUFFER_SIZE = 200;

  /**
   * Constructs a random sampler that samples <tt>n</tt> random elements from an input sequence of <tt>N</tt> elements.
   *
   * @param n               the total number of elements to choose (must be &gt;= 0).
   * @param N               number of elements to choose from (must be &gt;= n).
   * @param randomGenerator a random number generator. Set this parameter to <tt>null</tt> to use the default random
   *                        number generator.
   */
  public RandomSamplingAssistant(long n, long N, RandomEngine randomGenerator) {
    this.n = n;
    this.sampler = new RandomSampler(n, N, 0, randomGenerator);
    this.buffer = new long[(int) Math.min(n, MAX_BUFFER_SIZE)];
    if (n > 0) {
      this.buffer[0] = -1;
    } // start with the right offset

    fetchNextBlock();
  }

  /** Returns a deep copy of the receiver. */
  @Override
  public Object clone() {
    RandomSamplingAssistant copy = (RandomSamplingAssistant) super.clone();
    copy.sampler = (RandomSampler) this.sampler.clone();
    return copy;
  }

  /** Not yet commented. */
  protected void fetchNextBlock() {
    if (n > 0) {
      long last = buffer[bufferPosition];
      sampler.nextBlock((int) Math.min(n, MAX_BUFFER_SIZE), buffer, 0);
      skip = buffer[0] - last - 1;
      bufferPosition = 0;
    }
  }

  /** Returns the used random generator. */
  public RandomEngine getRandomGenerator() {
    return this.sampler.my_RandomGenerator;
  }

  /** Just shows how this class can be used; samples n elements from and int[] array. */
  public static int[] sampleArray(int n, int[] elements) {
    RandomSamplingAssistant assistant = new RandomSamplingAssistant(n, elements.length, null);
    int[] sample = new int[n];
    int j = 0;
    int length = elements.length;
    for (int i = 0; i < length; i++) {
      if (assistant.sampleNextElement()) {
        sample[j++] = elements[i];
      }
    }
    return sample;
  }

  /**
   * Returns whether the next element of the input sequence shall be sampled (picked) or not.
   *
   * @return <tt>true</tt> if the next element shall be sampled (picked), <tt>false</tt> otherwise.
   */
  public boolean sampleNextElement() {
    if (n == 0) {
      return false;
    } //reject
    if (skip-- > 0) {
      return false;
    } //reject

    //accept
    n--;
    if (bufferPosition < buffer.length - 1) {
      skip = buffer[bufferPosition + 1] - buffer[bufferPosition++];
      --skip;
    } else {
      fetchNextBlock();
    }

    return true;
  }

}
