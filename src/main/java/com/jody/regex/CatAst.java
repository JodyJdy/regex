package com.jody.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class CatAst extends Ast implements Cloneable {
    List<Ast> asts;
    CatAst(List<Ast> asts) {
        this.asts = asts;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CatAst catAst = (CatAst)super.clone();
        List<Ast> asts = new ArrayList<>();
        for(Ast a : this.asts){
            asts.add((Ast)a.clone());
        }
        catAst.asts = asts;
        return catAst;
    }

    @Override
    void clearNumAstStatus() {
        for(Ast a : asts){
            a.clearNumAstStatus();
        }
    }
    @Override
    void storeStatus(Map<Ast, MatcherStatus.AstStatus> map) {
        super.storeStatus(map);
        for(Ast a : asts){
            a.storeStatus(map);
        }
    }

    @Override
    void loadStatus(Map<Ast, MatcherStatus.AstStatus> map) {
        super.loadStatus(map);
        for(Ast a : asts){
            a.loadStatus(map);
        }
    }
}
