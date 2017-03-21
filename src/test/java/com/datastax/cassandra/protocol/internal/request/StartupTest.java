/*
 * Copyright (C) 2017-2017 DataStax Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.cassandra.protocol.internal.request;

import com.datastax.cassandra.protocol.internal.Message;
import com.datastax.cassandra.protocol.internal.MessageTest;
import com.datastax.cassandra.protocol.internal.TestDataProviders;
import com.datastax.cassandra.protocol.internal.binary.MockBinaryString;
import org.testng.annotations.Test;

import static com.datastax.cassandra.protocol.internal.Assertions.assertThat;

public class StartupTest extends MessageTest<Startup> {

  public StartupTest() {
    super(Startup.class);
  }

  @Override
  protected Message.Codec newCodec(int protocolVersion) {
    return new Startup.Codec(protocolVersion);
  }

  @Test(dataProviderClass = TestDataProviders.class, dataProvider = "protocolV3OrAbove")
  public void should_encode_and_decode_with_compression(int protocolVersion) {
    Startup initial = new Startup("LZ4");

    MockBinaryString encoded = encode(initial, protocolVersion);

    assertThat(encoded)
        .isEqualTo(
            new MockBinaryString()
                .unsignedShort(2) // size of string map
                // string map entries
                .string("COMPRESSION")
                .string("LZ4")
                .string("CQL_VERSION")
                .string("3.0.0"));
    assertThat(encodedSize(initial, protocolVersion))
        .isEqualTo(
            2
                + (2 + "COMPRESSION".length())
                + (2 + "LZ4".length())
                + (2 + "CQL_VERSION".length())
                + (2 + "3.0.0".length()));

    Startup decoded = decode(encoded, protocolVersion);

    assertThat(decoded.options)
        .hasSize(2)
        .containsEntry("COMPRESSION", "LZ4")
        .containsEntry("CQL_VERSION", "3.0.0");
  }

  @Test(dataProviderClass = TestDataProviders.class, dataProvider = "protocolV3OrAbove")
  public void should_encode_and_decode_without_compression(int protocolVersion) {
    Startup initial = new Startup();

    MockBinaryString encoded = encode(initial, protocolVersion);

    assertThat(encoded)
        .isEqualTo(new MockBinaryString().unsignedShort(1).string("CQL_VERSION").string("3.0.0"));
    assertThat(encodedSize(initial, protocolVersion))
        .isEqualTo(2 + (2 + "CQL_VERSION".length()) + (2 + "3.0.0".length()));

    Startup decoded = decode(encoded, protocolVersion);

    assertThat(decoded.options).hasSize(1).containsEntry("CQL_VERSION", "3.0.0");
  }
}
