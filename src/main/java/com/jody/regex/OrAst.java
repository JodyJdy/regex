package com.jody.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class OrAst extends Ast implements Cloneable{
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

}
