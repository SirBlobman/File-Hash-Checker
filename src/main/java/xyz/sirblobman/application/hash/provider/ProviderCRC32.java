package xyz.sirblobman.application.hash.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public final class ProviderCRC32 {
    public static String calculateHash(File file) {
        CRC32 crc32 = new CRC32();
        crc32.reset();

        try(FileInputStream stream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];

            int bytesRead;
            while((bytesRead = stream.read(buffer)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }

            long value = crc32.getValue();
            return Long.toHexString(value);
        } catch(IOException ex) {
            System.out.println("An error occurred while reading a file:");
            ex.printStackTrace();
            return "ERROR!";
        }
    }
}
