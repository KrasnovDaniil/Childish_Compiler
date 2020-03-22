package me.dev.Frontend;

import java.lang.reflect.*;

public class Hello {

    static int val = 50;
    static public void DivMethod(int denom) {
        int res;
        try {
            int num1;
            res = val / denom;
            System.out.println("res = " + res);
            res = 1;
        }
        catch (Exception e) {
            System.out.println("Throws Exception " + e.getMessage());
            res = 2;
        }
        finally{
            res = 100;
        }
        System.out.println("res = " + res);
    }

    static public void DivThrows (int denom) throws RuntimeException{
        int res = val/denom;
        System.out.println("throws res = " + res    );
    }


    static public void _main() throws Exception{
//        myClass my = new myClass();

        Class ownclass = myClass.class;
        ownclass.getConstructors();
        myClass newClass = (myClass) ownclass.newInstance();

        // вызвать метод PrintMe() экземпляра ownclass класса Class
        Method mth = ownclass.getMethod("PrintMe");
        System.out.println("invoking method PrintMe()");
        mth.invoke(newClass); // вызвать метод mth объекта ownclass

        // получить данные поля класса ownclass
        Field field01 = ownclass.getDeclaredField("name");
        field01.setAccessible(true); // установить модификатор доступа, в данном случае private-поле сделать public-полем
        field01.set(newClass, "Daniil");
        System.out.println("field of ownclass = " + field01);

        mth.invoke(newClass);
        pp(newClass);
    }

    static public void pp(myClass c){
        System.out.println("name = " + c.name + "\nage = " + c.age);
    }

}


class myClass{
    public int age = 21;
    public String name = "Grisha";

    public myClass(){
    }

    public void PrintMe(){
        System.out.println("Name: " + name + "\nAge :" + age);
    }
}