package eval;

import java.util.*;
import eval.action.*;

public class Globals {
  
  // listed in precedence order, lowest first
  //
  static Verb[][] verbs = {
    /*
    {
      new Verb(">",Verb.CHAIN, null),
      new Verb(">=",Verb.CHAIN, null),
      new Verb("=",Verb.CHAIN, null),
      new Verb("<=",Verb.CHAIN, null),
      new Verb("<",Verb.CHAIN, null),
      new Verb("!=",Verb.CHAIN, null),
      new Verb("~=",Verb.CHAIN, null),
    },
    */
    { 
      new Verb("+",Verb.LEFT, new Plus()),
      new Verb("-",Verb.LEFT, new Minus()),
    },
    {
      new Verb("*",Verb.LEFT, new Times()),
      new Verb("/",Verb.LEFT, new Over()),
    },
    {
      new Verb("^",Verb.RIGHT, new Power()),
    },
    {
      //  functions don't have a particular precedence.
      //
      new Verb("sin",   Verb.FUNCTION, new TrigSin()),
      new Verb("cos",   Verb.FUNCTION, new TrigCos()),
      new Verb("tan",   Verb.FUNCTION, new TrigTan()),
      new Verb("cot",   Verb.FUNCTION, new TrigCot()),
      new Verb("sqrt",  Verb.FUNCTION, new Sqrt()),
      
      //new Verb("sigma",Verb.FUNCTION,null),
    }
  };

  public static SymbolNode symbolTree=new SymbolNode();      
  
  static {
    for (int prec=0; prec<verbs.length; ++prec) {
      for (int i=0; i<verbs[prec].length; ++i) {
        verbs[prec][i].precedence=prec;
        symbolTree.add(verbs[prec][i]);
      }
    }
    symbolTree.add("pi",new Noun(Math.PI));
    symbolTree.add("e", new Noun(Math.E));
  }

}
