package eval;

import java.io.*;
import form.util.*;
import form.renderer.*;
import eval.action.*;

/**
 * This class provides the structure for list of symbols,
 * in the form of a recursive tree.  The find() method (below) is the key.
 * <p>
 * 
 * This is the eval version -- follows the same 'template' as the 
 * form.renderer version, but with different specific types.
 * <p>
 * 
 * @see SymbolNode#find
 * @see SymbolFindResult
 */
public class SymbolNode {
  /** only defined in leaf; else =0
   */ 
  private ExpressionToken token;  
  
  /**
   * A table of characters which might occur next in a valid 
   * symbol word.  (or null, if invalid)
   * 
   * <P> This represents the set of visible ASCII, ie. the characters
   * above 32.  NOTE: there IS an offset of 32, which must be 
   * taken into account when building this table.
   */
  public SymbolNode []nextKeyChar=new SymbolNode[96]; 
  
  /**
   * Create a node containing a symbol.
   * note that this is not necessarily a leaf.
   */
  public SymbolNode(ExpressionToken t) { token=t;
  }
  /**
   * Create an intermediary node, with no symbol associated.
   */
  public SymbolNode() {
  }
  
  public String toString() {
    StringBuffer R=new StringBuffer("SymbolNode: [token="+token+
      "; next=[ ");
    for (int i=0; i<nextKeyChar.length; ++i) {
      if (nextKeyChar[i]!=null) R.append(""+((char)(i+32))+" ");
    }
    R.append("]");
    return new String(R);
  }
    
  
  /**  find symbol defined from first char of this string.
   * If str starts with an alphabetic character, then 
   * trailing adjacent alpha characters cause the match to fail.
   * (eg. pie doesn't match pi)
   * 
   * @return: if present, the symbol; else null
   */
  public synchronized SymbolFindResult find(String str) { return find(str,0); }
  
  /**
   * find the symbol at str[idx] within this tree.
   * If str starts with an alphabetic character, then 
   * trailing adjacent alpha characters cause the match to fail.
   * (eg. pie doesn't match pi)
   * 
   * @return: if present, the symbol; else null
   */
  public synchronized SymbolFindResult
  find(String str, int idx) {
    //return find(str,idx,Analyze.isAlpha(str.charAt(idx)));
    try {
      return find(str,idx,Analyze.isAlpha(str.charAt(idx)));
    }
    catch (Exception e) {
      p.l("SymbolNode.find: "+e);
      return new SymbolFindResult(null);
    }
  }
  
  /**
   * find the symbol at str[idx] within this tree.
   */
  private SymbolFindResult
  find(String str, int idx, boolean isAlpha) {
    //db.pf("find... str="+str+"; idx="+idx+"; this="+this+";  isAlpha:"+isAlpha);
    
    SymbolFindResult R=null; 
    SymbolNode next=null;
    char nextChar=0;
    
    //  I think it's quicker to catch than to test  :-)
    try { 
      nextChar=str.charAt(idx);         
      next=nextKeyChar[nextChar-32];    
    }
    catch (ArrayIndexOutOfBoundsException e) {  // nextChar-32 >= nextKeyChar.length
      return new SymbolFindResult(null);
    }
    catch (StringIndexOutOfBoundsException e) { // str.length <= idx
    }

    if (token != null) {                      // this node is POTENTIAL TERMINATOR
      try {
        if (next==null) {                       // LEAF node
          if (isAlpha && Character.isLetter(nextChar))
            return new SymbolFindResult(null);    // trailing alpha -- no match
          return new SymbolFindResult(token);     // this node matches
        }
        
        R=next.find(str,idx+1, isAlpha);        // INTERMEDIATE node
        if (R.token==null) {
          if (isAlpha && Character.isLetter(nextChar))
            return new SymbolFindResult(null);    // trailing alpha -- no match
          return new SymbolFindResult(token);     // this node matches
        }
      }
      catch (StringIndexOutOfBoundsException e) {
                                                //  End of string -- no alpha after, 
        return new SymbolFindResult(token);     //  therefore match succeeds.
      }
      
      R.length++;  return R;                    // a node below matched
    }
                                            //  this node CANNOT TERMINATE

    if (next==null) return new SymbolFindResult(null); // nothing below -- fail
    
    R=next.find(str,idx+1, isAlpha);                 // recurse 
    R.length++; return R;
  }

  public boolean add(Verb v) { return add(v.string, v); }
 
  /**
   * add this symbol to the tree, creating nodes as necessary
   * or attaching to existing nodes.  
   * 
   * If the key already exists with a different symbol, it fails.
   * 
   * @return success (t/f)
   */
  public boolean add(String key, ExpressionToken t) {
    if (key.length()==0) { 
      if (token == null) { token=t; return true;
      }
      else return token == t;
    }
    if (nextKeyChar[key.charAt(0)-32]==null)
      nextKeyChar[key.charAt(0)-32]=new SymbolNode();
    return nextKeyChar[key.charAt(0)-32].add(key.substring(1),t);
  }
  
  
  public static void main(String [] args) {
    p.l("symbol table: + = add - = subtract * = multiply / = divide");
    /*
    SymbolNode s0=new SymbolNode();
    s0.add(new Verb("+",new Plus()));
    s0.add(new Verb("-",new Minus()));
    */
    
    
    /*
    System.out.println("symbol table: pi=10, pie=20, peace=30, <>=40");
    SymbolNode s0=new SymbolNode();
    s0.add("pi",10);
    s0.add("pie",20);
    s0.add("peace",30);
    s0.add("<>",40);
    */
    
    /*
    SymbolNode s0, s1, s2, s3, s4;
   
    s0=new SymbolNode(); 
    s0.nextKeyChar['<'-32]=s1=new SymbolNode();
    s1.nextKeyChar['>'-32]=new SymbolNode(40);     // <>
    
    s0.nextKeyChar['p'-32]=s1=new SymbolNode();
    s1.nextKeyChar['e'-32]=s2=new SymbolNode();
    s2.nextKeyChar['a'-32]=s3=new SymbolNode();
    s3.nextKeyChar['c'-32]=s4=new SymbolNode();
    s4.nextKeyChar['e'-32]=new SymbolNode(30);     // peace
    
    
    s1.nextKeyChar['i'-32]=s2=new SymbolNode(10);  //  pi
    s2.nextKeyChar['e'-32]=new SymbolNode(20);     //  pie
    */
    
    BufferedReader in=new BufferedReader(new InputStreamReader(System.in));      
      
    for (;;) {
      String input="";
      try {
       input=in.readLine();
      }
      catch (IOException e) {
      }
      if (0==input.trim().length()) break;
      //SymbolFindResult R=s0.find(input,0);
      SymbolFindResult R=Globals.symbolTree.find(input,0);
      System.out.println("result: "+R);
    }
  
  }
}
