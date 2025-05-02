package com.jody.regex;



public class ASTPattern {
    final RegexToASTree regexToASTree;
    final Ast ast;
    final int modifiers;

    public ASTPattern(RegexToASTree regexToASTree, int modifiers) {
        this.regexToASTree = regexToASTree;
        this.ast = regexToASTree.astTree();
        this.modifiers = modifiers;
    }

    public static ASTPattern compile(String regex) {
        return compile(regex, 0);
    }

    public static ASTPattern compile(String regex, int modifiers) {
        Util.checkEmpty(regex);
        RegexToASTree regexToASTree = new RegexToASTree(regex, modifiers);
        return new ASTPattern(regexToASTree, modifiers);
    }



    public ASTMatcher matcher(String str) {
        return new ASTMatcher(this, str);
    }
}
