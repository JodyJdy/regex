package com.jody.regex;

/**
 * 结尾标记
 */
class EndAst extends Ast{
    @Override
    protected Object clone() {
        return Util.END_AST;
    }
}
