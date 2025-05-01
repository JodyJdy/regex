package com.jody.regex;


import java.util.*;

/**
 * 正则表达式 转 抽象语法树
 */
class RegexToASTree {

    private final String regex;
    int i = 0;

    /**
     * 全部组的编号
     */
    int globalGroupCount = 0;
    /**
     * 捕获组的编号
     */
    int catchGroupCount = 0;
    /**
     * 模式修正编号
     */
    int modifierGroupCount = 0;

    /**
     * 可计数节点的编号
     */
    int numAstCount = 0;

    /**
     * 递归节点的编号
     */
    int recursiveCount = 0;

    /**
     * 所有可以捕获的组
     */
    List<Ast> catchGroups = new ArrayList<>();

    /**
     * 是否有模式修正符
     */
    boolean hasModifier = false;
    /**
     * 存储所有的组
     */
    List<Ast> allGroups = new ArrayList<>();


    RegexToASTree(String regex,boolean hasModifier) {
        this.regex = regex;
        this.hasModifier = hasModifier;
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

    /**
     * todo
     * 处理 \\x转移
     */
    private char getCh() {
        return regex.charAt(i);
    }

    /**
     * 将 抽象语法树，调整成 链表， 链表的顺序就是 搜索时的顺序，使用Ast的next节点调整
     */
    private static void tree2Linked(Ast ast) {
        if (ast.globalGroupNum != Util.NONE) {
            ast.nextLeaveGroupNum = ast.globalGroupNum;
            ast.nextLeaveGroupType = ast.groupType;
        }
        if (ast instanceof OrAst) {
            OrAst orAst = (OrAst) ast;
            for (Ast node : orAst.asts) {
                node.setNext(orAst.getNext());
                node.nextLeaveGroupNum = orAst.globalGroupNum;
                node.nextLeaveGroupType = orAst.nextLeaveGroupType;
                tree2Linked(node);
            }
        } else if (ast instanceof CatAst) {
            CatAst catAst = (CatAst) ast;
            List<Ast> asts = catAst.asts;
            int len = asts.size();
            Ast last = asts.get(len - 1);
            last.setNext(ast.getNext());
            last.nextLeaveGroupNum = ast.globalGroupNum;
            last.nextLeaveGroupType = ast.nextLeaveGroupType;
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
                numAst.ast.nextLeaveGroupNum = numAst.globalGroupNum;
                numAst.ast.nextLeaveGroupType = numAst.nextLeaveGroupType;
            } else {
                numAst.ast.setNext(numAst);
            }
            tree2Linked(numAst.ast);
        }
    }


    Ast astTree() {
        Ast ast = orTree();
        ast.setNext(Util.END_AST);
        tree2Linked(ast);
        //设置节点中可计数节点的编号范围
        Util.setNodeMinMaxNumAstNo(ast);
        sortGroups();
        return ast;
    }
    private  void sortGroups(){
        allGroups.sort(Comparator.comparingInt(x -> x.globalGroupNum));
        catchGroups.sort(Comparator.comparingInt(x -> x.catchGroupNum));
    }

    private Ast orTree() {
        //跳过空分支 |
        List<Ast> asts = new ArrayList<>();
        if (isEnd()) {
            return new OrAst(asts);
        }
        Ast left = catTree();
        asts.add(left);
        while (!isEnd() && getCh()=='|') {
            next();
            //遇到分组的结束了
            if(!isEnd() && getCh() == ')'){
                asts.add(new EmptyAst());
                break;
            }
            if (isEnd()) {
                break;
            }
            asts.add(catTree());
        }
        if (asts.size() == 1) {
            return left;
        }
        return new OrAst(asts);
    }

    private Ast catTree() {
        //  遇到了 ||的形式
        if(getCh() == '|'){
           return new EmptyAst();
        }
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
                    single = new NumAst(single, NumAst.AT_LEAST_1,numAstCount++);
                    while (!isEnd() && getCh() == '+') {
                        next();
                    }
                    break;
                case '*':
                    next();
                    while (!isEnd() && (getCh() == '*' || getCh() == '+')) {
                        next();
                    }
                    single = new NumAst(single, NumAst.AT_LEAST_0,numAstCount++);
                    break;
                case '?':
                    next();
                    Ast temp = single;
                    //特殊情况，对于递归调用 \\g<0>? 代表非贪婪模式， \\g<0> 代表贪婪模式，不再创建一个 NumAst
                    if(temp instanceof TerminalAst && ((TerminalAst) temp).isRecursiveType()){
                        //设置非贪婪模式
                        ((TerminalAst) temp).recursiveGreedy = false;
                    } else{
                        single = new NumAst(single, NumAst.MOST_1,numAstCount++);
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
                        single = new NumAst(single, start,numAstCount++);
                    } else {
                        single = new NumAst(single, start, end,numAstCount++);
                    }
                    break;
                default:
                    return single;
            }
            //非贪婪模式
            if (!isEnd() && getCh() == '?') {
                if (single instanceof NumAst) {
                    ((NumAst) single).greedy = false;
                }
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
            int level = 1;
            List<TerminalAst.CharRange> ranges = new ArrayList<>();
            while (level != 0 && !isEnd()) {
                if(getCh() =='['){
                    next();
                    level++;
                } else if(getCh() == ']'){
                    next();
                    level--;
                }else if (getCh() == '\\') {
                    next();
                    ch = getCh();
                    int tempType = getTerminalType(ch);
                    if (tempType != 0) {
                        type = type | tempType;
                    } else {
                        chs.add(ch);
                    }
                    next();
                }
                else if (getNext() == '-' && getNext(2) != ']') {
                    char start = getCh();
                    next(2);
                    char end = getCh();
                    next();
                    ranges.add(new TerminalAst.CharRange(start, end));
                } else {
                    chs.add(getCh());
                    next();
                }
            }
            return new TerminalAst(isNegative, chs, ranges, type);
        }
        // 遇到分组
        if (ch == '(') {
            return processGroup();
        }
        next();
        return new TerminalAst(ch, Terminal.SIMPLE);
    }
    private Ast processGroup(){
        int groupType = Group.CATCH_GROUP;
        next();
        String groupName = null;
        //处理模式修饰符 openFlag bit全为0，closeFlag全为1
        int openFlag = 0, closeFlag = Util.NONE;
        if (getCh() == '?') {
            next();
            // (?n)  开启命名捕获，默认开启
            if (getCh() == 'n') {
                next();
            }
            //遇到- 号
            boolean negative = false;
            while (!isEnd()) {
                char ch = getCh();
                if(ch == '-'){
                    negative = true;
                    next();
                    continue;
                } else if(ch == 'd'){
                    openFlag |= negative ? openFlag: Modifier.UNIX_LINES;
                    closeFlag &= negative ? Modifier.CLOSE_UNIX_LINES : closeFlag;
                }  else if(ch =='i'){
                    openFlag |= negative ? openFlag: Modifier.CASE_INSENSITIVE;
                    closeFlag &= negative ? Modifier.CLOSE_CASE_INSENSITIVE : closeFlag;
                } else if(ch == 'x'){
                    openFlag |= negative ? openFlag: Modifier.COMMENT;
                    closeFlag &= negative ? Modifier.CLOSE_COMMENT : closeFlag;
                } else if (ch == 'm') {
                    openFlag |= negative ? openFlag: Modifier.MULTILINE;
                    closeFlag &= negative ? Modifier.CLOSE_MULTILINE : closeFlag;
                } else if (ch == 's') {
                    openFlag |= negative ? openFlag: Modifier.DOTALL;
                    closeFlag &= negative ? Modifier.CLOSE_DOTALL : closeFlag;
                } else if(ch ==')'){
                    // -) 错误的结尾
                    if (negative) {
                        throw new RuntimeException("不支持的分组类型");
                    }
                    // 全局模式修正符
                    next();
                    ModifierAst modifierAst = new ModifierAst();
                    modifierAst.setModifierFlag(openFlag,closeFlag);
                    hasModifier = true;
                    return modifierAst;
                } else if(ch == ':'){
                    break;
                } else{
                    break;
                }
                negative = false;
                next();
            }
            switch (getCh()){
                case ':':
                    //说明有模式修正符
                    if(openFlag !=0 || closeFlag !=Util.NONE){
                        hasModifier = true;
                        groupType = Group.NOT_CATCH_GROUP_WITH_MODIFIER;
                    } else{
                        groupType = Group.NOT_CATCH_GROUP;
                    }
                    break;
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
                default:throw new RuntimeException("不支持的分组类型");
            }
            next();
        }
        int catchGroupNum = -1;
        if(groupType == Group.CATCH_GROUP){
           catchGroupNum = catchGroupCount++;
        }
        int groupNum = globalGroupCount++;
        Ast asTree = orTree();
        next();
        //设置组信息
        asTree.groupName = groupName;
        asTree.catchGroupNum = catchGroupNum;
        asTree.groupType = groupType;
        asTree.globalGroupNum = groupNum;
        asTree.openFlag = openFlag;
        asTree.closeFlag = closeFlag;
        if (groupType == Group.CATCH_GROUP) {
            catchGroups.add(asTree);
        }
        allGroups.add(asTree);
        return asTree;
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
