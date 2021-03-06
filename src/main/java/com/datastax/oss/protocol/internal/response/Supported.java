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
package com.datastax.oss.protocol.internal.response;

import com.datastax.oss.protocol.internal.Message;
import com.datastax.oss.protocol.internal.PrimitiveCodec;
import com.datastax.oss.protocol.internal.PrimitiveSizes;
import com.datastax.oss.protocol.internal.ProtocolConstants;
import java.util.List;
import java.util.Map;

public class Supported extends Message {

  public final Map<String, List<String>> options;

  public Supported(Map<String, List<String>> options) {
    super(true, ProtocolConstants.Opcode.SUPPORTED);
    this.options = options;
  }

  @Override
  public String toString() {
    return "SUPPORTED " + options;
  }

  public static class Codec extends Message.Codec {
    public Codec(int protocolVersion) {
      super(ProtocolConstants.Opcode.SUPPORTED, protocolVersion);
    }

    @Override
    public <B> void encode(B dest, Message message, PrimitiveCodec<B> encoder) {
      encoder.writeStringMultimap(((Supported) message).options, dest);
    }

    @Override
    public int encodedSize(Message message) {
      Supported supported = (Supported) message;
      return PrimitiveSizes.sizeOfStringMultimap(supported.options);
    }

    @Override
    public <B> Message decode(B source, PrimitiveCodec<B> decoder) {
      Map<String, List<String>> options = decoder.readStringMultimap(source);
      return new Supported(options);
    }
  }
}
