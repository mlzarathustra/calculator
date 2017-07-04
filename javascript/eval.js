//  evaluator 
//
//  2017 port from java
//  (c) miles zarathustra
//

class Value { getValue() { return null; } }

class Noun extends Value {
    constructor(v) { super(); this.value = parseFloat(v); }
    toString() { return "Noun: { value="+this.value+" } "; }
    isA() { return 'Noun'; }
    getValue() { return this.value; }
}

var ParseAs ={
    // left-to-right operator, right-to-left operator, 
    //      function, chain, noun
    LEFT:1, RIGHT:2, FUNCTION:3, CHAIN:4, NOUN: 5
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

class Expression extends Value {

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
                        s=m[2];
                    }
                }
            }
        }
        return rs;
    }
    
    // construct Expression from string or token list 
    //
    constructor(arg) { 
        super();
        this.ErrorList=[];
        this.list=[];
        let tokens;
        if (typeof(arg) == 'string') {
            tokens=this.tokenize(arg);
            if (this.ErrorList.length > 0) return; 
        }
        else tokens=arg;

        for (let i=0; i<tokens.length; ++i) {
            let token=tokens[i];
            if (token.isA() == 'Pren') {  
                if (token.isClose()) {
                    this.ErrorList.push("Unexpected ) token="+i);
                }
                else {
                    let nest=1;
                    for (let j=i+1; j<tokens.length; ++j) {
                        if (tokens[j].isA() == 'Pren') {
                            if (tokens[j].isOpen()) ++nest;
                            else {
                                --nest;
                                if (nest == 0) {
                                    let subExpr=new Expression(tokens.slice(i+1,j));
                                    if (subExpr.ErrorList.length>0) {
                                        subExpr.ErrorList.forEach( 
                                            function(err) { this.ErrorList.push(err); } 
                                        );
                                        this.list=[];
                                        return;
                                    }
                                    this.list.push(subExpr);
                                    tokens.splice(i,1+j-i);
                                    --i; 
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            else {

                //  TODO - implement
                /*
                  Expression should be one of:
                    Value
                    Verb Value
                    Value Verb Value

                    Value is either an Expression or a Noun (via getValue())

                */
                this.list.push(tokens[i]);
            }
        }
    }

    getValue() {
        //  TODO - implement
    }



    toString() { return "Expression: {"+ this.list.join(', ') +"}"; }
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






