
import java.awt.*;
import java.awt.event.*;
import java.applet.*;

import eval.*;
import gjt.*;

public class EvalApplet extends Applet {
  TextArea output;
  TextField input;
  public void init() {
    setLayout(new ColumnLayout(Orientation.CENTER, Orientation.TOP));
    add(output=new TextArea(10,60));
    output.setEditable(false);
    output.setBackground(Color.white);
    output.setFont(new Font("Serif",Font.PLAIN, 16));
    
    add(new Label("type formulae for evaluation into the below text field:"));
    add(input=new TextField(60));
    input.setFont(new Font("Serif",Font.PLAIN, 16));
    
    input.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        output.append(input.getText()+"\n");
        output.append(Eval.parseString(input.getText())+"\n\n");
        input.setText("");
      }
    });
  }
}
