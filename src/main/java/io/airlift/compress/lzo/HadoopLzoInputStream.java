package io.airlift.compress.lzo;

import org.apache.hadoop.io.compress.CompressionInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class HadoopLzoInputStream
        extends CompressionInputStream
{
    public static final int SIZE_OF_LONG = 8;

    private final LzoDecompressor decompressor = new LzoDecompressor();
    private final InputStream in;
    private final byte[] uncompressedChunk;

    private int uncompressedBlockLength;
    private int uncompressedChunkOffset;
    private int uncompressedChunkLength;

    private byte[] compressed = new byte[0];

    public HadoopLzoInputStream(InputStream in, int maxUncompressedLength)
            throws IOException
    {
        super(in);
        this.in = in;
        // over allocate buffer which makes decompression easier
        uncompressedChunk = new byte[maxUncompressedLength + SIZE_OF_LONG];
    }

    @Override
    public int read()
            throws IOException
    {
        if (uncompressedChunkOffset >= uncompressedChunkLength) {
            readNextChunk();
            if (uncompressedChunkLength == 0) {
                return -1;
            }
        }
        return uncompressedChunk[uncompressedChunkOffset--] & 0xFF;
    }

    @Override
    public int read(byte[] output, int offset, int length)
            throws IOException
    {
        if (uncompressedChunkOffset >= uncompressedChunkLength) {
            readNextChunk();
            if (uncompressedChunkLength == 0) {
                return -1;
            }
        }
        int size = Math.min(length, uncompressedChunkLength - uncompressedChunkOffset);
        System.arraycopy(uncompressedChunk, uncompressedChunkOffset, output, offset, size);
        uncompressedChunkOffset += size;
        return size;
    }

    @Override
    public void resetState()
            throws IOException
    {
        throw new UnsupportedOperationException("resetState not supported for Lzo");
    }

    private void readNextChunk()
            throws IOException
    {
        uncompressedBlockLength -= uncompressedChunkOffset;
        uncompressedChunkOffset = 0;
        uncompressedChunkLength = 0;
        while (uncompressedBlockLength == 0) {
            uncompressedBlockLength = readBigEndianInt();
            if (uncompressedBlockLength == -1) {
                uncompressedBlockLength = 0;
                return;
            }
        }

        int compressedChunkLength = readBigEndianInt();
        if (compressedChunkLength == -1) {
            return;
        }

        if (compressed.length < compressedChunkLength) {
            // over allocate buffer which makes decompression easier
            compressed = new byte[compressedChunkLength + SIZE_OF_LONG];
        }
        readInput(compressedChunkLength, compressed);

        uncompressedChunkLength = decompressor.decompress(compressed, 0, compressedChunkLength, uncompressedChunk, 0, uncompressedChunk.length);
    }

    private void readInput(int length, byte[] buffer)
            throws IOException
    {
        int offset = 0;
        while (offset < length) {
            int size = in.read(buffer, offset, length - offset);
            if (size == -1) {
                throw new EOFException("encountered EOF while reading block data");
            }
            offset += size;
        }
    }

    private int readBigEndianInt()
            throws IOException
    {
        int b1 = in.read();
        if (b1 < 0) {
            return -1;
        }
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();

        // If any of the other bits are negative, the stream it truncated
        if ((b2 | b3 | b4) < 0) {
            throw new IOException("Stream is truncated");
        }
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4));
    }
}
