package com.jody.regex;


public class ASTPattern {
    final RegexToASTree regexToASTree;

    public ASTPattern(RegexToASTree regexToASTree) {
        this.regexToASTree = regexToASTree;
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
