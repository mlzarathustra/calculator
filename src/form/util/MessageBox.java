//
//    the jdk 1.0 version of message box
//
package form.util;

import java.awt.*;
import java.awt.event.*;

public class MessageBox extends Dialog {
    protected MultiLineLabel label;
    
    /** result will be set to the text of the button pressed
     */
    public String result=null; 
    
    /**  some typical button sets.
     */
    public static String[] ok={"ok"},okCancel={"ok","cancel"};

    public 
    MessageBox(Frame parent, String title, String message, 
      String[] buttons) {
        super(parent, title, true);
        setLayout(new BorderLayout(15, 15));
        label = new MultiLineLabel(message, 20, 20);
        add("Center", label);

        // add each of the buttons specified.
        Panel p = new Panel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        for (int i=0; i<buttons.length; ++i) {
          Button b=new Button(buttons[i]);
          b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
              result=((Button)evt.getSource()).getLabel();
              dispose();
            }
          });
          p.add(b);
        }  
        add("South", p);        
        addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent evt) { dispose();
          }
        });

	setLocation(100,100); // ###  center it
        pack();
        setVisible(true);
    }

   
    public 
    MessageBox(Frame parent, String title, String message) {
      this(parent,title,message,ok);        
    }
    
    public static void 
    main(String[] args) {
        Frame f = new Frame("MessageBox Test");
        MessageBox d = new MessageBox(f, ">> test <<", 
	  "This is a test of the MessageBox function.\n"+
	  "It can accept multiple lines.\n"+
	  ".\n.\n.\n.\n");
        
        System.out.println("result: "+d.result);

    }
}
