package graph;

import java.awt.*;

public class GraphPanel extends Container {
  Graph graph;
  EquationPanel equations;
  
  public GraphPanel() {
    setLayout(new BorderLayout());
    add("Center", graph=new EvalGraph());
    
    add("North", equations=new EquationPanel((EvalGraph)graph));
    add("South", new GraphControlPanel(graph));
  }
  
}
