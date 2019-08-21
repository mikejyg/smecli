package mikejyg.smecli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

import mikejyg.smecli.CmdReturnType.ReturnCode;
import mikejyg.socket.PacketSocket;
import mikejyg.socket.TlvPacket;
import mikejyg.socket.TlvPacket.ReadException;
import mikejyg.socket.TlvPacketType.IllegalValueException;

/**
 * test CLI call/return across a socket.
 * 
 * @author mikejyg
 *
 */
public class CliSocketTest {
	private int port;

	private void serve(Socket socket) throws IOException, ReadException, IllegalValueException {
		PacketSocket packetSocket = new PacketSocket(socket);
		while (true) {

			TlvPacket tlvPacket = packetSocket.receive();

			CmdCallType cmdCall = new CmdCallType(tlvPacket.getData());
			System.out.println("received cmdCall: " + cmdCall.toString());
			CmdReturnType cmdReturn = new CmdReturnType(ReturnCode.OK, "1");

			packetSocket.send(TlvPacket.wrap(cmdReturn.toBytes()));

			if (cmdCall.getCommandName().equals("exit")) {
				System.out.println("serve() exiting...");
				break;
			}
		}
	}

	private void callRemote(PacketSocket packetSocket, CmdCallType cmdCall) throws IOException, ReadException, IllegalValueException, mikejyg.smecli.CmdReturnType.ReturnCode.IllegalValueException {
		packetSocket.send(TlvPacket.wrap(cmdCall.toBytes()));

		TlvPacket tlvPacket = packetSocket.receive();
		CmdReturnType cmdReturn = new CmdReturnType(tlvPacket.getData());

		System.out.println("received return: " + cmdReturn.toString());
	}

	@Test
	public void test() throws IOException, InterruptedException, ReadException, IllegalValueException, mikejyg.smecli.CmdReturnType.ReturnCode.IllegalValueException {
		Thread serverThread = new Thread(()->{
			try (ServerSocket serverSocket = new ServerSocket()) {
				serverSocket.bind(new InetSocketAddress(0));

				port = serverSocket.getLocalPort();
				System.out.println("server port: " + port);

				try (Socket clientSocket = serverSocket.accept()) {

					serve(clientSocket);	

				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		serverThread.start();

		Thread.sleep(1000);

		try (Socket consumerSocket = new Socket()) {
			consumerSocket.connect(new InetSocketAddress("localhost", port));

			PacketSocket packetSocket = new PacketSocket(consumerSocket);

			callRemote(packetSocket, new CmdCallType("abc", "defg"));
			callRemote(packetSocket, new CmdCallType("123", ""));
			callRemote(packetSocket, new CmdCallType("123", new String[]{""}));
			callRemote(packetSocket, new CmdCallType("123", new String[]{"345","678"}));

			callRemote(packetSocket, new CmdCallType("exit"));
		}

		serverThread.join();

		System.out.println("all done.");

	}


}
