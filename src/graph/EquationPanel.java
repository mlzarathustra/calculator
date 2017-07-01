package graph;

import java.awt.*;
import java.awt.event.*;

import gjt.*;

import form.util.*;
import eval.*;
/**
 *   contains a series of EquationEntry components,
 *   and a reference to an EvalGraph object (graph).
 * 
 *   When one of the EquationEntries changes, 
 *   we notify "graph" to update (or add or erase) 
 */
public class EquationPanel extends Panel {
  Button addEquation         =new Button("add equation"), 
         removeEquation      =new Button("remove equation"),
         graphIt             =new Button("graph it!");
  
  TextField polarMinField=new TextField("0",10), 
    polarMaxField=new TextField("2 pi",10), 
    polarIncrementField=new TextField(".1", 10);
  boolean polarChanged;
  double polarMin, polarMax, polarIncrement;
  
  EquationPanel me;
  EvalGraph graph;
    
  EquationPanel(EvalGraph g) {
    graph=g;
    g.setEquationPanel(this);
    me=this;
    setLayout(new ColumnLayout());
    
    Panel buttonPanel=new Panel();
    buttonPanel.add(addEquation);
    buttonPanel.add(removeEquation);
    buttonPanel.add(graphIt);
    addEquation.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        add(new Equation(graph));
        Util.validateFromRoot(me);
      }
    });
    removeEquation.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Component c=getComponent(getComponentCount()-1);
        if (c instanceof Equation) {
          remove(c);
          Util.validateFromRoot(me);
        }
      }
    });
    graphIt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        graph.repaint();
      }
    });
    
    Panel polarPanel=new Panel();
    polarPanel.add(new Label("polar min:"));
    polarPanel.add(polarMinField);
    polarPanel.add(new Label("polar max:"));
    polarPanel.add(polarMaxField);
    polarPanel.add(new Label("polar increment:"));
    polarPanel.add(polarIncrementField);
    
    polarMinField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) { setPolarMin(); }
    });
    polarMaxField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) { setPolarMax(); }
    });
    polarIncrementField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) { setPolarIncrement(); }
    });
    polarMinField.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent evt) { polarChanged=true; }
    });
    polarMaxField.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent evt) { polarChanged=true; }
    });
    polarIncrementField.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent evt) { polarChanged=true; }
    });
    
    polarChanged=true; // force initialize
    
    add(buttonPanel);
    add(polarPanel);
    add(new Equation(graph));
  }
  
  void setPolarMin() {
    Noun n=Eval.parse(polarMinField.getText(),false);
    if (n != null) {
      polarMin=n.value(); graph.repaint();
    }
  }
  void setPolarMax() {
    Noun n=Eval.parse(polarMaxField.getText(),false);
    if (n != null) {
      polarMax=n.value(); graph.repaint();
    }
  }
  void setPolarIncrement() {
    Noun n=Eval.parse(polarIncrementField.getText(),false);
    if (n != null) {
      polarIncrement=n.value(); graph.repaint();
    }
  }
  void setPolarParameters() { 
    if (polarChanged) {
      setPolarMin(); setPolarMax(); setPolarIncrement(); 
      polarChanged=false;
    }
  }
  
}
