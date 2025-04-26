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

}
