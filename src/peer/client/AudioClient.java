package peer.client;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class AudioClient extends AbstractClientPeer
{
    private static final String HEADER = "(AudioClient)";

    private static final String COMMAND_CONNECT = "connect";
    private static final String COMMAND_DISCONNECT = "disconnect";
    private static final String COMMAND_SEND_AUDIO = "audio";
    private static final String COMMAND_EXIT = "exit";

    public AudioClient()
    {
        super("audio");
    }

    private Socket connectToServer(String from)
    {
        return super.connectToServer("audio", from);
    }

    @Override
    public void run()
    {
        Scanner input = new Scanner(System.in);
        Socket server = null;

        InputStream serverInputStream = null;
        OutputStream serverOutputStream = null;

        while (true)
        {
            try
            {
                System.out.print(HEADER + " >>> ");
                String read = input.nextLine();

                String[] command = read.split(" ");

                switch (command[0])
                {
                    case COMMAND_CONNECT: // args: [from]
                        server = connectToServer(command[1]);

                        try
                        {
                            serverInputStream = server.getInputStream();
                            serverOutputStream = server.getOutputStream();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_DISCONNECT:
                        try
                        {
                            serverOutputStream.write(new byte[]{0, 0, 0, 0});
                            serverOutputStream.flush();

                            if (server != null)
                            {
                                server.close();
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_SEND_AUDIO: // args: [source_file] [dest_file]
                        try
                        {
                            File audioFile = new File(command[1]);
                            FileInputStream fileInputStream = new FileInputStream(audioFile);

                            System.out.println(HEADER + ": file " + command[1] + " has size " + audioFile.length());

                            byte[] sendSizeBytes = ByteBuffer.allocate(4).putInt((int) audioFile.length()).array();
                            serverOutputStream.write(sendSizeBytes);

                            byte[] inputByteBuffer = new byte[8192];
                            int sentBytes = 0;

                            while(sentBytes != audioFile.length())
                            {
                                int chunkSize = fileInputStream.read(inputByteBuffer, 0, 8192);
                                sentBytes += chunkSize;

                                System.out.println(HEADER + ": sent chunk of size " + chunkSize + ", sentBytes = " + sentBytes);

                                serverOutputStream.write(inputByteBuffer, 0, chunkSize);
                            }

                            serverOutputStream.flush();
                            fileInputStream.close();

                            System.out.println(HEADER + ": sent file, waiting for converted size");

                            byte[] recvSizeBytes = new byte[4];
                            serverInputStream.read(recvSizeBytes);

                            int recvSize = ByteBuffer.wrap(recvSizeBytes).asIntBuffer().get();

                            System.out.println(HEADER + ": converted file has size " + recvSize);

                            byte[] outputByteBuffer = new byte[8192];
                            File resultFile = new File(command[2]);
                            FileOutputStream fileOutputStream = new FileOutputStream(resultFile);

                            int receivedBytes = 0;
                            while(receivedBytes != recvSize)
                            {
                                int chunkSize = serverInputStream.read(outputByteBuffer, 0, 8192);
                                receivedBytes += chunkSize;

                                System.out.println(HEADER + ": received chunk of size " + chunkSize + ", receivedBytes = " + receivedBytes);

                                fileOutputStream.write(outputByteBuffer, 0, chunkSize);
                            }

                            System.out.println(HEADER + ": received converted data, writing to file");
                            fileOutputStream.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        break;

                    case COMMAND_EXIT:
                        try
                        {
                            if (server != null)
                            {
                                server.close();
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace(System.out);
                        }

                        return;
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace(System.out);
            }
        }
    }
}
