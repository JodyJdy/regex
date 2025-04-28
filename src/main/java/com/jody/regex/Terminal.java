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
     * 组表达式引用
     */
    final  static int EXPRESSION = 1 << 9;
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
    final static int START = 1 << 12;
    /**
     * 匹配结束符号
     */
    final static int END = 1 << 13;

    /**
     * 组合类型
     */

    final static int COMPOSITE = 1 << 20;


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
    static boolean isExpression(int type){
        return (type & EXPRESSION)!= 0;
    }

    static boolean isb(int type){
        return (type & b) != 0;
    }
    static boolean isB(int type){
        return (type &B) != 0;
    }

    private static boolean isNumberType(int type){
        return  (type & NUMBER) != 0;
    }
    static boolean isNumber(char ch){
        return ch>='0' && ch<='9';
    }

    private static boolean isNotNumberType(int type){
        return  (type & NOT_NUMBER) != 0;
    }
    private static boolean isNotNumber(char ch){
        return !isNumberType(ch);
    }
    private static boolean isDotType(int type){
        return  (type & DOT) != 0;
    }
    private static boolean isWType(int type){
        return  (type & W) != 0;
    }
    static boolean isW(char ch){
        return !isw(ch);
    }
    private static boolean iswType(int type){
        return  (type & w) != 0;
    }

    static boolean isw(char c) {
        return isUnder(c) || isUpper(c) || isLower(c) || isNumber(c);
    }

    static boolean isUnder(char c) {
        return c == '_';
    }

    static boolean isLower(char c) {
        return c >= 'a' && c <= 'z';
    }

    static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z';
    }
    private static boolean isS(char c){
        return !iss(c);
    }
    private static boolean isSType(int type){
        return  (type & S) != 0;
    }

    private static boolean iss(char c){
        return c == '\n' || c == '\f' || c == '\r' || c == '\t' || c ==' ';
    }
    private static boolean issType(int type){
        return  (type & s) != 0;
    }

    public static boolean isStart(int type) {
        return type == START;
    }
    public static boolean isEnd(int type) {
        return type == END;
    }

    static boolean match(char ch,int type,int modifier){
        boolean result = false;
        if(isNumberType(type)){
            result = isNumber(ch);
        }
        if(isNotNumberType(type)){
            result = result || isNotNumber(ch);
        }
        if(isDotType(type)){
            // 默认 . 是不匹配换行的
            if (Modifier.openDotAll(modifier)) {
                result = true;
            } else {
                result = ch != '\r' && ch != '\n';
            }
        }
        if(isWType(type)){
            result = result || isW(ch);
        }
        if(iswType(type)){
            result = result || isw(ch);
        }
        if(isSType(type)){
            result = result || isS(ch);
        }
        if(issType(type)){
            result = result || iss(ch);
        }
        return result;
    }
}
