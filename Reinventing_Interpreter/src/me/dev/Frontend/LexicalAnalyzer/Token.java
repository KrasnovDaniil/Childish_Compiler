package me.dev.Frontend.LexicalAnalyzer;

public class Token {
    public String basetext;
    public int val = 0;
    public TokenType type;

    public Token(String text, TokenType type){
        this.type = type;
        try{
            val = Integer.parseInt(text);
        } catch (Exception e){}
        basetext = text;
    }





}
