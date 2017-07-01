package form.util;

import java.util.*;

/**
     print stuff.
     
     p.c(String, Object) //  show class, with message   <br>
     p.l(Object) // print line				<br>

     p.s(... ) // print Object.toString(), Vector, or Object array.
*/

public class p {
  public static void
  c(String m, Object o) {
    s(m + o.getClass().toString());
  } 
  public static void l(Object o) {
    System.out.println(o.toString());
  }

  public static void s(Object o) {
    System.out.print(o.toString());
  }
  
  public static void l(Vector v) { s(v,true); }   // put a line between each element
  public static void s(Vector v) { s(v,false); }  // all on the  same line
  public static void
  s(Vector v, boolean addLine) {
    int i=0;
    Enumeration e=v.elements();
    while (e.hasMoreElements()) 
      s(" ["+i++ + "]= " + e.nextElement() + (addLine?"\n":""));
    
  }
  public static void
  s(Object[] ary) {
    for (int i=0; i<ary.length; ++i) s(" ["+i++ + "]= " + ary[i]);
  }

}
