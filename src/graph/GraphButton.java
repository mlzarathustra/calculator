package graph;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;

public class GraphButton extends Applet {
  Frame frame=new Frame("Equation Grapher");
  Button doIt=new Button("start Equation Grapher");
  public GraphButton() {}
  
  public void init() {
    setLayout(new BorderLayout());
    add("Center",doIt);
    frame.setLayout(new BorderLayout());
    frame.add(new GraphPanel());
    frame.setSize(640,480);
    frame.setLocation(50,50);
    doIt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        frame.setVisible(true);
        frame.toFront();
      }
    });
    
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        frame.dispose();
      }
    });
    
    
  }
}
