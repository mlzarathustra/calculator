package eval;

/**
 
  a noun is a thing a verb acts on.
  it may be an atom, or an expression consisting of 
  nouns and verbs.

  the 'value' function always resolves it to a double number.
*/

public class Noun implements ExpressionToken {
  int type;
  
  // possible type values:
  static final int    ACTION    = 0,
		      NUMBER    = 1,
		      VARIABLE  = 2;
  private Verb[] verbs;
  private Noun[] nouns;
  private double number;
  private String key;
  
  public Noun(double n) { type=NUMBER; number=n; }
  public Noun(Verb v) { 
    verbs=new Verb[1]; verbs[0]=v;
    type=ACTION;
  }
  public Noun(Verb v, Noun n) { 
    this(v); 
    nouns=new Noun[] { n };
  }
  public Noun(Noun L, Verb v, Noun R) {
    this(v); 
    nouns=new Noun[] { L,R };
  }
  
  public void setNumber(double n) { 
    type=NUMBER; number=n;
  }

  /**  dynamically evaluate a compound expression
   */
  double act() {
    if (verbs[0].parseMode==Verb.CHAIN) return evalChain();
    return evalSingle();
  }
   
  /**  evaluates an expression with a single verb.
   */
  double evalSingle() {
    double[] values=new double[nouns.length];
    
    // dynamically recurse
    for (int i=0; i<nouns.length; ++i) 
      values[i]=nouns[i].value();
    
    // use the action (doIt) to evaluate our node's value.
    return verbs[0].doIt.value(values);
  }

  /**  evaluates an expression with a chain of verbs
   */
  double evalChain() {
    // will return a boolean value :  a < b < c  -->  a<b && b<c
    //  NOTE: there is no specifically boolean return value.
    // ### implement
    return 0.0; 
  }

  /**  dynamically evaluate (or simply return value if we're a leaf node)
   */
  public double value() {
    switch (type) {
      case NUMBER: return number;
      case VARIABLE: return ((Noun)Globals.symbolTree.find(key).token).value(); 
      case ACTION: return act();
    }
    return 0.0;
  }
  
  public String toString() {
    if (type == NUMBER) return "Noun: "+number;
    if (type == VARIABLE) return "Noun: variable "+key+"="+value();
    
    StringBuffer r=new StringBuffer("Noun: action");
    r.append("\n  Verbs: ");
    for (int i=0; i<verbs.length; ++i) r.append(verbs[i]+"; ");
    r.append("\n  Nouns: ");
    for (int i=0; i<nouns.length; ++i) r.append("["+i+"]="+nouns[i]+"; ");
    return new String(r);
  }
}
