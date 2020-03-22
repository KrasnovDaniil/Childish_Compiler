package me.dev.Frontend.LangParser;

import me.dev.Frontend.LexicalAnalyzer.Token;
import me.dev.Frontend.LexicalAnalyzer.TokenType;

import java.util.ArrayList;
import java.util.Stack;

public class Parser {

    private static ArrayList<Token> tokensProgram;
    static int LEN;
    static int _i;
    static Token Currtoken;

    public Parser(ArrayList<Token> tokens){
        _i=0;
        tokensProgram = new ArrayList<Token>();
        tokensProgram = tokens;
        LEN = tokensProgram.size();
        Currtoken = peekToken(0);
    }

    //      ###___beginning of the parsing___####

    public static ExprNode MainProcess(){
        return ParseExpression();
    }


    public static ExprNode ParseStmt(){
        nextToken();
        ExprNode p1, p2;
        p1 = ParseSimple();
        p2 = ParseLangOperators(); // parse IF and WHILE
        if(p1 != null) return p1;
        if(p2 != null) return p2;
        return null;
    }

    public static ExprNode ParseSimple(){
        // stmt -> VAR_decl| stmt_block | expr| if_stmt| while_stmt| "return" [expr];| "break";| ;
        // this method should parse VAR, NUM, LPAR, COMMA, SEMICOLON, EOF
        switch (Currtoken.type) { // here may be all tokens: IF,.. ,VAR,NUM,
            case VAR:
                return ParseIdentifier();
            case NUM:
                return ParseNumberExpr(); // arithmetic expression
            case LPAR:
                return ParseParenExpr();
            case COMMA: break;
            case EQ: return ParseStmt();
            case SEMICOLON: break;
            case EOF: return null;
            default:
                if (Currtoken.type.isBinary()){
                    ParseArithmeticExpr();
                }
        }
        return null;
    }

    public static ExprNode ParseLangOperators(){
        switch (Currtoken.type){
            case IF: return parseIFExpr();
            case WHILE: return parseWHILEExpr();
        }
        return null; // if token is not a lang operator
    }


    public static ExprNode ParseNumberExpr(){
        // if next token is an binary operation, then process arithmetic expression
        if (peekToken(0).type.isBinary())
            return ParseArithmeticExpr();
        return new NumberNode(Currtoken.val);
    }

    // parsing paren expressions
    public static ExprNode ParseParenExpr(){
        if (Currtoken.type != TokenType.LPAR)
            ErrorMsg("expected \"(\" ");
        nextToken();
        ExprNode parexpr = ParseExpression();

        if (peekToken(0).type != TokenType.RPAR)
            ErrorMsg("expected \")\"");
        nextToken();
        return parexpr;
    }

    // parsing typical expressions
    public static ExprNode ParseExpression(){
        if (Currtoken.type == TokenType.EOF) return null;

        ExprNode leftPart = ParseStmt();
        ExprNode rightPart = ParseStmt();
        return new ExprNode(leftPart, rightPart);
    }

    // processing arithmetic expression with RPN
    public static ExprNode ParseArithmeticExpr() {
        ArrayList<Token> expr = new ArrayList<>();
        // parsing boolean expressions
        TokenType tokentype;

        do{ expr.add(Currtoken);
            tokentype = nextToken().type;}
        while (!tokentype.isBooleanOper() && tokentype != TokenType.SEMICOLON );
        ArrayList<Token> ans = ConvertToRPN(expr); // works properly
        ExprNode exprNode = RPNtoTree(ans);
        return exprNode;
//        BinOperatorNode binNode = new BinOperatorNode(Currtoken.basetext, peekToken(-1), peekToken(1));
    }

    public static ExprNode RPNtoTree(ArrayList<Token> rpn1){ // complete!
        ArrayList<Object> rpn = ObjectizeList(rpn1);
        Object op1, op2;
        ExprNode op1toEN, op2toEN;
        Token oper;
        int i=0;
        while(rpn.size()!=1) {
            oper = (Token)rpn.get(i);
            if (oper.type.isBinary()){
                op1 = rpn.remove(i-2);
                op2 = rpn.remove(i-2);
                // transform simple numbers to NumNode()
                if (op1 instanceof Token && ((Token)op1).type == TokenType.NUM) op1 = new NumberNode(((Token)op1).val);
                if (op2 instanceof Token && ((Token)op2).type == TokenType.NUM) op2 = new NumberNode(((Token)op2).val);
                rpn.set(i-2, new BinOperatorNode(oper.type,(ExprNode)op1,(ExprNode) op2));
                i-=2;
            }
            ++i;
        }
        System.out.println();
        return (ExprNode)rpn.get(0);
    }

    public static ArrayList<Object> ObjectizeList(ArrayList<Token> tokens){
        ArrayList<Object> ans = new ArrayList<>();
        for(Token tok: tokens)
            ans.add(tok);
        return ans;
    }

     public static ExprNode Solve(ExprNode op1, ExprNode op2, Token oper){
        return new BinOperatorNode(oper.type, op1, op2);
     }

    public static ArrayList ConvertToRPN(ArrayList<Token> infixNotation){
        Stack<Token> stack = new Stack<>();
        ArrayList<Token> rpn = new ArrayList<>();
        Object priority, stackOperation;

        for (Token token: infixNotation) {
            stackOperation=-1;
            priority = (token.type).getPriority();

            if (!stack.empty())
                stackOperation = (stack.peek().type).getPriority();
            // if it's a number, then add to answer
            if (priority == null) { priority = -1; rpn.add(token); continue;}

            // else
            while ((int)priority < (int)stackOperation){
                rpn.add(stack.pop());
                if (stack.empty()) break;
                stackOperation = (stack.peek().type).getPriority();
            }
            stack.push(token);
        }

        while(!stack.empty())
            rpn.add(stack.pop());
        System.out.println();
        return rpn;
    }

    // parsing identifier and determine it's type (func call or variable)
    public static ExprNode ParseIdentifier(){
        // VAR -> WORD = NUM ; | WORD ;
        // CALLF -> WORD ( PARAMS ) ; | WORD ( ) ;

        String name = Currtoken.basetext;
        Token next = peekToken(0);
        if (next.type != TokenType.LPAR && nextToken().type == TokenType.EQ) { // then it's variable
            ExprNode value = ParseStmt();
            if(Currtoken.type == TokenType.SEMICOLON)
                return new VariableNode(name, value);
            else {ErrorMsg("; expected"); return null;}
        }
        // Otherwise it's may be call function
        nextToken();
        nextToken();
        ArrayList<ExprNode> args = parseArgs();

        // it should be stored into symbol table
        // SymbolTable.put(new CallFunc(name, args, null));
        return new CallFunc(name, args, null);
    }

    public static ArrayList<ExprNode> parseArgs(){
        ArrayList<ExprNode> args = new ArrayList<>();
        while (true) {
            ExprNode simpleArg = ParseExpression(); // let ParseExpression will not process "," in func args list
            if (Currtoken.type == TokenType.COMMA) nextToken();
            // below just condition when argument has invalid type
            if (simpleArg == null) { ErrorMsg("Wrong argument of function"); break; }
            else args.add(simpleArg);
            if (Currtoken.type == TokenType.RPAR) break;
        }
        return args;
    }

    public static ExprNode parseIFExpr(){
        // IFEXPR -> if "(" cond ")" stmt
        // IFEXPR -> if "(" stmt ")" BLOCK
        // IFEXPR -> if "(" stmt ")" BLOCK ELSE
        // ELSEEXPR -> else BLOCK
        // ELSEEXPR -> else IF "(" stmt ")" BLOCK
        nextToken();
        ExprNode cond = null, elseOp = null, body = null;
        if(Currtoken.type == TokenType.LPAR){
            nextToken();
            cond = parseCond(); // parsing condition. Here should be boolean operator with operands
            if(nextToken().type == TokenType.RPAR){
                // parsing block
                nextToken();
                body = ParseBlock();
                // parsing body, here should be BLOCK with "{" and "}"
                if(nextToken().type == TokenType.ELSE) { // parsing ELSE block
                    elseOp  = ParseBlock();
                }
            }
            else {ErrorMsg("} expected"); return null; }

        }
        return new IfExprNode(TokenType.IF, "if", cond, body, elseOp);
    }

    public static ExprNode ParseBlock(){
        ExprNode body = null;
        if(Currtoken.type == TokenType.LBRA) // curr == nexttoken()
            body = ParseStmt();
        else { ErrorMsg("{ expected"); return null; }
        if (Currtoken.type == TokenType.RBRA) return body;
        ErrorMsg("} expected");
        return body;
    }

    // parsing "while()"
    /*
    * WHILE -> while ( COND ) BLOCK
    * */
    public static ExprNode parseWHILEExpr(){
        ExprNode cond = null, body = null;
        nextToken();
        if(Currtoken.type == TokenType.LPAR){
            nextToken();
            cond = parseCond();
            if(nextToken().type == TokenType.RPAR){
                nextToken();
                body = ParseBlock(); // parsing body of WHILE
            }
            else {
                ErrorMsg("expected { here"); return null;
            }
        }
        return new WhileExprNode(TokenType.WHILE, cond, body);
    }

    // parsing conditions in while(), for(), if() (and in simple expressions)
    public static ExprNode parseCond(){
        // left part must contain any expression
        // middle part is boolean operation
        // right part is expression
        // COND -> EXPR boolop EXPR | VAR | bool func()
        // boolop ->  < | > | == | <= | >= | !=
        // case if cond is boolean variable
        // ...

        // case if cond is boolean expression
        nextToken();
        ExprNode left = ParseSimple();
        if (!Currtoken.type.isBooleanOper()){ErrorMsg("Expected boolean operator in condition"); return null;}
        TokenType boolOp = Currtoken.type;
        nextToken();
        ExprNode right = ParseSimple();
        ExprNode _cond = new BinOperatorNode(boolOp, left, right);
        /* Here I added logic operators OR and AND which has highest priority
         * among boolean operations
        */
        if(Currtoken.type == TokenType.OR || Currtoken.type==TokenType.AND)
            return new BinOperatorNode(Currtoken.type,_cond,parseCond());
        return _cond;
    }


    // return next token in the list of tokens
    public static Token nextToken(){
        if (_i >= LEN) return null;
        Currtoken = peekToken(0);
        _i++;
        return Currtoken;
    }

    // method doesn't change position of lexer's head (_i), it only return next token
    public static Token peekToken(int offset){
        if (_i + offset >= LEN) return new Token("eof", TokenType.EOF);
        return tokensProgram.get(_i + offset);
    }

    public static ExprNode ErrorMsg(String text){
        System.out.println(text);
        return null;
    }
}




// class of variables
class VariableNode extends ExprNode{
    private String varname;
    private ExprNode value; // not necessary if it's identification of variable

    public VariableNode(String name) {
        varname = name;
    }
    public VariableNode(String varname, ExprNode value){
        this.varname = varname;
        this.value = value;
    }
    

}

// class of called functions
class CallFunc extends ExprNode{
    private String calledname; // name of function
    private ArrayList<ExprNode> Args; // list of args
    private FunctionNode selffunction;
    public CallFunc(String called, ArrayList<ExprNode> args, FunctionNode selffunction) {
        calledname = called;
        Args = args;
        this.selffunction = selffunction;
    }
}

// node for binary operators
class BinOperatorNode extends ExprNode {

    private TokenType BinOp;
    private ExprNode operand1;
    private ExprNode operand2;

    public BinOperatorNode(TokenType binOp, ExprNode operand1, ExprNode operand2) {
        BinOp = binOp;
        this.operand1 = operand1;
        this.operand2 = operand2;
        System.out.println("\t BinOp: " + operand1.toString() + " " +BinOp+ " "+ operand2.toString());
    }

    // this method calculate binary operation of instance
    public int computation(){
        int result=0;
        try {
            switch (BinOp) {
                case PLUS:
                    return result = computeoperand(operand1) + computeoperand(operand2);
                case MINUS:
                    return result = computeoperand(operand1) - computeoperand(operand2);
                case MUL:
                    return result = computeoperand(operand1) * computeoperand(operand2);
                case DIV:
                    int op2 = computeoperand(operand2);
                    if (op2 == 0){
                        Parser.ErrorMsg("Dividing on zero"); return -1;}
                    return result = computeoperand(operand1) / op2;
            }
        }
        catch (Exception e){
        }
        return result;
    }

    // define type of operand and compute its value
    public int computeoperand(ExprNode operand) throws Exception{
        int op = 0;
        if (operand instanceof NumberNode) op = ((NumberNode) operand).getNumber();
        else if (operand instanceof BinOperatorNode) op = ((BinOperatorNode) operand).computation();
        else throw new Exception();
//        else if (operand instanceof FunctionNode) op = ((FunctionNode) operand).computation(); if node is function
        return op;
    }
}

//function declaration
class DeclareFunc extends ExprNode{
    private String name;
    private ArrayList<String> args; // here strings instead of ExprNode, because MyLang has only one type of function args (it's integers)
    public DeclareFunc(String name, ArrayList<String> args, Token lbra, Token rbra) {
//        super(lbra, rbra);
        this.name = name;
        this.args = args;
    }
}

// self function
class FunctionNode extends ExprNode{
    private DeclareFunc declaration;
    private ExprNode Body;

    public FunctionNode(DeclareFunc decl, ExprNode body){
        declaration = decl;
        Body = body;
    }
}


// node for IF expression with THEN and ELSE
class IfExprNode extends ExprNode {

    private ExprNode Condition;
    private ExprNode Then;
    private ExprNode Else;

    // constructor without Else
    public IfExprNode(TokenType type, String text, ExprNode condition, ExprNode then, Token lbra, Token rbra) {
//        super(lbra, rbra);
        Condition = condition;
        Then = then;
    }

    // ctor with Else
    public IfExprNode(TokenType type, String text, ExprNode condition, ExprNode then, ExprNode anElse) {
//        super(lbra, rbra);
        Condition = condition;
        Then = then;
        Else = anElse;
    }
}

class WhileExprNode extends ExprNode{
    private ExprNode cond;
    private ExprNode body;
    private TokenType type;

    public WhileExprNode(TokenType type, ExprNode cond, ExprNode body){
        this.cond = cond;
        this.type = type;
        this.body = body;
    }
}

class BlockNode extends ExprNode{
    private BlockNode superBlock;
    private ArrayList<BlockNode> subBlock;

    public BlockNode(BlockNode superBlock, ArrayList<BlockNode> subBlock){
        this.superBlock = superBlock;
        this.subBlock = new ArrayList<>(subBlock);
    }

    public BlockNode getSuperBlock(){
        return superBlock;
    }

    public void AddBlock(BlockNode block){
        subBlock.add(block);
    }
}

