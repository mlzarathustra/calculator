package eval;

import form.renderer.*;
import form.util.*;

public class ExpressionTokenizer extends Analyze {
  String what;
  int here, next;
  
  ExpressionTokenizer(String w) { what=w; here=0; }
 
  /**  returns a noun or verb, the next token.
   */
  ExpressionToken nextToken() throws ExpressionSyntaxException {
    SymbolFindResult fn;
    
    try {
      while (isSpace(what.charAt(here))) ++here;
      if (isSign(what,here) || isNumeric(what.charAt(here))) {  //  number
        next=here; 
        while (++next < what.length()) if (!isNumeric(what.charAt(next))) break;
        double n=0;
        try {
           n=Double.valueOf(what.substring(here,next)).doubleValue();
        }
        catch (NumberFormatException e) {
          throw new ExpressionSyntaxException(
            "bad number: "+what.substring(here,next));
        }
        here=next;
        return new Noun(n);
      }                
      else if (null != (fn=Globals.symbolTree.find(what,here)).token) { //  verb / symbol
        here += fn.length;
        return fn.token;
      }
      else if (isLeftBracket(what.charAt(here))) {                   //  bracketed phrase
        next=nextRightPren(what,here);
        Noun R=Eval.parse(what.substring(here+1,next));
        here=next+1;
        return R;
      }
      else 
        throw new ExpressionSyntaxException(
          "unknown symbol: " + what.substring(here));
    }
    catch (StringIndexOutOfBoundsException e) {
    }
    
    return null;
  }
  
  
}
     