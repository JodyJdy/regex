package com.jody.regex;

/**
 * 处理终结符相关信息
 */
class Terminal {
    /**
     * 普通字符
     */
    static final int SIMPLE = 0B0;
    /**
     * 数字
     */
    static final int NUMBER = 1;
    /**
     * 非数字
     */
    static final int NOT_NUMBER = 1<<1;
    /**
     * .
     */
    final static int DOT = 1<<2;
    /**
     * 匹配非字母，数字或者下划线
     */
    final static int W = 1<<3;

    /**
     * 匹配字母，数字，或者下划线
     */
    final static int w = 1<<4;
    /**
     * 匹配非空白符号
     *
     */
    final static int S = 1<<5;
    /**
     * 匹配空白符号
     */
    final static int s = 1<<6;

    /**
     * 分组引用， 引用的组的编号存储在低7位
     * groupNum catch
     */
    final static int GROUP_CAPTURE = 1<<8;
    /**
     * 分组引用时 低7位存储匹配的组 groupNum
     */
    final static int GROUP_NUM_MAST = 0b1111111;

    /**
     * \\p{}
     */
    final  static int P = 1 << 9;
    /**
     *匹配单词边界，不占字符位置，是字符之间的间隙
     * 左边占位的字符或右边占位的字符，至少有一个不是 \w
     */
    final static int b = 1 << 10;
    /**
     * 匹配"非单词"的边界，不占字符位置，是字符之间的间隙
     * 左右占位的字符，都必须是 \w。
     */
    final static int B = 1 << 11;

    /**
     * 匹配开始符号
     */
    final static int START_OF_LINE = 1 << 12;
    /**
     * 匹配结束符号
     */
    final static int END_OF_LINE = 1 << 13;

    /**
     * \\A
     */
    final static int INPUT_START = 1 << 14;
    /**
     * \z
     */
    final static int INPUT_END = 1<<15;

    /**
     * \Z
     */
    final static int INTPUT_END_WITH_TERMINATOR = 1 << 16;
    /**
     * \R
     */
    final static int UNICODE_LINE_BREAK = 1 << 17;

    /**
     * 组合类型
     */

    final static int COMPOSITE = 1 << 20;

    /**
     *递归类型
     */
    final static int RECURSIVE = 1 << 21;



    static boolean isSimple(int type){
        return type == SIMPLE;
    }

    static boolean isComposite(int type){
        return (type & COMPOSITE) != 0;
    }
    static boolean isGroupType(int type){
        return (type & GROUP_CAPTURE) != 0;
    }
    static int getReferenceGroupNum(int type){
        return type & GROUP_NUM_MAST;
    }

    static boolean isb(int type){
        return (type & b) != 0;
    }
    static boolean isB(int type){
        return (type &B) != 0;
    }
    public static boolean isStartOfLine(int type) {
        return type == START_OF_LINE;
    }
    public static boolean isEndOfLine(int type) {
        return type == END_OF_LINE;
    }

    public static boolean isStartOfInput(int type) {
        return type == INPUT_START;
    }
    public static boolean isEndOfInput(int type) {
        return type == INPUT_END;
    }

    public static boolean isEndOfInputWithTerminator(int type) {
        return type == INTPUT_END_WITH_TERMINATOR;
    }

    public static boolean isUniCodeLineBreak(int type) {
        return type == UNICODE_LINE_BREAK;
    }

}