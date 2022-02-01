package se.sics.test;
import se.sics.kompics.KompicsEvent;
import java.util.Comparator;

public class Pong implements KompicsEvent {
  public int id;
  public Pong(int id) {
    this.id = id;
  }
  public static Comparator<Pong> comparator = new Comparator<Pong>() {
    public int compare(Pong p1, Pong p2) {
      return p1.id - p2.id;
    }
  };
}
