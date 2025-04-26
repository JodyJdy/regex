package com.jody.regex;


import java.util.List;

public class ASTPattern {
    final RegexToASTree regexToASTree;
    final Ast ast;
    final List<Ast> groups;

    public ASTPattern(RegexToASTree regexToASTree) {
        this.regexToASTree = regexToASTree;
        this.ast = regexToASTree.astTree();
        this.groups = regexToASTree.groupAsts;
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
