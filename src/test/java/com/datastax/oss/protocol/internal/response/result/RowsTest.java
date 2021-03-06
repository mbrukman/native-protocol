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
package com.datastax.oss.protocol.internal.response.result;

import static com.datastax.oss.protocol.internal.Assertions.assertThat;

import com.datastax.oss.protocol.internal.Message;
import com.datastax.oss.protocol.internal.MessageTestBase;
import com.datastax.oss.protocol.internal.PrimitiveSizes;
import com.datastax.oss.protocol.internal.ProtocolConstants;
import com.datastax.oss.protocol.internal.TestDataProviders;
import com.datastax.oss.protocol.internal.binary.MockBinaryString;
import com.datastax.oss.protocol.internal.response.Result;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.datastax.oss.protocol.internal.util.collection.NullAllowingImmutableList;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class RowsTest extends MessageTestBase<Rows> {
  private static final RawType BLOB_TYPE = RawType.PRIMITIVES.get(ProtocolConstants.DataType.BLOB);

  public RowsTest() {
    super(Rows.class);
  }

  @Override
  protected Message.Codec newCodec(int protocolVersion) {
    return new Result.Codec(protocolVersion);
  }

  @Test
  @UseDataProvider(location = TestDataProviders.class, value = "protocolV3OrAbove")
  public void should_encode_and_decode(int protocolVersion) {
    RowsMetadata metadata =
        new RowsMetadata(
            NullAllowingImmutableList.of(
                new ColumnSpec("ks1", "table1", "column1", 0, BLOB_TYPE),
                new ColumnSpec("ks1", "table1", "column2", 1, BLOB_TYPE)),
            null,
            null,
            null);
    Queue<List<ByteBuffer>> data = new ArrayDeque<>();
    data.add(
        NullAllowingImmutableList.of(Bytes.fromHexString("0x11"), Bytes.fromHexString("0x12")));
    data.add(
        NullAllowingImmutableList.of(Bytes.fromHexString("0x21"), Bytes.fromHexString("0x22")));
    data.add(
        NullAllowingImmutableList.of(Bytes.fromHexString("0x31"), Bytes.fromHexString("0x32")));
    Rows initial = new DefaultRows(metadata, data);

    MockBinaryString encoded = encode(initial, protocolVersion);

    assertThat(encoded)
        .isEqualTo(
            new MockBinaryString()
                .int_(ProtocolConstants.ResultKind.ROWS)
                // Simple metadata with 2 columns:
                .int_(0x0001)
                .int_(2)
                .string("ks1")
                .string("table1")
                .string("column1")
                .unsignedShort(ProtocolConstants.DataType.BLOB)
                .string("column2")
                .unsignedShort(ProtocolConstants.DataType.BLOB)
                // Rows:
                .int_(3) // count
                .bytes("0x11")
                .bytes("0x12")
                .bytes("0x21")
                .bytes("0x22")
                .bytes("0x31")
                .bytes("0x32"));
    assertThat(encodedSize(initial, protocolVersion))
        .isEqualTo(
            PrimitiveSizes.INT
                + (PrimitiveSizes.INT
                    + PrimitiveSizes.INT
                    + (PrimitiveSizes.SHORT + "ks1".length())
                    + (PrimitiveSizes.SHORT + "table1".length())
                    + ((PrimitiveSizes.SHORT + "column1".length()) + PrimitiveSizes.SHORT)
                    + ((PrimitiveSizes.SHORT + "column2".length()) + PrimitiveSizes.SHORT))
                + (PrimitiveSizes.INT
                    + (PrimitiveSizes.INT + "11".length() / 2)
                    + (PrimitiveSizes.INT + "12".length() / 2)
                    + (PrimitiveSizes.INT + "21".length() / 2)
                    + (PrimitiveSizes.INT + "22".length() / 2)
                    + (PrimitiveSizes.INT + "31".length() / 2)
                    + (PrimitiveSizes.INT + "32".length() / 2)));

    Rows decoded = decode(encoded, protocolVersion);

    assertThat(decoded)
        .hasNextRow("0x11", "0x12")
        .hasNextRow("0x21", "0x22")
        .hasNextRow("0x31", "0x32");
  }

  @Test
  @UseDataProvider(location = TestDataProviders.class, value = "protocolV3OrAbove")
  public void should_encode_and_decode_when_no_metadata(int protocolVersion) {
    RowsMetadata emptyMetadata = new RowsMetadata(2, null, null, null);
    Queue<List<ByteBuffer>> data = new ArrayDeque<>();
    data.add(
        NullAllowingImmutableList.of(Bytes.fromHexString("0x11"), Bytes.fromHexString("0x12")));
    data.add(
        NullAllowingImmutableList.of(Bytes.fromHexString("0x21"), Bytes.fromHexString("0x22")));
    data.add(
        NullAllowingImmutableList.of(Bytes.fromHexString("0x31"), Bytes.fromHexString("0x32")));
    Rows initial = new DefaultRows(emptyMetadata, data);

    MockBinaryString encoded = encode(initial, protocolVersion);

    assertThat(encoded)
        .isEqualTo(
            new MockBinaryString()
                .int_(ProtocolConstants.ResultKind.ROWS)
                // No metadata:
                .int_(0x0004)
                .int_(2) // column count
                // Rows:
                .int_(3) // count
                .bytes("0x11")
                .bytes("0x12")
                .bytes("0x21")
                .bytes("0x22")
                .bytes("0x31")
                .bytes("0x32"));
    assertThat(encodedSize(initial, protocolVersion))
        .isEqualTo(
            PrimitiveSizes.INT
                + PrimitiveSizes.INT
                + PrimitiveSizes.INT
                + (PrimitiveSizes.INT
                    + (PrimitiveSizes.INT + "11".length() / 2)
                    + (PrimitiveSizes.INT + "12".length() / 2)
                    + (PrimitiveSizes.INT + "21".length() / 2)
                    + (PrimitiveSizes.INT + "22".length() / 2)
                    + (PrimitiveSizes.INT + "31".length() / 2)
                    + (PrimitiveSizes.INT + "32".length() / 2)));

    Rows decoded = decode(encoded, protocolVersion);

    assertThat(decoded)
        .hasNextRow("0x11", "0x12")
        .hasNextRow("0x21", "0x22")
        .hasNextRow("0x31", "0x32");
  }
}
