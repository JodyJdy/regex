package com.example.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CatAst extends Ast implements Cloneable {
    List<Ast> ast;
    CatAst(List<Ast> ast) {
        this.ast = ast;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CatAst catAst = (CatAst)super.clone();
        List<Ast> asts = new ArrayList<>();
        for(Ast a : ast){
            asts.add((Ast)a.clone());
        }
        catAst.ast = asts;
        return catAst;
    }

    @Override
    void clearNumAstStatus() {
       for(Ast a : ast){
           a.clearNumAstStatus();
       }
    }
    @Override
    void storeStatus(Map<Ast, MatcherStatus.AstStatus> map) {
        super.storeStatus(map);
        for(Ast a : ast){
            a.storeStatus(map);
        }
    }

    @Override
    void loadStatus(Map<Ast, MatcherStatus.AstStatus> map) {
        super.loadStatus(map);
        for(Ast a : ast){
            a.loadStatus(map);
        }
    }
}
