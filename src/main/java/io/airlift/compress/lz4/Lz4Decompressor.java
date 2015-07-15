/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.compress.lz4;

import io.airlift.compress.Decompressor;
import io.airlift.compress.MalformedInputException;

import java.nio.ByteBuffer;

import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

public class Lz4Decompressor
    implements Decompressor
{
    @Override
    public int decompress(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset, int maxOutputLength)
            throws MalformedInputException
    {
        long inputAddress = ARRAY_BYTE_BASE_OFFSET + inputOffset;
        long inputLimit = inputAddress + inputLength;
        long outputAddress = ARRAY_BYTE_BASE_OFFSET + outputOffset;
        long outputLimit = outputAddress + maxOutputLength;

        return Lz4RawDecompressor.uncompress(input, inputAddress, inputLimit, output, outputAddress, outputLimit);
    }

    @Override
    public int decompress(ByteBuffer input, int inputOffset, int inputLength, ByteBuffer output, int outputOffset, int maxLength)
            throws MalformedInputException
    {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
