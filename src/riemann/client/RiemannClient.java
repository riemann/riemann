package riemann.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Arrays;

import riemann.Proto.Event;
import riemann.Proto.Query;
import riemann.Proto.Msg;

public abstract class RiemannClient {

  public static final int DEFAULT_PORT = 5555;

  protected final InetSocketAddress server;

  public RiemannClient(final InetSocketAddress server) {
    this.server = server;
  }

  public RiemannClient(final int port) throws UnknownHostException {
    this.server = new InetSocketAddress(InetAddress.getLocalHost(), port);
  }

  public RiemannClient() throws UnknownHostException {
    this(new InetSocketAddress(InetAddress.getLocalHost(), DEFAULT_PORT));
  }
 
  public EventDSL event() {
    return new EventDSL(this);
  }

  public Boolean sendEvents(final Event... events) throws IOException {
    Msg m = sendMaybeRecvMessage(Msg.newBuilder()
        .addAllEvents(Arrays.asList(events))
        .build());

    if (m.getOk() == false) {
      return false;
    } else {
      return true;
    }
  }

  public List<Event> query(String q) throws IOException {
    Msg m = sendRecvMessage(Msg.newBuilder()
        .setQuery(
          Query.newBuilder().setString(q).build())
        .build());
    if (m.getOk() == false) {
      return null;
    } else {
      return m.getEventsList();
    }
  }

  public abstract void sendMessage(Msg message) throws IOException;

  public abstract Msg recvMessage() throws IOException;

  public abstract Msg sendRecvMessage(Msg message) throws IOException;

  public abstract Msg sendMaybeRecvMessage(Msg message) throws IOException;

  public abstract boolean isConnected();

  public abstract void connect() throws IOException;

  public abstract void disconnect() throws IOException;
}
