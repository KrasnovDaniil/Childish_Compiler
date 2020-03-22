package me.dev.Frontend.LexicalAnalyzer;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {
    PLUS, MINUS, MUL, DIV,
    EQ,

    EQEQ, LESS, OVER,
    OR, AND,

    IF, ELSE, WHILE, FOR,

    VAR, NUM,

    LBRA, RBRA, LPAR, RPAR, COMMA, SEMICOLON,

    INT, DOUBLE, STRING,

    EOF;

    private static Map<TokenType,Integer> priority;


    public boolean Find(){
        try{
            return TokenType.valueOf(this.toString()) != null;
        } catch (Exception anyexception){
            return false;
        }
    }

    public boolean isBinary(){
            switch(this){
                case PLUS: return true;
                case MINUS: return true;
                case MUL: return true;
                case DIV: return true;
                default: return false;
            }
    }

    public boolean isBooleanOper(){
        switch(this){
            case LESS: return true;
            case OVER: return true;
            case EQEQ: return true;
            default: return false;
        }
    }

    public void SetPriorityTable(){
        priority = new HashMap<>();
        priority.put(MINUS, 1);
        priority.put(PLUS, 1);
        priority.put(MUL, 3);
        priority.put(DIV, 3);
    }

    public Integer getPriority(){
        if (priority == null) SetPriorityTable();
        return priority.get(this);
    }

}


