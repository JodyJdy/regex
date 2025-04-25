package com.jody.regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 正则表达式 转 抽象语法树
 */
class RegexToASTree {

    private final String regex;
    int i = 0;
    /**
     * 组的编号
     */
    private int groupCount = 0;

    /**
     * 收集所有的组
     */
    List<Ast> groupAsts = new ArrayList<>();

    /**
     * 是否有递归非贪婪匹配， \\g<0>?,需要特殊处理结果集
     */
    boolean hasRecursiveNoGreedy = false;

    RegexToASTree(String regex) {
        this.regex = regex;
    }

    public boolean isEnd() {
        return i >= regex.length();
    }

    private char getNext(int n) {
        return regex.charAt(i + n);
    }

    private char getNext() {
        return regex.charAt(i + 1);
    }

    private boolean checkAndNext(char ch) {
        if (ch == getCh()) {
            i++;
            return true;
        }
        return false;
    }

    private void next(int n) {
        i += n;
    }

    private void next() {
        i++;
    }

    private char getCh() {
        return regex.charAt(i);
    }

    /**
     * 将 抽象语法树，调整成 链表， 链表的顺序就是 搜索时的顺序，使用Ast的next节点调整
     */
    private static void tree2Linked(Ast ast) {
        if (ast.groupNum != 0) {
            ast.nextLeaveGroup = true;
            ast.leaveGroupNum = ast.groupNum;
        }
        if (ast instanceof TerminalAst) {
            return;
        }
        if (ast instanceof OrAst) {
            OrAst orAst = (OrAst) ast;
            for (Ast node : orAst.asts) {
                node.setNext(orAst.getNext());
                node.nextLeaveGroup = orAst.nextLeaveGroup;
                node.leaveGroupNum = orAst.leaveGroupNum;
                tree2Linked(node);
            }
        } else if (ast instanceof CatAst) {
            CatAst catAst = (CatAst) ast;
            List<Ast> asts = catAst.ast;
            int len = asts.size();
            Ast last = asts.get(len - 1);
            last.setNext(ast.getNext());
            last.nextLeaveGroup = ast.nextLeaveGroup;
            last.leaveGroupNum = ast.leaveGroupNum;
            for(int x = 0; x < len - 1;x++){
                asts.get(x).setNext(asts.get(x+1));
            }
            for(Ast a : asts){
                tree2Linked(a);
            }

        } else if (ast instanceof NumAst) {
            NumAst numAst = (NumAst) ast;
            //对? 进行优化，直接进行链接
            if (numAst.type.equals(NumAst.MOST_1)) {
                numAst.ast.setNext(numAst.getNext());
                numAst.ast.nextLeaveGroup = numAst.nextLeaveGroup;
                numAst.ast.leaveGroupNum = numAst.leaveGroupNum;
            } else {
                numAst.ast.setNext(numAst);
            }
            tree2Linked(numAst.ast);
        }
    }

    private void reset(){
        i = 0;
        groupCount = 0;
        groupAsts = new ArrayList<>();
    }

    Ast astTree() {
        reset();
        Ast ast = orTree();
        ast.setNext(Util.END_AST);
        tree2Linked(ast);
        //将自身当作编号为0的组
        groupAsts.add(0, ast);
        return ast;
    }

    private Ast orTree() {
        Ast left = catTree();
        List<Ast> asts = new ArrayList<>();
        asts.add(left);
        while (!isEnd() && getCh() == '|') {
            next();
            asts.add(catTree());
        }
        if (asts.size() == 1) {
            return left;
        }
        return new OrAst(asts);
    }

    private Ast catTree() {
        Ast mul = multiTree();
        List<Ast> asts = new ArrayList<>();
        asts.add(mul);
        while (!isEnd() && getCh() != '|' && getCh() != ')') {
            asts.add(multiTree());
        }
        if(asts.size() == 1){
            return mul;
        }
        //合并部分终结符，减少节点数量
        asts = Util.mergeTerminal(asts);
        return new CatAst(asts);
    }

    private Ast multiTree() {
        Ast single = single();
        for (; ; ) {
            if (isEnd()) {
                return single;
            }
            char ch = getCh();
            switch (ch) {
                case '+':
                    next();
                    single = new NumAst(single, NumAst.AT_LEAST_1);
                    while (!isEnd() && getCh() == '+') {
                        next();
                    }
                    break;
                case '*':
                    next();
                    while (!isEnd() && (getCh() == '*' || getCh() == '+')) {
                        next();
                    }
                    single = new NumAst(single, NumAst.AT_LEAST_0);
                    break;
                case '?':
                    next();
                    Ast temp = single;
                    single = new NumAst(single, NumAst.MOST_1);
                    //特殊情况，对于递归调用 \\g<0>? 代表非贪婪模式， \\g<0> 代表贪婪模式
                    if (temp instanceof TerminalAst && ((TerminalAst) temp).isRecursiveType()) {
                        ((NumAst) single).greedy = false;
                        hasRecursiveNoGreedy = true;
                    }
                    break;
                case '{':
                    next();
                    int start = getNum();
                    int end = 0;
                    if (getCh() == ',') {
                        next();
                        end = getNum();
                        //{num,}
                        if (end == 0) {
                            end = Integer.MAX_VALUE;
                        }
                    }
                    checkAndNext('}');
                    if (end == 0) {
                        single = new NumAst(single, start);
                    } else {
                        single = new NumAst(single, start, end);
                    }
                    break;
                default:
                    return single;
            }
            //非贪婪模式
            if (!isEnd() && getCh() == '?') {
                ((NumAst) single).greedy = false;
                next();
            }
        }
    }

    private int getTerminalType(char ch) {
        switch (ch) {
            case 'd':
                return Terminal.NUMBER;
            case 'W':
                return Terminal.W;
            case 'S':
                return Terminal.S;
            case 's':
                return Terminal.s;
            case 'D':
                return Terminal.NOT_NUMBER;
            case 'w':
                return Terminal.w;
            case 'b':
                return Terminal.b;
            case 'B':
                return Terminal.B;
            //表达式引用形式是\g<0> \g<1>  \g<0>代表递归匹配
            case 'g':
                next(2);
                return Terminal.EXPRESSION | getNum();
            default:
                return Terminal.SIMPLE;
        }
    }

    private Ast single() {
        TerminalAst terminator;
        char ch = getCh();
        if (ch == '\\') {
            next();
            ch = getCh();
            //引用组
            if (Terminal.isNumber(ch)) {
                int groupCount = getNum();
                terminator = new TerminalAst(Terminal.GROUP_CAPTURE | groupCount);
            } else {
                int type = getTerminalType(ch);
                if (type == Terminal.SIMPLE) {
                    terminator = new TerminalAst(ch, Terminal.SIMPLE);
                } else {
                    terminator = new TerminalAst(type);
                }
                next();
            }
            return terminator;
        }
        if (ch == '^') {
            next();
            return new TerminalAst(Terminal.START);
        }
        if (ch == '$') {
            next();
            return new TerminalAst(Terminal.END);
        }
        if (ch == '.') {
            next();
            return new TerminalAst(Terminal.DOT);
        }
        if (ch == '[') {
            next();
            boolean isNegative = checkAndNext('^');
            //[]里面也可以放 \d,\w这种,
            int type = Terminal.COMPOSITE;
            Set<Character> chs = new HashSet<>();
            List<TerminalAst.CharRange> ranges = new ArrayList<>();
            while (getCh() != ']') {
                if (getNext() == '-' && getNext(2) != ']') {
                    char start = getCh();
                    next(2);
                    char end = getCh();
                    next();
                    ranges.add(new TerminalAst.CharRange(start, end));
                } else if (getCh() == '\\') {
                    next();
                    ch = getCh();
                    int tempType = getTerminalType(ch);
                    if (tempType != 0) {
                        type = type | tempType;
                    } else {
                        chs.add(ch);
                    }
                    next();
                } else {
                    chs.add(getCh());
                    next();
                }
            }
            next();
            return new TerminalAst(isNegative, chs, ranges, type);
        }
        // 遇到分组
        if (ch == '(') {
            int groupNum = ++groupCount;
            int groupType = Group.CATCH_GROUP;
            next();
            String groupName = null;
            if (getCh() == '?') {
                next();
                switch (getCh()){
                    case ':':groupType = Group.NOT_CATCH_GROUP;break;
                    case '=':groupType = Group.FORWARD_POSTIVE_SEARCH;break;
                    case '!':groupType = Group.FORWARD_NEGATIVE_SEARCH;break;
                    case '<':next();
                        if (getCh() == '=') {
                            groupType = Group.BACKWARD_POSTIVE_SEARCH;
                            break;
                        } else if (getCh() == '!') {
                            groupType = Group.BACKWARD_NEGATIVE_SEARCH;
                            break;
                        } else{
                            //分组命名
                            groupName = readGroupName();
                            break;
                        }
                    default:throw new RuntimeException("error groupType");
                }
                next();
            }
            Ast asTree = orTree();
            asTree.groupName = groupName;
            next();
            asTree.groupNum = groupNum;
            asTree.groupType = groupType;
            groupAsts.add(asTree);
            return asTree;
        }
        next();
        return new TerminalAst(ch, Terminal.SIMPLE);
    }
    private String readGroupName() {
        StringBuilder sb = new StringBuilder();
        if (!Terminal.isUpper(getCh()) && !Terminal.isLower(getCh())) {
            throw new RuntimeException("分组命名以大小写字母开头");
        }
        do {
            sb.append((getCh()));
            next();
        } while (Terminal.isUpper(getCh()) || Terminal.isLower(getCh()) || Terminal.isNumber(getCh()));
        if (getCh() != '>')
            throw new RuntimeException("分组命名以>结尾");
        return sb.toString();
    }
    private int getNum() {
        int num = 0;
        while (!isEnd() && Terminal.isNumber(getCh())) {
            num *= 10;
            num += getCh() - '0';
            next();
        }
        return num;
    }


}
