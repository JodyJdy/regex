package com.jody.regex;

/**
 * 模式修正符号
 */
public class Modifier {
    /**
     * (?d)  \n 视为行结束符， 不考虑 \r
     */
    public static final int UNIX_LINES = 0B1;
    /**
     * (?-d)
     */
    static final int CLOSE_UNIX_LINES = ~UNIX_LINES;

    /**
     * 大小写不敏感
     * (?i)
     */
    public static int CASE_INSENSITIVE = 0B10;
    /**
     * 大小写敏感
     * (?-i)
     */
    static int CLOSE_CASE_INSENSITIVE = ~CASE_INSENSITIVE;
    /**
     * 注释
     * (?x)
     */
    public static int COMMENT = 0B100;
    /**
     * 关闭注释
     * (?-x)
     */
    static int CLOSE_COMMENT = ~COMMENT;
    /**
     * 多行模式
     * (?m)
     */
    public static int MULTILINE = 0B1000;
    /**
     * 关闭多行
     * (?-m)
     */
    static int CLOSE_MULTILINE = ~MULTILINE;
    /**
     * . 匹配任意字符模式
     * (?s)
     */
    public static int DOTALL = 0B10000;
    /**
     * 关闭 .匹配任意字符模式
     * (?-s)
     */
    static int CLOSE_DOTALL = ~DOTALL;


    static boolean openUnixLine(int modifier) {
        return (modifier & UNIX_LINES) != 0;
    }

    static boolean openCaseInsensitive(int modifier) {
        return (modifier & CASE_INSENSITIVE) != 0;
    }

    static boolean openComment(int modifier) {
        return (modifier & COMMENT) != 0;
    }

    static boolean openMultiline(int modifier) {
        return (modifier & MULTILINE) != 0;
    }

    static boolean openDotAll(int modifier) {
        return (modifier & DOTALL) != 0;
    }
}
