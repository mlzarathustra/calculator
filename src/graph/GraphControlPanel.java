package graph;

import java.awt.*;
import java.awt.event.*;

import form.util.*;

public class GraphControlPanel extends Panel {
  Graph graph;
  TextField scale;
  Button resetOrigin;
  
  GraphControlPanel(Graph g) {
    graph=g;
    add(new Label("set scale"));
    add(scale=new TextField(""+graph.getScale(), 20));
    add(resetOrigin=new Button("reset origin to center"));
    
    resetOrigin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        graph.resetOrigin(); graph.repaint();
      }
    });
    
    scale.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        double d=0;
        try {
          d=(new Double(scale.getText())).doubleValue();
        }
        catch (NumberFormatException e) {
          Util.messageBox(">>error!<<",
            scale.getText() + " is not a valid number.");
          return;
        }
        if (d != 0.0) graph.setScale(d); graph.repaint();
      }
    });
  }
  
}
