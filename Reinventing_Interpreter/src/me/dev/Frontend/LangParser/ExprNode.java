package me.dev.Frontend.LangParser;


public class ExprNode {
    // parent class which have fields: (TokenType)type, (Text)text
     // I think, first 2 fields are not necessary (I've deleted them )
    //    private Token Lbra, Rbra; // openning and closing brackets ( "{", "}" ) it is needed for properly work of STACK
        private ExprNode leftPart;
        private ExprNode rightPart;

        public ExprNode(){}

//    public ExprNode(ExprNode leftPart, ExprNode rightPart){
//        this.leftPart = leftPart;
//        this.rightPart = rightPart;
//    }

        public ExprNode(ExprNode leftPart, ExprNode rightPart){
            this.leftPart = leftPart;
            this.rightPart = rightPart;
//        Lbra = lbra;
//        Rbra = rbra;
        }
    }

    // class of numbers
    class NumberNode extends ExprNode {
        private int number;

        public NumberNode(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }
    }

