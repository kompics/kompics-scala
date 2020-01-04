package se.sics.test;
import se.sics.kompics.KompicsEvent;
import java.util.Comparator;

public class Ping implements KompicsEvent {
  public int id;
  public Ping(int id) {
    this.id = id;
  }
  public static Comparator<Ping> comparator = new Comparator<Ping>() {
    public int compare(Ping p1, Ping p2) {
      return p1.id - p2.id;
    }
  };
}