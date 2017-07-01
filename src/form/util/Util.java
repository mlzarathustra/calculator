package form.util;

import java.awt.*;
import java.io.*;
import java.util.*;

/**  the eval version of form.util.Util == warning: this version has been 
 *   abridged, to avoid dependencies (eg. Platform, KeyPair)
 */

public abstract class Util {

  /** remove \r's  (grr!)
   */
  public static String
  rmCrs(String str) {
    StringBuffer strBuf=new StringBuffer(str);
    int dst=0, src=0;
    while (src<strBuf.length()) {
      if (strBuf.charAt(src) != '\r') 
        strBuf.setCharAt(dst++,strBuf.charAt(src));
      ++src;
    }
    strBuf.setLength(dst);
    return strBuf.toString();
  }

  /**	APL vector drop
  */
  public static String [] 
  drop(int howMany, String[] ary) {
    String[] r=new String[ary.length - howMany];
    for (int ai=(howMany<0)?0:howMany, ri=0;
         ri<r.length; ++ri,++ai) 	
      r[ri]=ary[ai];

    return r;
  }
  /**    APL vector take
         ### can't handle negative howMany
    */
  public static String[]
  take(int howMany, String[] ary) {
    if (ary.length==howMany) return ary;
    String[] r=new String[howMany];
    for (int ri=0; ri<howMany; ++ri) {
      if (ri>=ary.length) r[ri]="";
      else r[ri]=ary[ri];
    }

    return r;
  }

  /**    Vector "not"
  */
  public static boolean[]
  not(boolean[] vec) {
    boolean r[]=new boolean[vec.length];
    for (int i=0; i<r.length; i++) r[i]=!vec[i];
    return r;
  }

  /**    APL compress
  */
  public static String[]
  compress(boolean[] vec, String[] ary) {
    int count=0;
    for (int i=0; i<vec.length; ++i) if (vec[i]) ++count;
    String[] r=new String[count];
    for (int i=0,rp=0; i<vec.length; ++i) 
      if (vec[i]) r[rp++]=ary[i];

    return r;
  }

                                                    /*
                                  //
       //
                     various ways to get a filename
                     with or without a parent frame    //
             //
                              //
               //
   */

  public static String
  getFilename(Frame f, String mask, int mode) {
    FileDialog fd=new FileDialog(f,"open file", mode);
    if (mask==null || "".equals(mask.trim()))
      fd.setFile("*");
    else fd.setFile(mask);
    fd.setVisible(true);

    return fd.getFile();
  }
  public static String
  getLoadFilename(String mask) {
    Frame f=new Frame();
    return getLoadFilename(f,mask);
  }
  public static String
  getSaveFilename(String mask) {
    Frame f=new Frame();
    return getSaveFilename(f,mask);
  }
  public static String
  getLoadFilename(Frame f,String mask) {
    return getFilename(f,mask,FileDialog.LOAD);
  }
  public static String
  getSaveFilename(Frame f, String mask) {
    return getFilename(f,mask,FileDialog.SAVE);
  }

  /** return string containing file contents, 
      with \r's removed
  */
  public static String
  getFile(String filename) {
    byte[] data;
    try {
      File f=new File(filename);
      int size = (int) f.length();
      int bytesRead=0;
      FileInputStream in = new FileInputStream(f);
      data=new byte[size];
      while (bytesRead<size) 
        bytesRead += in.read(data,bytesRead,size-bytesRead);
    }
    catch (IOException e) {
      return e.toString();
    }
    return rmCrs(new String(data));
  }

  public static String
  doubleToString(double d) {
    String r=Double.toString(d);
    int i=r.indexOf(".");
    //  if decimal part is 0, don't include the ".0"
    if (i >= 0 && 0 == Integer.parseInt(r.substring(i+1)))
      r=r.substring(0,i);
    return r;
  }

  public static int
  doubleToInteger(double d) {
    if (d>=Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (d<=Integer.MIN_VALUE) return Integer.MIN_VALUE;
    return (int)d;
  }

    //  NB. message boxes seem to leave a thread running
  public static void
  messageBox(String title, String msg) {
    Frame f=new Frame();
    messageBox(f,title,msg);
    f.dispose();
  }
  public static void
  messageBox(Frame f, String title, String msg) {
    MessageBox m=new MessageBox(f,title,msg);
  }
  /**
      note: c can be a container or a component.
  */
  public static void
  messageBox(Container c, String title, String msg) {
    Frame frame=getFrame(c);
    if (frame == null) frame=new Frame();
    MessageBox m=new MessageBox(frame,title,msg);
  }
  public static String
  messageBox(Container c, String title, String msg, 
    String[] buttons) {
    Frame frame=getFrame(c);
    if (frame == null) frame=new Frame();
    return messageBox(frame,title,msg,buttons);
  }
  public static String
  messageBox(String title, String msg, String[] buttons) {
    Frame f=new Frame();
    String R=messageBox(f,title,msg,buttons);
    f.dispose(); // this should not be necessary
    return R;
  }
  public static String
  messageBox(Frame f, String title, String msg, String[] buttons) {
    MessageBox m=new MessageBox(f,title,msg,buttons);
    return m.result;
  }
  //  puts in \n's
  public static void
  drawString(Graphics g, String s, int x, int y) {

    FontMetrics fm=g.getFontMetrics(g.getFont());
    int lineHeight=fm.getHeight();

    StringTokenizer st=new StringTokenizer(s,"\n");
    int nTokens=st.countTokens();
    while (nTokens-->0) {
      String line=st.nextToken();
      g.drawString(line,x,y);
      y+= lineHeight;
    }
  }

  // ### catch StringIndexOutOfBoundsException
  public static String 
  delete(String str, int start, int end) {
    return str.substring(0,start) + str.substring(end);
  }
  public static String 
  replace(String str, int start, int end, 
    String replacement) {
    return str.substring(0,start) + replacement +
      str.substring(end);
  }
  public static String 
  deleteChar(String str, int i) {
    return str.substring(0,i) + str.substring(i+1);
  }

  /**
      like perl 'join' without the between char
  */
  public static String
  join(String[] ary) {
    StringBuffer sb=new StringBuffer();
    for (int i=0; i<ary.length; ++i) sb.append(ary[i]);
    return new String(sb);
  }



    //
                                  //
            //     //

      //
  //      WARNING: do not call this function from c.validate()
  //      it will cause infinite recursion.
  //
  public static void
  validateFromRoot(Component c) {
    //db.pf("validateFromRoot("+c+")"); Thread.dumpStack();
    if (c==null) return;
    c.invalidate();

    Container p=getRootContainer(c);
    if (p!=null) p.validate();
    c.repaint();
  }

  /**
        attempts to get the absolute offset
        for this component.
  */
  public static Point 
  getCumulativeLocation(Component c) {
    if (null==c) return new Point(0,0);
    Point here=c.getLocation();
    Container parent=c.getParent();
    if (null==parent) return here;
    Point parentHere=getCumulativeLocation(parent);
    return new Point(here.x+parentHere.x, here.y+parentHere.y);
  }
  /*

    //  the iterative solution is actually cleaner than the recursive one
    //  in this case.

  public static Container 
  getRootContainer(Component c) {
    Container p=c.getParent();
    if (null==p) {
      if (c instanceof Container) return (Container)c;
      else return null;
    }
    return getRootContainer(p);
  }
  */

  public static Container 
  getRootContainer(Component c) {
    if (null==c) return null;
    Container R=null;
    while ((c=c.getParent()) !=null)
      if (c instanceof Container) R=(Container)c;
    return R;
  }


  /**
    similar to the @ref getRootContainer, but returns the highest
    frame it finds.
   */
  public static Frame 
  getFrame(Component c) {
    Frame frame=null;
    while ((c=c.getParent()) !=null)
      if (c instanceof Frame) frame=(Frame)c;
    return frame;
  }

  public static String
  spaceOut(String str) {
      String R=" ";
      for (int i=0; i<str.length(); ++i) 
        R += str.charAt(i) + " ";
      return R;
  }

  public static void
  unimplemented(String msg) {
    messageBox(">> unimplemented <<",msg + 
      " has not yet been implemented.");
  }

  /*
  public static int
  getInt(TextField tf, int value) {
    int result;
    try {
       result=Integer.parseInt(tf.getText());
    }
    catch (Exception ex) {
      if ("cancel".equalsIgnoreCase(
        Util.messageBox(">> error! <<",
          tf.getText() + " isn't a valid number.",
          MessageBox.okCancel)) ) {
            tf.setText(Integer.toString(value));
      }
      if (Platform.isHotjava()) tf.requestFocus();
      return value; // reset
    }
    return result;
  }

  */
  
  /** to escape html strings
  */
  /*
  public static final KeyPair[] escapes= {
    new KeyPair("&lt;",'<'),
    new KeyPair("&gt;",'>'),
    new KeyPair("&amp;",'&'),
    new KeyPair("<BR>\n",'\n')
  };
  public static int 
  whichValue(int c, KeyPair[] list) {
    for (int i=0; i<list.length; ++i) 
      if (list[i].value == c) return i;

    return -1;
  }
  */
  /**  attempt to optimize the original
   */
  /*
  public static String
  htmlEscape(String str) {
    StringBuffer R=new StringBuffer(str);
    for (int i=0; i<R.length(); ++i) {
      
      // space: replace any blanks following with &nbsp;
      if (R.charAt(i)==' ') {
        int howmany=0;
        for (int j=i+1; j<R.length(); ++j) {
          if (R.charAt(j)==' ') ++howmany;
          else break;
        }
        
        if (howmany>0) {
          String t="";
          for (int n=0; n<howmany; ++n) t+="&nbsp;";
          // XXX kludge -- we really should delete the extra spaces
          //     wait till java 1.2
          R.insert(i,t);
          i += howmany+t.length();
        }
        
      }
      int which=whichValue(R.charAt(i),escapes);
      if (which>=0) {
        try {
          String t=escapes[which].key;
          R.setCharAt(i,t.charAt(0));
          R.insert(i+1,t.substring(1));
          i += t.length() - 1;
        }
        catch (StringIndexOutOfBoundsException e) {
        }
      } 
    }
    return new String(R);
  }
  */
  

  public static void
  drawGoldRect(Graphics g, Dimension d) {
    // inner line
    g.setColor(new Color(128,128,0));
    g.draw3DRect(3, 3, d.width - 7, d.height - 7, false);
    
    // outer line
    g.draw3DRect(0, 0, d.width - 1, d.height - 1, true);
    
    // part in between
    g.setColor(new Color(0xc19335));
    g.draw3DRect(1, 1, d.width - 3, d.height - 3, true);
    g.draw3DRect(2, 2, d.width - 5, d.height - 5, false);
  }

  public static String zeroPadLeft(int n, int howmany) {
    return zeroPadLeft(""+n, howmany);
  }
  public static String zeroPadLeft(String str, int howmany) {
    StringBuffer r=new StringBuffer(str);
    while (r.length()<howmany) r.insert(0,'0');
    return new String(r);
  }

  /**
      convert integer into its bytes, high-endian; 
      put into array, at offset.

  */
  public static void 
  toBytes(int n, byte[] ary, int off) {
    ary[off++]= (byte) (n>>24);
    ary[off++]= (byte) (n>>16);
    ary[off++]= (byte) (n>>8);
    ary[off  ]= (byte)  n;
  }
  private static final String NEWLINE = "\n";
    //System.getProperty("line.separator");

  public static void
  wrapAt80(StringBuffer sb) { wrapAt(sb,80);
  }
  /**
     word wrap
     ### messes up if there are no word breaks
     ### add an "indent" parameter
  */
  public static void
  wrapAt(StringBuffer sb, int where) {
    int lastLineStart=0, lastWordStart=0;
    boolean inWord=false;
    String str=new String(sb);  // has to be kept in line with sb
    for (int i=0; i<sb.length(); ++i) {

      if (Character.isWhitespace(str.charAt(i))) inWord=false;
      else {
        if (!inWord) { inWord=true; lastWordStart=i;
        }
      }

      if ( str.charAt(i) == '\n'  ) {
        lastLineStart=i+1; lastWordStart=i+1;
        continue;
      }

      if (i - lastLineStart > where) {  // wrap
        sb.insert(lastWordStart,NEWLINE);
        lastWordStart += NEWLINE.length();
        lastLineStart=lastWordStart;
        i += NEWLINE.length();
        str=new String(sb);
      }
    }
  }
  /**
     like >> but takes negative values as left-shifts. 

  */
  public static int rightShift(int n, int amt) {
    if (amt==0) return n;
    if (amt>0) return n >> amt;
    return n << -amt;
  }
  
  public static void centerWindow(Window w) {
    if (w == null) return;
    try {
      Dimension d=w.getSize(); 
      Dimension maxD=w.getToolkit().getScreenSize();
      w.setLocation((maxD.width - d.width) / 2, (maxD.height - d.height) / 2);
    }
    catch (NullPointerException e) {  // the window probably got disappeared
    }
  }

  
}
