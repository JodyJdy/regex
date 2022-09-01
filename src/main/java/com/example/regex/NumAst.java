package com.example.regex;

import java.util.Map;

public class NumAst extends Ast implements Cloneable{
    final static String AT_LEAST_0 = "*";
    final static String AT_LEAST_1 = "+";
    final static String MOST_1 = "?";
    final static String UN_FIX = "num";
    final static String RANGE = "range";
    Ast ast;
    /**
     * 语法分析用，记录节点循环的次数
     */
    int circleNum;
    /**
     * 默认是贪婪模式 读取最长的匹配项目
     */
    boolean greedy = true;
    /**
     *记录，已经处理过的最大状态
     */
    int maxI = -1;

    String type;
    /**
     * {num}
      */
    int num;
    /**
     {start,end}
     */
    int start,end;
    NumAst(Ast ast, String type) {
        this.ast = ast;
        this.type = type;
    }
    NumAst(Ast ast, int num){
        this.ast = ast;
        this.num = num;
        type = UN_FIX;
    }
    NumAst(Ast ast, int start, int end){
        this.ast =ast;
        this.start = start;
        this.end = end;
        type = RANGE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        NumAst numAst = (NumAst)super.clone();
        numAst.ast = (Ast)ast.clone();
        return numAst;
    }

    @Override
    void clearNumAstStatus() {
        circleNum = 0;
        maxI = -1;
    }
    @Override
    void storeStatus(Map<Ast, MatcherStatus.AstStatus> map) {
        map.put(this,new MatcherStatus.AstStatus(this));
        ast.storeStatus(map);
    }

    @Override
    void loadStatus(Map<Ast, MatcherStatus.AstStatus> map) {
        super.loadStatus(map);
        MatcherStatus.AstStatus status = map.get(this);
        this.circleNum = status.circleNum;
        this.maxI = status.maxI;
    }
}
