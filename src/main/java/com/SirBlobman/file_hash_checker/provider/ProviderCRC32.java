package com.SirBlobman.file_hash_checker.provider;

import java.security.MessageDigest;
import java.util.zip.CRC32;

public class ProviderCRC32 extends MessageDigest {
    private final CRC32 crc32;
    public ProviderCRC32() {
        super("CRC32");
        this.crc32 = new CRC32();
    }

    @Override
    protected void engineReset() {
        this.crc32.reset();
    }

    @Override
    protected void engineUpdate(byte input) {
        this.crc32.update(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        this.crc32.update(input, offset, len);
    }

    @Override
    protected byte[] engineDigest() {
        long value = crc32.getValue();
        byte[] bytes = new byte[4];
        bytes[3] = (byte) ((value & 0xFF000000) >> 24);
        bytes[2] = (byte) ((value & 0x00FF0000) >> 16);
        bytes[1] = (byte) ((value & 0x0000FF00) >> 8);
        bytes[0] = (byte) (value & 0x000000FF);
        return bytes;
    }
}
