package riemann.client;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import riemann.Proto.Msg;

public class RiemannTcpClient extends RiemannClient {

  private Socket socket;
  private DataOutputStream out;
  private DataInputStream in;

  public static int connectTimeout = 5;
  public static int readTimeout = 5;

  public RiemannTcpClient() throws UnknownHostException {}

  public RiemannTcpClient(int port) throws UnknownHostException {
    super(port);
  }

  public RiemannTcpClient(InetSocketAddress server) {
    super(server);
  }

  @Override
  public void sendMessage(Msg message) throws IOException {
    out.writeInt(message.getSerializedSize());
    message.writeTo(out);
    out.flush();
  }

  @Override
  public Msg recvMessage() throws IOException {
    // Get length header
    int len = in.readInt();
    if (len < 0) {
      throw new IOException("FUCKED");
    }

    // Get body
    byte[] body = new byte[len];
    in.readFully(body);
    return Msg.parseFrom(body);
  }

  @Override
  public Msg sendRecvMessage(Msg message) throws IOException {
    synchronized(this) {
      sendMessage(message);
      return recvMessage();
    }
  }

  @Override
  public Msg sendMaybeRecvMessage(Msg message) throws IOException {
    return sendRecvMessage(message);
  }

  @Override
  public boolean isConnected() {
    return this.socket != null && this.socket.isConnected();
  }

  @Override
  public void connect() throws IOException {
    synchronized(this) {
      socket = new Socket();
      socket.connect(super.server, 5);
      socket.setTcpNoDelay(true);
      this.out = new DataOutputStream(this.socket.getOutputStream());
      this.in = new DataInputStream(this.socket.getInputStream());
    }
  }

  @Override
  public void disconnect() throws IOException {
    synchronized(this) {
      this.out.close();
      this.in.close();
      this.socket.close();
    }
  }
}
