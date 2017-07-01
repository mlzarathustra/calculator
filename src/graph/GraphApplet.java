package graph;

import java.awt.*;
import java.applet.*;

public class GraphApplet extends Applet {
  public GraphApplet() {}
  public void init() {
    setLayout(new BorderLayout());
    add("Center", new GraphPanel());
  }
}

