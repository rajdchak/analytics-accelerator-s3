/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.amazon.s3.analyticsaccelerator.io.physical.prefetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import software.amazon.s3.analyticsaccelerator.io.physical.data.Block;
import software.amazon.s3.analyticsaccelerator.io.physical.data.BlockStore;

public class SequentialPatternDetectorTest {

  @Test
  public void test__isSequentialRead__falseAtPositionZero() {
    // Given: a pattern detector
    BlockStore blockStore = mock(BlockStore.class);
    SequentialPatternDetector sequentialPatternDetector = new SequentialPatternDetector(blockStore);

    // When: asked about position 0
    boolean isSequential = sequentialPatternDetector.isSequentialRead(0);

    // Then: answer should always be FALSE
    assertFalse(isSequential);
  }

  @Test
  public void test__isSequentialRead__trueAfterExistingBlock() {
    // Given: a pattern detector with a block store already containing a block
    BlockStore blockStore = mock(BlockStore.class);
    when(blockStore.getBlock(100)).thenReturn(Optional.of(mock(Block.class)));
    SequentialPatternDetector sequentialPatternDetector = new SequentialPatternDetector(blockStore);

    // When: asked if byte 101 would be a sequential read
    boolean isSequential = sequentialPatternDetector.isSequentialRead(101);

    // Then: answer is TRUE
    assertTrue(isSequential);
  }

  @Test
  public void test__getGeneration__zeroAtPositionZero() {
    // Given: a pattern detector
    BlockStore blockStore = mock(BlockStore.class);
    SequentialPatternDetector sequentialPatternDetector = new SequentialPatternDetector(blockStore);

    // When: position zero's generation is requested
    long generation = sequentialPatternDetector.getGeneration(0);

    // Then: answer is ZERO
    assertEquals(0, generation);
  }

  @Test
  public void test__getGeneration__oneMoreThanPreviousBlock() {
    // Given: a pattern detector with a block store already containing a block
    final long GEN = 7;
    Block mockBlock = mock(Block.class);
    when(mockBlock.getGeneration()).thenReturn(GEN);
    BlockStore mockBlockStore = mock(BlockStore.class);
    when(mockBlockStore.getBlock(100)).thenReturn(Optional.of(mockBlock));
    SequentialPatternDetector sequentialPatternDetector =
        new SequentialPatternDetector(mockBlockStore);

    // When: next position's generation is requested
    long generation = sequentialPatternDetector.getGeneration(101);

    // Then: generation is one higher than the previous block's generation
    assertEquals(GEN + 1, generation);
  }

  @Test
  public void test__getGeneration__zeroWhenNotSequentialRead() {
    // Given: a pattern detector with an empty block store
    final long GEN = 7;
    Block mockBlock = mock(Block.class);
    when(mockBlock.getGeneration()).thenReturn(GEN);
    BlockStore mockBlockStore = mock(BlockStore.class);
    when(mockBlockStore.getBlock(100)).thenReturn(Optional.empty());
    SequentialPatternDetector sequentialPatternDetector =
        new SequentialPatternDetector(mockBlockStore);

    // When: any position's generation is requested
    long generation = sequentialPatternDetector.getGeneration(101);

    // Then: it should be zero because the read is not sequential
    assertEquals(0, generation);
  }

  @Test
  public void test__patternDetector__verifiesPos() {
    // Given: an pattern detector
    SequentialPatternDetector sequentialPatternDetector =
        new SequentialPatternDetector(mock(BlockStore.class));

    // When & Then: pattern detector verifies its arguments
    assertThrows(
        IllegalArgumentException.class, () -> sequentialPatternDetector.isSequentialRead(-100));
    assertThrows(
        IllegalArgumentException.class, () -> sequentialPatternDetector.getGeneration(-100));
  }
}
