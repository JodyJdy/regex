package com.jody.regex;

import java.util.Map;

public class NumAst extends Ast implements Cloneable{
    final static String AT_LEAST_0 = "*";
    final static String AT_LEAST_1 = "+";
    final static String MOST_1 = "?";
    final static String UN_FIX = "num";
    final static String RANGE = "range";

    Ast ast;
    /**
     * 默认是贪婪模式 读取最长的匹配项目
     */
    boolean greedy = true;

    String type;
    /**
     * {num}
     */
    int num;
    /**
     * 可计数的ast的编号
     */
    int numAstNo;
    /**
     {start,end}
     */
    int start,end;
    NumAst(Ast ast, String type,int numAstNo) {
        this.ast = ast;
        this.type = type;
        this.numAstNo = numAstNo;
    }
    NumAst(Ast ast, int num,int numAstNo){
        this.ast = ast;
        this.num = num;
        type = UN_FIX;
        this.numAstNo = numAstNo;
    }
    NumAst(Ast ast, int start, int end,int numAstNo){
        this.ast =ast;
        this.start = start;
        this.end = end;
        type = RANGE;
        this.numAstNo = numAstNo;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        NumAst numAst = (NumAst)super.clone();
        numAst.ast = (Ast)ast.clone();
        return numAst;
    }

}
