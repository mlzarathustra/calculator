//  evaluator 
//
//  2017 port from java
//  (c) miles zarathustra
//

function isValue(obj) {
    return (obj.isA()=='Noun' || obj.isA()=='Expression');
}


class Noun {
    constructor(v) { this.value = parseFloat(v); }
    toString() { return "Noun: { value="+this.value+" } "; }
    isA() { return 'Noun'; }
    getValue() { return this.value; }
}

var ParseAs ={
    // left-to-right operator, right-to-left operator, 
    //      function, chain, noun
    LEFT:1, RIGHT:2, FUNCTION:3, NOUN: 4
}

class Verb {
    constructor(s,p,a) {
        this.string = s;
        this.parseMode = p;
        this.act = a;

        this.precedence=0; // set later
    }

    toString() {
        return "Verb: {\""+this.string+"\" precedence "+this.precedence+
            "; parse mode: "+ ["left","right","function","chain"][this.parseMode-1]+"}";
    }
    isA() { return 'Verb'; }
 }

 class Pren {
     constructor(which) { this.which = (which=='(') ? '(' : ')'; }
     isOpen() { return this.which=='('; }
     isClose() { return this.which==')'; }
     toString() { return "Pren: "+this.which; }
     isA() { return 'Pren'; }
}

/*
    Expression.cmp (components) are in one of these forms:

        Value               rightArg
        Verb Value          action rightArg
        Value Verb Value    leftArg action rightArg

    Value is either an Expression or a Noun, as determined by the 
    isValue() fuction;

    look at eval() to see how they are used.

*/

class Expression {

    // parse formula into an array of Noun, Verb, and Pren tokens
    //
    tokenize(s) {
        let rs=[];
        while (s!=null && s.length > 0) {
            let pm=/^([\(\)])(.*)/.exec(s); 
            if (pm != null) {
                rs.push(new Pren(pm[1]));
                s=pm[2];
            }
            else {
                let nm=/^([0-9\.]+)(.*)/.exec(s);
                if (nm != null) {
                    rs.push(new Noun(nm[1]));
                    s=nm[2];
                }
                else {
                    let m=/^([A-Za-z]+|.)(.*)/.exec(s);
                    let sym=m[1].trim();
                    if (sym.length > 0) {
                        let v=Symbols[sym];
                        if (v == undefined) this.ErrorList.push('Undefined: '+m[1]);
                        rs.push(v);
                    }
                    s=m[2];
                }
            }
        }
        return rs;
    }

    // returns a number
    getValue() {
        if (action == null) return rightArg.getValue();
        if (leftArg == null) return action(rightArg);
        return action(leftArg,rightArg);
    }

    collapsePrens(tokens) {
        let rs=[];
        for (let idx=0; idx<tokens.length; ++idx) {   
            let token=tokens[idx];                

            if (token.isA() == 'Pren') { 
                if (token.isClose()) {
                    this.ErrorList.push("Unexpected ) token="+idx);
                }
                else {
                    let nest=1;
                    for (let j=idx+1; j<tokens.length; ++j) {
                        if (tokens[j].isA() == 'Pren') {
                            if (tokens[j].isOpen()) ++nest;
                            else {
                                --nest;
                                if (nest == 0) {
                                    let subExpr=new Expression(tokens.slice(idx+1,j));
                                    if (subExpr.ErrorList.length>0) {
                                        subExpr.ErrorList.forEach( 
                                            function(err) { this.ErrorList.push(err); } 
                                        );
                                        return [];
                                    }

                                    rs.push(subExpr);

                                    tokens.splice(idx,1+j-idx);
                                    --idx; 
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            else {
                rs.push(tokens[idx]);
            }
        }
        return rs;
    }

    getSymbolWithMaxPrecedence(tokens) {
        let rs=null;
        for (let token of tokens) {
            if (token.isA() == 'Verb') {
                if (rs == null || token.precedence > rs.precedence) rs=token;
            }
        }
        return rs;
    }

    addImpliedMul(tokens) {
        let rs=[];
        for (let t of tokens) {
            if (isValue(t) && rs.length>0 && isValue(rs[rs.length-1])) {
                rs.push(Symbols['*']);
            }
            rs.push(t);
        }
        return rs;
    }

    findTreeTopIdx(tokens) {
        let maxPreced = this.getSymbolWithMaxPrecedence(tokens);
        console.log('max precedence is '+maxPreced);

        if (maxPreced.parseMode == ParseAs.RIGHT) {
            for (let idx=tokens.length-1; idx>=0; --idx) {
                if (tokens[idx].isA()=='Verb' && tokens[idx].precedence == maxPreced.precedence) {
                    return idx;
                }
            }
        }
        else {
            for (let idx=0; idx<tokens.length; ++idx) {
                if (tokens[idx].isA()=='Verb' && tokens[idx].precedence == maxPreced.precedence) {
                    return idx;
                }
            }
        }



        // not reached
    }


    // construct Expression from string or token list 
    //
    constructor(arg) { 
        this.ErrorList=[];
        this.leftArg=null;  // see comment above class decl
        this.rightArg=null;
        this.action=null;

        let tokens;
        if (typeof(arg) == 'string') {
            tokens=this.tokenize(arg);
            if (this.ErrorList.length > 0) return; 
        }
        else tokens=arg;

        tokens = this.collapsePrens(tokens);
        console.log('tokens, after collapsePrens'+tokens);

        tokens = this.addImpliedMul(tokens);
        console.log('tokens, after addImpliedMul'+tokens);

        let treeTopIdx = this.findTreeTopIdx(tokens);
        console.log('treeTopIdx is '+treeTopIdx);

        /* 
                    TODO

                    i think all that's left is to make a tree with the 
                    new Expression(left tokens) action new Expression(right tokens) 
                    
                    The precedence is reversed. It should get the min, not the max.
                    and the RIGHT/LEFT parsing should be correct.
        
        
        */


    }


    toString() { return "Expression: { leftArg:"+this.leftArg+'; action='+this.action+
        '; rightArg:'+this.rightArg +
    
    "}"; }
    isA() { return 'Expression'; }


}


// \\// \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\


let _verbs = // outer dimension is precedence
[
    [
        new Verb('+',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 0; // identity
                    case 1: return a;
                    case 2: return a+b;
                }
                return 0;
            }
        ),
        new Verb('-',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 0; // identity
                    case 1: return -a;
                    case 2: return a-b;
                }
                return 0;
            }
        )
    ],    
    [
    new Verb('/',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 1; // identity
                    case 1: return 1/a;
                    case 2: return a/b;
                }
                return 0;
            }
        )
        ,
        new Verb('*',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 1; // identity
                    case 1: return Math.sign(a);
                    case 2: return a*b;
                }
                return 0;
            }
        )
    ],
    [
        new Verb('^',ParseAs.RIGHT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 1; // identity
                    case 1: return Math.exp(a);
                    case 2: return Math.pow(a,b);
                }
                return 0;
            }
        )
    ],
    [   
        new Verb( 'sqrt', ParseAs.FUNCTION, function(n) { return Math.sqrt(n); } ),
        new Verb( 'sin', ParseAs.FUNCTION, function(n) { return Math.sin(n); } ),
        new Verb( 'cos', ParseAs.FUNCTION, function(n) { return Math.cos(n); } ),
        new Verb( 'tan', ParseAs.FUNCTION, function(n) { return Math.tan(n); } ),
        new Verb( 'cot', ParseAs.FUNCTION, function(n) { return Math.cot(n); } )
    ]

]

var Symbols = {
        pi: new Noun( Math.PI ),
        e: new Noun(Math.E)
};

function initSymbols() {
    for (prec=0; prec<_verbs.length; ++prec) {
        for (let v of _verbs[prec]) {
            v.precedence = prec;
            Symbols[v.string] = v;
        }
    }
}

initSymbols();





function showTokens(id) {
    let el=document.getElementById(id);
    let s=el.value;
    console.log('s is '+s);
    let expr=new Expression(s);
    console.log(expr);

    if (expr.ErrorList.length>0) {
        console.log('ERROR(S)');
        expr.ErrorList.forEach( function(msg) { console.log(msg); });
    }
}






