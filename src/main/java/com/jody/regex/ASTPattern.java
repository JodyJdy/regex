package com.jody.regex;


import java.util.List;

public class ASTPattern {
    final RegexToASTree regexToASTree;
    final Ast ast;
    final List<Ast> catchGroups;

    public ASTPattern(RegexToASTree regexToASTree) {
        this.regexToASTree = regexToASTree;
        this.ast = regexToASTree.astTree();
        this.catchGroups = regexToASTree.groupAsts;
    }

    public static ASTPattern compile(String regex) {
        Util.checkEmpty(regex);
        RegexToASTree regexToASTree = new RegexToASTree(regex);
        return new ASTPattern(regexToASTree);
    }

    public ASTMatcher matcher(String str) {
        return new ASTMatcher(this, str);
    }
}
