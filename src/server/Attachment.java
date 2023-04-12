package server;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Attachment {

    public ByteBuffer readingBuffer = ByteBuffer.allocate(10);
    public int objectLength = 0;
    public int bytesToRead = 0;
    public ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public byte[] getUsefulBytes(){
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Arrays.copyOfRange(byteArray, 4, objectLength+4);
    }

    public boolean isFull(){
        return (getUsefulBytes().length == objectLength);
    }

}
