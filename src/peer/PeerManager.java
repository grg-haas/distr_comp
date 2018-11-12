package peer;

import peer.client.AbstractClientPeer;
import peer.client.TextClient;
import peer.server.AbstractServerPeer;
import peer.server.TextServer;

import java.util.Scanner;

public class PeerManager
{
    public static final String HEADER = "(PeerManager)";
    public static void main(String[] args)
    {
        Scanner input = new Scanner(System.in);

        System.out.print(HEADER + " >>> Do you want to run a (1) client or (2) server? ");
        int choice = input.nextInt();

        input.nextLine();

        System.out.print(HEADER + " >>> IP of ServerRouter? ");
        String IP = input.nextLine();

        System.out.print(HEADER + " >>> Port of ServerRouter? ");
        String port = input.nextLine();

        if(choice == 1)
        {
            AbstractClientPeer client = null;

            System.out.print(HEADER + " >>> Type of client? (1) Text, (2) Image, (3) Video: ");
            choice = input.nextInt();

            if(choice == 1)
            {
                client = new TextClient();
            }

            client.registerToServerRouter(IP, Integer.parseInt(port), -1);
            client.run();
        }
        else
        {
            AbstractServerPeer server = null;

            System.out.print(HEADER + " >>> Type of server? (1) Text, (2) Image, (3) Video: ");
            choice = input.nextInt();

            input.nextLine();

            if(choice == 1)
            {
                server = new TextServer();
            }

            System.out.print(HEADER + " >>> Port to listen on: ");
            String listenPort = input.nextLine();

            server.registerToServerRouter(IP, Integer.parseInt(port), Integer.parseInt(listenPort));
            server.run();
        }
    }
}