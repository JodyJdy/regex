package com.jody.regex;


import java.util.List;

public class ASTPattern {
    final RegexToASTree regexToASTree;
    final Ast ast;
    final List<Ast> catchGroups;
    final int modifiers;

    public ASTPattern(RegexToASTree regexToASTree,int modifiers) {
        this.regexToASTree = regexToASTree;
        this.ast = regexToASTree.astTree();
        this.catchGroups = regexToASTree.catchGroups;
        this.modifiers = modifiers;
    }

    public static ASTPattern compile(String regex){
        return compile(regex, 0);
    }
    public static ASTPattern compile(String regex,int modifiers) {
        Util.checkEmpty(regex);
        RegexToASTree regexToASTree = new RegexToASTree(regex);
        return new ASTPattern(regexToASTree,modifiers);
    }

    public ASTMatcher matcher(String str) {
        return new ASTMatcher(this, str);
    }
}
