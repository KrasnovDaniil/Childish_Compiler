package me.dev.Frontend.LexicalAnalyzer;


import me.dev.Frontend.MainFile;

import java.util.ArrayList;
import java.util.Hashtable;

public class Lexer {
    public int val;
    public char currsym;
    public int currentpos;
    public StringBuffer textProgram;
    public ArrayList<Token> tokens;
    private int LEN;

    public Hashtable<String, TokenType> operators;
    public Hashtable<String, TokenType> logicOpers;
    public Hashtable<String, TokenType> vartypes;
    public Hashtable<String, TokenType> ArithmOpers;


    public Lexer(String Program){
        textProgram = new StringBuffer(Program);
        tokens = new ArrayList<Token>();
        currentpos = 0;
        currsym = ShowSpecifiedToken(0);
        operators = new Hashtable<String, TokenType>();
        logicOpers = new Hashtable<String, TokenType>();
        vartypes = new Hashtable<String, TokenType>();
        ArithmOpers = new Hashtable<String, TokenType>();
        LEN = textProgram.length();
        addOperators();
    }

    public char ShowSpecifiedToken(int offset) {
        if (offset + currentpos >= textProgram.length()) return currsym = '\0';
        currsym = textProgram.charAt(currentpos + offset);
        return currsym;
    }

    public char next(){
        currentpos++;
        return ShowSpecifiedToken(0);
    }


    public ArrayList<Token> TokenizeCurrentWord(){

        while (currentpos < LEN){
//            currsym = ShowSpecifiedToken(0);
            if (Character.isAlphabetic(currsym)) tokenizeOperator();
            else if (operators.containsKey(currsym+"")) tokenizeCorrectingSymbols();
            else if (ArithmOpers.containsKey(currsym+"") || logicOpers.containsKey(currsym+"")) tokenizeBinOpers();
            else if (Character.isDigit(currsym)) tokenizeNumber();
//            else if (currsym == '\0') System.out.println("end of text file");
            else if (currsym == ' ') next();
            else ErrorMSG("invalid syntax");
        }
        System.out.println("END of work");
        tokens.add(new Token("EOFfe", TokenType.EOF));
        return tokens;
    }

    public void tokenizeNumber(){
        StringBuffer value = new StringBuffer("");
        char sym = currsym;
        while (mainCond() && Character.isDigit(sym)){
            value.append(sym);
            sym = next();
        }

        tokens.add(new Token(value+"", TokenType.NUM));
    }
    // tokenize operators of Language (while, for, if)
    public void tokenizeOperator(){
        StringBuffer buffer = new StringBuffer("");
        char sym = ShowSpecifiedToken(0);

        while (mainCond() && Character.isAlphabetic(sym)){
            buffer.append(sym);
            sym = next();
        }
        // is it func operator?
        if (operators.containsKey(buffer+"")) {
            tokens.add(new Token(buffer + "", operators.get(buffer+"")));
        }
        // or name of type
        else if (vartypes.containsKey(buffer+"")){
            tokens.add(new Token(buffer+"", vartypes.get(buffer+"")));
        }
        // or it's just variable
        else{
            tokens.add(new Token(buffer+"",TokenType.VAR));
            //ErrorMSG("invalid operator");
        }
    }

    public void tokenizeCorrectingSymbols(){
        char sign = ShowSpecifiedToken(0);
            Object justsym = operators.get(sign + "");
            if (justsym != null)
                tokens.add(new Token(sign + "", (TokenType) justsym));
            else ErrorMSG("Wrong sign");
        next();
    }

    public void tokenizeBinOpers(){
        int oldpos = currentpos;
        if (tokenizeLogicOper()) return;
        currentpos = oldpos;
        char sign = ShowSpecifiedToken(0);
        Object binOper = ArithmOpers.get(sign+"");

        if (binOper != null){ // if it's binary operator
            tokens.add(new Token(sign+"", (TokenType) binOper));
        } else { // or may be it's brackets or parentheses
            Object justsym = operators.get(sign + "");
            if (justsym != null)
                tokens.add(new Token(sign + "", (TokenType) justsym));
            else ErrorMSG("Wrong sign");
        }
        next();
    }

    public boolean tokenizeLogicOper() {
        StringBuffer buffer = new StringBuffer("");
        char sign = ShowSpecifiedToken(0);

        while (mainCond() && ArithmOpers.containsKey(sign+"") || logicOpers.containsKey(sign+"")) {
            buffer.append(sign);
            sign = next();
        }
        if (logicOpers.containsKey(buffer+"")){
            tokens.add(new Token(buffer+"", logicOpers.get(buffer+"")));
            return true;
        }
        else return false;
    }


    public void addOperators(){
        ArithmOpers.put("+",TokenType.PLUS);
        ArithmOpers.put("-",TokenType.MINUS);
        ArithmOpers.put("*",TokenType.MUL);
        ArithmOpers.put("/",TokenType.DIV);
        ArithmOpers.put("=",TokenType.EQ);

        operators.put("while",TokenType.WHILE);
        operators.put("if",TokenType.IF);
        operators.put("else",TokenType.ELSE);
        operators.put("for",TokenType.FOR);
        operators.put("{",TokenType.LBRA);
        operators.put("}",TokenType.RBRA);
        operators.put("(",TokenType.LPAR);
        operators.put(")",TokenType.RPAR);
        operators.put(",",TokenType.COMMA);
        operators.put(";",TokenType.SEMICOLON);

        logicOpers.put("<",TokenType.LESS);
        logicOpers.put(">",TokenType.OVER);
        logicOpers.put("==", TokenType.EQEQ);

        vartypes.put("int", TokenType.INT);
        vartypes.put("double", TokenType.DOUBLE);
        vartypes.put("string", TokenType.STRING);

    }



    public void ErrorMSG(String text){
        System.out.println(text);
        MainFile.Print(tokens);
        System.exit(0);
    }

    public void EOFexit(){
        MainFile.Print(tokens);
        System.exit(1);
    }

    public boolean mainCond(){
        return currentpos < LEN;
    }
}
