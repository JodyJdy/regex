package com.jody.regex;


import java.util.HashMap;
import java.util.Map;

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
        String preprocessed = preprocess(regex);
        RegexToASTree regexToASTree = new RegexToASTree(preprocessed, modifiers != 0);
        return new ASTPattern(regexToASTree, modifiers);
    }

    /**
     * 16进制数字长度
     */
    private static final int HEX_LEN = 2;
    /**
     * unicode 长度
     */
    private static final int UNI_CODE_LEN = 4;

    /**
     * \ 开头的特殊字符
     */
    private static Map<Character, Character> specialCharsMap = new HashMap<>();

    static {
        specialCharsMap.put('n', '\n');
        specialCharsMap.put('r', '\r');
        specialCharsMap.put('f', '\f');
        specialCharsMap.put('t', '\t');
    }

    /**
     * 预处理
     * 1. 预处理 \\xAA
     * 2.  \\u0000
     * 3. \n \r \f
     */
    private static String preprocess(String regex) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < regex.length()) {
            if (regex.charAt(i) == '\\' && i + 1 < regex.length()) {
                Character nextCh = regex.charAt(i + 1);
                if (nextCh == 'x') {
                    //读取16进制数字
                    if (i + 1 + HEX_LEN >= regex.length()) {
                        throw new RuntimeException("错误的16进制序列");
                    }
                    char ch = (char) Integer.valueOf(regex.substring(i + 2, i + 2 + HEX_LEN), 16).intValue();
                    sb.append(ch);
                    i += 2 + HEX_LEN;
                } else if (nextCh == 'u') {
                    //读取16进制数字
                    if (i + 1 + UNI_CODE_LEN >= regex.length()) {
                        throw new RuntimeException("错误的Unicode序列");
                    }
                    char ch = (char) Integer.valueOf(regex.substring(i + 2, i + 2 + UNI_CODE_LEN), 16).intValue();
                    sb.append(ch);
                    i += 2 + UNI_CODE_LEN;
                } else if (specialCharsMap.containsKey(nextCh) && i > 0 && regex.charAt(i - 1) != '\\') {
                    sb.append(specialCharsMap.get(nextCh));
                    i += 2;
                } else {
                    sb.append(regex.charAt(i));
                    i++;
                }
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
