package com.example.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrAst extends Ast implements Cloneable{
    List<Ast> asts;
    OrAst(List<Ast> asts) {
        this.asts = asts;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        OrAst orAst = (OrAst)super.clone();
        List<Ast> cloneAsts = new ArrayList<>();
        for (Ast ast : asts) {
            cloneAsts.add((Ast)ast.clone());
        }
        orAst.asts = cloneAsts;
        return orAst;
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
