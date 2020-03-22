package me.dev.Frontend;

import me.dev.Frontend.LangParser.ExprNode;
import me.dev.Frontend.LexicalAnalyzer.Lexer;
import me.dev.Frontend.LexicalAnalyzer.Token;
import me.dev.Frontend.LangParser.Parser;

import java.util.ArrayList;

public class MainFile {

    public static void main(String[] args) throws Exception {
        String textofprogram = "if (2+3*5/2==4) { " +
                "                   while (a < 3) a = a + 1;" +
                "               } ";
        String text2 = "a = 4;" +
                        "b = 5;";
//        textofprogram = text2;
        Lexer lexer = new Lexer(textofprogram);
        Print(lexer.TokenizeCurrentWord());
        Parser parser = new Parser(lexer.tokens);
        ExprNode root = parser.MainProcess();
        System.out.println();
    }

    static public void Print(ArrayList<Token> toks){
        for (Token i: toks) {
            System.out.println(i.basetext + " " + i.type);
        }
    }
}