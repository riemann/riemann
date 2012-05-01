package riemann.client;

import riemann.Proto.Event;
import riemann.client.RiemannClient;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;

public class EventDSL {
  public RiemannClient client;
  public Event.Builder builder;

  public EventDSL(RiemannClient client) {
    this.client = client;
    this.builder = Event.newBuilder();
  }

  public EventDSL host(String host) {
    builder.setHost(host);
    return this;
  }
  
  public EventDSL service(String service) {
    builder.setService(service);
    return this;
  }
  
  public EventDSL state(String state) {
    builder.setState(state);
    return this;
  }
  
  public EventDSL description(String description) {
    builder.setDescription(description);
    return this;
  }
  
  public EventDSL time(int time) {
    builder.setTime(time);
    return this;
  }
  
  public EventDSL metric(float metric) {
    builder.setMetricF(metric);
    return this;
  }

  public EventDSL metric(int metric) {
    builder.setMetricF((float) metric);
    return this;
  }
  
  public EventDSL tag(String tag) {
    builder.addTags(tag);
    return this;
  }
  
  public EventDSL ttl(float ttl) {
    builder.setTtl(ttl);
    return this;
  }
 
  public EventDSL tags(List<String> tags) {
    builder.addAllTags(tags);
    return this;
  }

  public EventDSL tags(String... tags) {
    builder.addAllTags(Arrays.asList(tags));
    return this;
  }

  public Boolean send() throws IOException {
    return client.sendEvents(builder.build());
  }
}
