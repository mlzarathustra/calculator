package graph;

import java.awt.*;
import java.awt.event.*;

import form.util.*;
import eval.*;

/**  both the view and the data (a Noun)
 */
public class Equation extends Panel {
  Choice leftSide=new Choice();
  TextField text=new TextField(60);
  EvalGraph graph;
  boolean polar;
  Noun noun;
  
  Equation(EvalGraph g) {
    graph=g;
    leftSide.addItem("y=");
    leftSide.addItem("r=");
    add(leftSide); add(text);
    
    text.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        noun=Eval.parse(text.getText(),false);
        graph.repaint();
      }
    });
    leftSide.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        polar=leftSide.getSelectedItem().
          trim().equalsIgnoreCase("r=");
      }
    });
  }
  
  public final boolean isPolar() { return polar; }
}
