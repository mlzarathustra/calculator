package eval;

import eval.action.*;

/**  a verb can be a function or an operator
 */   
public class Verb implements ExpressionToken {
  String string;
  int parseMode, precedence, nArgs; // nArgs only applies to functions
  Action doIt;
  
  /** possible parseFrom values:
   */
  static final int 	LEFT		= 0, 
  			RIGHT		= 1,
                        FUNCTION        = 2, // f(x,...)
  			CHAIN		= 3  // eg. a<b<c
                        ; 
  Verb(String s, Action doIt) { this(s,LEFT,doIt);
  }
  /**  default number of args = 1
   */
  Verb(String s, int p, Action doIt) { this(s,p,doIt,1); }
  Verb(String s, int p, Action doIt, int n) {
    string=s; parseMode=p; this.doIt=doIt; nArgs=n;
  }
  
  public String toString() {
    return "Verb: \""+string+"\" precedence "+precedence+"; parse mode: "+
      (new String[] {"left","right","function","chain"})[parseMode];
  }
}

