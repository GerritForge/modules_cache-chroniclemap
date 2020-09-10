package com.googlesource.gerrit.modules.cache.chroniclemap;

import com.google.gerrit.server.cache.serialize.ObjectIdCacheSerializer;
import net.openhft.chronicle.bytes.Bytes;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import java.nio.ByteBuffer;

import static com.google.common.truth.Truth.assertThat;

public class ChronicleValueWrapperTest {

  @Test
  public void shouldSerializeAndDeserializeBack() {
    ObjectId id = ObjectId.fromString("1234567890123456789012345678901234567890");
    long timestamp = 1600329018L;
    ChronicleMapValueWrapperMarshaller<ObjectId> marshaller =
        new ChronicleMapValueWrapperMarshaller<>(ObjectIdCacheSerializer.INSTANCE);

    final ChronicleMapValueWrapper<ObjectId> wrapped =
        new ChronicleMapValueWrapper<>(id, ObjectIdCacheSerializer.INSTANCE)
            .setTimestamp(timestamp);

    Bytes<ByteBuffer> out = Bytes.elasticByteBuffer();
    marshaller.write(out, wrapped);
    final ChronicleMapValueWrapper<ObjectId> actual = marshaller.read(out, null);
    assertThat(actual).isEqualTo(wrapped);
  }
}
