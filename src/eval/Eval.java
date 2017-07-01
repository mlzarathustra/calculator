package eval;

import java.util.*;
import java.io.*;


import form.util.*;
import eval.action.*;

public class Eval {
  Noun root;
  static double roundFactor=10e14;
  
  Eval(String s) { root=parse(s);   }
  
  /**  Dynamically evaluate
   */
  double eval() { return root.value(); }
  
  public static Noun parse(String s) { return parse(s,true); }
  public static Noun parse(String s, boolean useStdout) {
    Noun n=null;
    try {
      n=makeNoun(makeTokenList(s)); 
    }
    catch (ExpressionSyntaxException e) {
      if (useStdout) p.l("parse: "+e);
      else Util.messageBox(">> error <<","parse: "+e);
    }
    return n;
  }
  
  /**  guaranteed to return a string value
   */
  public static String parseString(String s) {
    Noun n=null;
    try {
      n=makeNoun(makeTokenList(s)); 
    }
    catch (ExpressionSyntaxException e) {
      return("parse: "+e);
    }
    if (n == null) return "";
    
    double r=n.value();
    r = ((double)Math.round(r * roundFactor)) / roundFactor;
    return ""+r;
  }
  
  /**  convert string into a series of tokens
   */
  public static Vector makeTokenList(String s) throws ExpressionSyntaxException {
    Vector list=new Vector(); // of Verbs, Nouns
    ExpressionTokenizer et=new ExpressionTokenizer(s);
    
    for (;;) { 
      ExpressionToken token=et.nextToken();
      if (token == null) break;
      list.addElement(token);
      
      //p.l("" + token);
    }
    return list;
  }
  
  /**  the major spaghetti -- turn the flat list
   *   into a recursive hierarchy, according to precedence and grouping.
   */
  public static Noun makeNoun(Vector list) throws ExpressionSyntaxException {
    if (list.size() == 0) return null;
    if (list.size() == 1) {
      if (list.elementAt(0) instanceof Noun) return (Noun)list.elementAt(0);
      else throw new ExpressionSyntaxException(
        "phrase cannot end in a verb.");
    }
    
    // step one: collapse function arguments
    //  XXX assume only one argument (so root 2,3 won't work)
    for (int i=0; i<list.size(); ++i) {
      Object here=list.elementAt(i);
      if (here instanceof Verb && 
        ((Verb)here).parseMode == Verb.FUNCTION) {
        
        Verb verb=(Verb)here; // save it, because it gets removed
        
        Vector subList=new Vector();
        for (int j=i+1; j<list.size(); ++j) 
          subList.addElement(list.elementAt(j));
        
        list.setSize(i);
        list.addElement(new Noun(verb, makeNoun(subList)));
      }
    }
    
    // step 2: insert multiply between consecutive nouns
    for (int i=0; i < list.size()-1; ++i) {
      if (list.elementAt(i) instanceof Noun &&
          list.elementAt(i+1) instanceof Noun) 
        list.insertElementAt(Globals.symbolTree.find("*").token, i+1);
    }
    
    // step 3: (R->L) where there are 2 consecutive verbs,
    //         or a verb at the beginning,
    //         combine the rightmost with the adjacent noun.
    //
    //         repeat until the sequence is N V N V N V
    //
    if (! (list.lastElement() instanceof Noun)) 
        throw new ExpressionSyntaxException (
          "phrase cannot end in a verb");
    
    for (int i=list.size()-2; i >= 0; --i) {

      if (list.elementAt(i) instanceof Verb &&
        (i==0 || list.elementAt(i-1) instanceof Verb)) {
           Noun n=new Noun((Verb)list.elementAt(i), (Noun)list.elementAt(i+1));
           list.removeElementAt(i); 
           list.setElementAt(n,i);
      }
    }
    
    
    
    // step 4: starting with the highest precedence, 
    //         and according to grouping,
    //         group each N V N sequence into a N
    //         until only one N remains.
    //     optimization: in a sequence of like operators
    //         (eg. 2 + 3 + 4) combine the set into one noun.
    
    // typical case: group left to right.
    //   XXX add logic for R->L grouping, for ^
    
    for (;;) {
      if (list.size() == 1) return (Noun)list.elementAt(0);
      int highest = -1, pos=-1;
      for (int i=0; i<list.size(); ++i) {
        try {
          if (((Verb)list.elementAt(i)).precedence > highest) {
            pos=i; highest=((Verb)list.elementAt(i)).precedence;
          }
        }
        catch (ClassCastException e) {
        }
      }
      try {
        Noun n=new Noun(
          (Noun)list.elementAt(pos-1), 
          (Verb)list.elementAt(pos),
          (Noun)list.elementAt(pos+1));
          
        list.removeElementAt(pos-1);  
        list.removeElementAt(pos-1);  
        list.removeElementAt(pos-1);  
        list.insertElementAt(n,pos-1);
          
      }
      catch (ArrayIndexOutOfBoundsException e) {
        throw new ExpressionSyntaxException("syntax error.");
      }
      //p.l("highest precedence: "+highest+"; pos="+pos);
    }
  }
  
  public static void main(String[] args) {
    BufferedReader in=new BufferedReader(new InputStreamReader(System.in));      
      
    for (;;) {
      String input="";
      try { input=in.readLine(); }
      catch (IOException e) { }
      if (0==input.trim().length()) break;
      /*
      Noun R=parse(input);
      if (R != null) p.l("result: "+R.value());
      // else an error was shown.
      */
      p.l("result: " + parseString(input));
      
    }
  
  }
}


