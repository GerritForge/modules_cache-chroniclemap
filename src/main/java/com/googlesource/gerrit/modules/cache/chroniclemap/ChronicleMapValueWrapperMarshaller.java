package com.googlesource.gerrit.modules.cache.chroniclemap;

import com.google.gerrit.server.cache.serialize.CacheSerializer;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.util.ReadResolvable;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;

import java.nio.ByteBuffer;

public class ChronicleMapValueWrapperMarshaller<V>
        implements BytesWriter<ChronicleMapValueWrapper<V>>, BytesReader<ChronicleMapValueWrapper<V>>, ReadResolvable<ChronicleMapValueWrapperMarshaller<V>> {

    private final CacheSerializer<V> serializer;

    ChronicleMapValueWrapperMarshaller(CacheSerializer<V> serializer) {
        this.serializer = serializer;
    }

    @Override
    public ChronicleMapValueWrapperMarshaller<V> readResolve() {
        return new ChronicleMapValueWrapperMarshaller<>(serializer);
    }

    @Override
    public ChronicleMapValueWrapper<V> read(Bytes in, ChronicleMapValueWrapper<V> using) {
        // Total capacity
        int serializedLength = in.toByteArray().length;

        // Deserialize the timestamp (first 8 bytes)
        byte[] serializedLong = new byte[Long.BYTES];
        in.read(serializedLong, 0, Long.BYTES);
        ByteBuffer buffer = ByteBuffer.wrap(serializedLong);
        long t = buffer.getLong(0);
        in.readPosition(Long.BYTES);

        // Deserialize object V (remaining bytes)
        int vLength = serializedLength - Long.BYTES;
        byte[] serializedV = new byte[vLength];
        in.read(serializedV, 0, vLength);
        V v = serializer.deserialize(serializedV);

        using = new ChronicleMapValueWrapper<>(v, serializer).setTimestamp(t);

        return using;
    }

    @Override
    public void write(Bytes out, ChronicleMapValueWrapper<V> toWrite) {
        byte[] serialized = serializer.serialize(toWrite.getValue());
        final int capacity = Long.BYTES + serialized.length;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        buffer.putLong(0, toWrite.getTimestamp());
        buffer.position(Long.BYTES);
        buffer.put(serialized);

        out.write(buffer.array());
    }
}
