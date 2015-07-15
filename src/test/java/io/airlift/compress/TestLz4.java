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
package io.airlift.compress;

import io.airlift.compress.lz4.Lz4Decompressor;
import net.jpountz.lz4.LZ4Factory;

public class TestLz4
    extends AbstractTestCompression
{
    protected byte[] prepareCompressedData(byte[] uncompressed)
    {
        return LZ4Factory.fastestInstance().fastCompressor().compress(uncompressed);
    }

    @Override
    protected Decompressor getDecompressor()
    {
        return new Lz4Decompressor();
    }
}
