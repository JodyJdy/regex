package com.jody.regex;


public class ASTPattern {
    final RegexToASTree regexToASTree;
    final Ast ast;
    final int modifiers;

    public ASTPattern(RegexToASTree regexToASTree,int modifiers) {
        this.regexToASTree = regexToASTree;
        this.ast = regexToASTree.astTree();
        this.modifiers = modifiers;
    }

    public static ASTPattern compile(String regex){
        return compile(regex, 0);
    }
    public static ASTPattern compile(String regex,int modifiers) {
        Util.checkEmpty(regex);
        String preprocessed = preprocess(regex);
        RegexToASTree regexToASTree = new RegexToASTree(preprocessed,modifiers != 0);
        return new ASTPattern(regexToASTree,modifiers);
    }

    /**
     * 16进制数字长度
     */
    private static final int HEX_LEN = 2;
    /**
     *预处理
     * 1. 预处理 \\xAA
     */
    private static String preprocess(String regex) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < regex.length()) {
            if (regex.charAt(i) == '\\' && i + 1 < regex.length() && regex.charAt(i + 1) == 'x') {
                //读取16进制数字
                if (i + 1+ HEX_LEN >= regex.length()) {
                    throw new RuntimeException("错误的16进制序列");
                }
                char ch = (char) Integer.valueOf(regex.substring(i + 2, i + 2 + HEX_LEN), 16).intValue();
                sb.append(ch);
                i += 2 + HEX_LEN;
            } else {
                sb.append(regex.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
    public ASTMatcher matcher(String str) {
        return new ASTMatcher(this, str);
    }
}
