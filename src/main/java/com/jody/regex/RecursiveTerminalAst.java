package com.jody.regex;

/**
 * 表示递归类型的终结符
 */
public class RecursiveTerminalAst extends TerminalAst{
    RecursiveTerminalAst() {
        super(Terminal.RECURSIVE);
    }
}
