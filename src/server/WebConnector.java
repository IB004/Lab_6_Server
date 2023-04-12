package server;

import client.Message;
import client.Warning;
import data.CommandData;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

public class WebConnector {
    public WebConnector(Message messageComponent, Warning warningComponent) throws Exception{
        this.messageComponent = messageComponent;
        this.warningComponent = warningComponent;
        startActions();
    }
    private int PORT = 8888;
    private SocketAddress socketAddress;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private final Message messageComponent;
    private final Warning warningComponent;

    public void serveConnections() throws Exception{

        if (selector.select(5000) == 0) {
            messageComponent.waitingMessage();
            return;
        }
        //System.out.println( Arrays.toString(selector.selectedKeys().toArray()));
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            Attachment attachment = (Attachment) key.attachment();
            try {
                if (key.isAcceptable()) {
                    accept();
                }
                if (key.isReadable()) {
                    read(key);
                    fillAttachment(key);
                    if (attachment.isFull()){
                        CommandData commandData = transformToCommandData(attachment.getUsefulBytes());
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
                if (key.isWritable()){
                    System.out.println("Writable channel");
                    key.cancel();
                }
                iterator.remove();
                System.out.println("Iterator was removed");
            }
            catch (IOException e){
                warningComponent.showWarning(e);
                key.cancel();
            }
        }
    }

    public void startActions() throws Exception{
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        socketAddress = new InetSocketAddress(PORT);
        serverSocketChannel.socket().bind(socketAddress);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    private void accept() throws Exception{
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, new Attachment());
        messageComponent.printText("New client connected: " + socketChannel.toString());
    }

    private int read(SelectionKey key) throws Exception{
        Attachment attachment = (Attachment) key.attachment();
        ByteBuffer buffer = attachment.readingBuffer;
        messageComponent.printText("Reading");
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int r = socketChannel.read(buffer);
        if (r == -1) {
            socketChannel.close();
            warningComponent.warningMessage("Connection was ended.");
            return -1;
        }
        messageComponent.printText("Server got " + r + " bytes: " + Arrays.toString(buffer.array()));
        return r;
    }
    private void fillAttachment(SelectionKey key) throws IOException {
        Attachment attachment = (Attachment) key.attachment();
        ByteBuffer buffer = attachment.readingBuffer;
        buffer.flip();

        //if this record is first
        if (attachment.bytesToRead == 0){
            if (buffer.remaining() > 4){
                attachment.objectLength = buffer.getInt();
                attachment.bytesToRead = attachment.objectLength;
                int canRead = Math.min(buffer.remaining(), attachment.bytesToRead);
                attachment.byteArrayOutputStream.write((buffer.get(new byte[canRead])).array());
                attachment.bytesToRead -= canRead;
            }
            else{
                warningComponent.warningMessage("Not enough bytes for object");
                key.cancel();
            }
        }
        //if this record is second+
        else{
            int canRead = Math.min(buffer.remaining(), attachment.bytesToRead);
            attachment.byteArrayOutputStream.write((buffer.get(new byte[canRead])).array());
            attachment.bytesToRead -= canRead;
        }
        //if object is ended
        if (attachment.getUsefulBytes().length >= attachment.objectLength){
            messageComponent.printText("Can transform!");
            System.out.println("Useful bytes: " + Arrays.toString(attachment.getUsefulBytes()));
        }
        if (attachment.bytesToRead < 0){
            warningComponent.warningMessage("Something went wrong during buffered reading");
        }
        if (attachment.bytesToRead > 0){
            messageComponent.printText("Still have to read something");
        }
        buffer.clear();
        buffer.flip();
    }

    private CommandData transformToCommandData(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        System.out.println("Bytes in object: " + bytes.length);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        CommandData commandData = (CommandData) objectInputStream.readObject();
        System.out.println(commandData.command.getName());
        return commandData;
    }


}


