package org.lodder.subtools.multisubdownloader.subtitleproviders;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.jspecify.annotations.NullMarked;

/**
 * Hash code is based on Media Player Classic. In natural language it calculates: size + 64bit checksum of the first and
 * last 64k (even if they overlap because the file is smaller than 128k).
 */
@NullMarked
public class FileHasher {

    /**
     * Size of the chunks that will be hashed in bytes (64 KB)
     */
    private static final int CHUNK_SIZE = 64 * 1024;

    public static String computeHash(Path path) throws IOException {
        long size = Files.size(path);
        long chunkSize = Math.min(CHUNK_SIZE, size);

        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            long head = hash(fileChannel.map(MapMode.READ_ONLY, 0, chunkSize));
            long tail = hash(
                fileChannel.map(MapMode.READ_ONLY, Math.max(size - CHUNK_SIZE, 0), chunkSize));
            return "%016x".formatted(size + head + tail);
        }
    }

    public static String computeHash(InputStream in, long length) throws IOException {

        int chunkSize = (int) Math.min(CHUNK_SIZE, length);

        // buffer that will contain the head and the tail chunk, chunks will overlap if length is smaller than two
        // chunks
        byte[] buffer = new byte[(int) Math.min(2 * CHUNK_SIZE, length)];

        try (DataInputStream data = new DataInputStream(in)) {

            // first chunk
            data.readFully(buffer, 0, chunkSize);

            long position = chunkSize;
            long tailChunkPosition = length - chunkSize;
            // seek to position of the tail chunk, or not at all if length is smaller than two chunks
            while (position < tailChunkPosition && (position += data.skip(tailChunkPosition - position)) >= 0) {
            }
            //data.skipNBytes(Math.max(0, length - 2L * chunkSize));

            // second chunk, or the rest of the data if length is smaller than two chunks
            data.readFully(buffer, chunkSize, buffer.length - chunkSize);

            long head = hash(ByteBuffer.wrap(buffer, 0, chunkSize));
            long tail = hash(ByteBuffer.wrap(buffer, buffer.length - chunkSize, chunkSize));

            return "%016x".formatted(length + head + tail);
        }
    }

    private static long hash(ByteBuffer buffer) {
        LongBuffer longBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
        long hash = 0;
        while (longBuffer.hasRemaining()) {
            hash += longBuffer.get();
        }
        return hash;
    }
}
