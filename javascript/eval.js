//  evaluator 
//
//  2017 port from java
//  (c) miles zarathustra
//

class Noun {
    constructor(v) { this.value = parseFloat(v); }
    toString() { return "Noun: { value="+this.value+" } "; }
    isA() { return 'Noun'; }
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

//
//



// parse formula into an array of (String) tokens
//
function tokenize(s) {
    let rs=[];
    while (s.length > 0) {
        let m=/([A-Za-z]+|[0-9\.]+|.)(.*)/.exec(s);
        if (m[1].trim().length > 0) rs.push(m[1]);
        s=m[2];
    }
    return rs;
}

function showTokens(id) {
    let el=document.getElementById(id);
    let s=el.value;
    console.log('s is '+s);
    let tokens=tokenize(s);
    console.log(tokens);
}






