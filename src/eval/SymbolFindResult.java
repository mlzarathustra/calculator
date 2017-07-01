package eval;

 /**
  * The eval version -- follows the same 'template' as the form.renderer version,
  * but different specific types.
  * <p>
  * 
  * the result from a SymbolNode.find()
  * 
  * <P>
  * symbol is either the value, the symbol found 
  * (a unicode value, disguised as an int)
  * or -1 if not found.
  * 
  * <P>
  * length is how long the key word was (eg sqrt --> 4)
  * but is only meaningful if symbol != -1.
  * 
  * @see SymbolNode#find
  */
public class SymbolFindResult {
  public int length;
  public ExpressionToken token;
  
  SymbolFindResult(ExpressionToken t) { this(t,0);
  }
  SymbolFindResult(ExpressionToken t, int l) { 
    token=t; length=l;
  }
  boolean found() { return token != null; }
  
  public String toString() {
    return "SymbolFindResult [token="+token+"; length="+length+"]";
  }
  
}
