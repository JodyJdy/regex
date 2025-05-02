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
     * 可计数节点的编号
     */
    int numAstCount = 0;


    /**
     * 所有可以捕获的组
     */
    List<Ast> catchGroups = new ArrayList<>();

    /**
     * 16进制数字长度
     */
    private static final int HEX_LEN = 2;
    /**
     * unicode 长度
     */
    private static final int UNI_CODE_LEN = 4;


    int modifier;
    /**
     * 存储所有的组
     */
    List<Ast> allGroups = new ArrayList<>();


    RegexToASTree(String regex,int modifier) {
        this.regex = regex;
        this.modifier = modifier;
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
        int curModifier = modifier;
        Ast ast = orTree();
        ast.setNext(Util.END_AST);
        tree2Linked(ast);
        //设置节点中可计数节点的编号范围
        Util.setNodeMinMaxNumAstNo(ast);
        sortGroups();
        modifier = curModifier;
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
                    single = new NumAst(single, NumAst.MOST_1, numAstCount++);
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

    /**
     *读取 \ 之后的特殊的 TerminalAst
     *
     *读取后下标会 移动
     */
    private TerminatorMsg readTerminator(){
        char ch = getCh();
        int type = Terminal.SIMPLE;
        CharPredicates.CharPredicate predicate = null;
        Character character = null;
        // 简单情形
        if (ch != '\\') {
            //移动下标
            next();
            return new TerminatorMsg(type, ch);
        }
        //跳过\符号
        next();
        ch = getCh();
        //以转移符号 \\开头的内容
        //引用组
        if (ASCII.isDigit(ch)) {
            int groupCount = getNum();
            type = Terminal.GROUP_CAPTURE | groupCount;
        } else {
            switch (ch) {
                case 'c': next(); character = (char) (getCh() ^ 64);next();break;
                case 'n': character = '\n';next();break;
                case 'r': character = '\r';next();break;
                case 'f': character = '\f';next();break;
                case 't': character = '\t';next();break;
                case 'x':
                    if (i + 1 + HEX_LEN >= regex.length()) {
                        throw new RuntimeException("错误的16进制序列");
                    }
                    character = (char) Integer.valueOf(regex.substring(i + 1 , i + 1 + HEX_LEN), 16).intValue();
                    next(1 + HEX_LEN);
                    break;
                case 'u':
                    if (i + 1 + UNI_CODE_LEN >= regex.length()) {
                        throw new RuntimeException("错误的Unicode序列");
                    }
                    character = (char) Integer.valueOf(regex.substring(i + 1 , i + 1 + UNI_CODE_LEN), 16).intValue();
                    next(1 + UNI_CODE_LEN);
                    break;
                case 'd':
                    predicate = Modifier.openUnicodeCharacterClass(modifier)?
                            CharPredicates.DIGIT() : CharPredicates.ASCII_DIGIT();
                    type =  Terminal.NUMBER;next();break;
                case 'W':
                    predicate = Modifier.openUnicodeCharacterClass(modifier) ?
                           CharPredicates.WORD() :CharPredicates.ASCII_WORD();
                    predicate = predicate.negate();
                    type =  Terminal.W;next();break;
                case 'S':
                    predicate = Modifier.openUnicodeCharacterClass(modifier) ?
                            CharPredicates.WHITE_SPACE() : CharPredicates.ASCII_SPACE();
                    predicate = predicate.negate();
                    type =  Terminal.S;next();break;
                case 's':
                    predicate = Modifier.openUnicodeCharacterClass(modifier) ?
                            CharPredicates.WHITE_SPACE() : CharPredicates.ASCII_SPACE();
                    type =  Terminal.s;next();break;
                case 'D':
                    predicate = Modifier.openUnicodeCharacterClass(modifier)?
                            CharPredicates.DIGIT() : CharPredicates.ASCII_DIGIT();
                    predicate = predicate.negate();
                    type =  Terminal.NOT_NUMBER;next();break;
                case 'w':
                    predicate = Modifier.openUnicodeCharacterClass(modifier) ?
                            CharPredicates.WORD() :CharPredicates.ASCII_WORD();
                    type =  Terminal.w;next();break;
                case 'b':
                    type =  Terminal.b;next();break;
                case 'B':
                    type =  Terminal.B;next();break;
                case 'h':predicate = CharPredicates.HorizWS().negate();break;
                case 'H':predicate = CharPredicates.HorizWS();break;
                case 'v':predicate = CharPredicates.VertWS();break;
                case 'V':predicate = CharPredicates.VertWS().negate();break;
                //读取\p{}
                case 'p':
                    next();
                    checkAndNext('{');
                    int start = i;
                    while (getCh() != '}'){
                        next();
                        if (isEnd()) {
                            throw new RuntimeException("错误的 unicode category");
                        }
                    }
                    String name = regex.substring(start, i);
                    checkAndNext('}');
                    int i = name.indexOf('=');
                    if (i != -1) {
                        // property construct \p{name=value}
                        String value = name.substring(i + 1);
                        name = name.substring(0, i).toLowerCase(Locale.ENGLISH);
                        switch (name) {
                            case "sc":
                            case "script":
                                predicate = CharPredicates.forUnicodeScript(value);
                                break;
                            case "blk":
                            case "block":
                                predicate = CharPredicates.forUnicodeBlock(value);
                                break;
                            case "gc":
                            case "general_category":
                                predicate = CharPredicates.forProperty(value, Modifier.openCaseInsensitive(modifier));
                                break;
                            default:
                                break;
                        }
                        if (predicate == null){
                            throw new RuntimeException("错误的 unicode category 类型");
                        }
                    } else {
                        if (name.startsWith("In")) {
                            // \p{InBlockName}
                            predicate = CharPredicates.forUnicodeBlock(name.substring(2));
                        } else if (name.startsWith("Is")) {
                            // \p{IsGeneralCategory} and \p{IsScriptName}
                            String shortName = name.substring(2);
                            predicate = CharPredicates.forUnicodeProperty(shortName, Modifier.openCaseInsensitive(modifier));
                            if (predicate == null)
                                predicate = CharPredicates.forProperty(shortName, Modifier.openCaseInsensitive(modifier));
                            if (predicate == null)
                                predicate = CharPredicates.forUnicodeScript(shortName);
                        } else {
                            if (Modifier.openUnicodeCharacterClass(modifier))
                                predicate = CharPredicates.forPOSIXName(name, Modifier.openCaseInsensitive(modifier));
                            if (predicate == null)
                                predicate = CharPredicates.forProperty(name, Modifier.openCaseInsensitive(modifier));
                        }
                        if (predicate == null)
                            throw new RuntimeException("错误的 unicode category 类型");
                    }
                    type =  Terminal.P;break;
                default:
                    character = getCh();
                    next();
                    break;
            }
        }
        return new TerminatorMsg(type,predicate,character);
    }


    private Ast single() {
        TerminalAst ast;
        //readTerminator会自动执行next()
        TerminatorMsg terminatorMsg = readTerminator();
        Character ch = terminatorMsg.ch;
        // 当没有转义符号时
        if (terminatorMsg.type == Terminal.SIMPLE && !terminatorMsg.hasTrans) {
            if(ch == '^') {
                ast =  new TerminalAst(Terminal.START);
            } else if (ch == '$') {
                ast = new TerminalAst(Terminal.END);
            } else if (ch == '.') {
                CharPredicates.CharPredicate charPredicate;
                if (Modifier.openDotAll(modifier)) {
                    charPredicate = CharPredicates.ALL();
                } else {
                    if (Modifier.openUnixLine(modifier)) {
                        charPredicate = CharPredicates.UNIXDOT();
                    } else {
                        charPredicate = CharPredicates.DOT();
                    }
                }
                ast =  new TerminalAst(Terminal.COMPOSITE);
                ast.predicate = charPredicate;
            } else if(ch == '[') {
                CharPredicates.CharPredicate classes = processClasses();
                ast = new TerminalAst(Terminal.COMPOSITE);
                ast.predicate = classes;
            } else if(ch == '('){
                return processGroup();
            } else {
                ast = new TerminalAst(ch,Terminal.SIMPLE);
            }
        }else if(Terminal.SIMPLE == terminatorMsg.type){
            ast = new TerminalAst(ch,Terminal.SIMPLE);
        }else {
            //复杂类型 \\d, \\s
            ast = new TerminalAst(terminatorMsg.type);
            ast.predicate = terminatorMsg.charPredicate;
        }
        //设置终结符的模式修正
        ast.modifier = modifier;
        return ast;
    }

    private CharPredicates.CharPredicate processClasses(){
        boolean isNegative = checkAndNext('^');
        //[]里面也可以放 \d,\w这种,
        int type = Terminal.COMPOSITE;
        Set<Character> chs = new HashSet<>();
        // [ 后面的]是普通字符
        if (getCh() == ']') {
            chs.add(']');
            next();
        }
        int level = 1;
        CharPredicates.CharPredicate result = new CharPredicates.EmptyCharPredicate();
        while (level != 0 && !isEnd()) {
            if(getCh() =='['){
                next();
                if (getCh() == ']') {
                    chs.add(']');
                    next();
                }
                level++;
            } else if(getCh() == ']'){
                next();
                level--;
            }else  {
                //读取字符类型并自动跳过
                TerminatorMsg curTerminatorMsg = readTerminator();
                if (curTerminatorMsg.type == Terminal.B || curTerminatorMsg.type == Terminal.b) {
                    throw new RuntimeException("[]中不允许有\\b,\\B");
                }
                //普通字符
                if (curTerminatorMsg.type == Terminal.SIMPLE) {
                    if (getCh() == '-' && getNext() != ']') {
                        next(1);
                        TerminatorMsg end = readTerminator();
                        if (end.type != Terminal.SIMPLE) {
                            throw new RuntimeException("[]包含错误的字符范围");
                        }
                        CharPredicates.CharPredicate range = processRange(curTerminatorMsg.ch, end.ch);
                        result = result.union(range);
                    } else{
                       //将字符放入chs集合中
                       singleCharCaseInsensitive(curTerminatorMsg.ch,chs);
                    }
                } else {
                    //复杂类型
                    result = result.union(curTerminatorMsg.charPredicate);
                    type = type | curTerminatorMsg.type;
                }
            }
        }
        result = result.union(chs::contains);
        if (isNegative) {
            result = result.negate();
        }
        return result;
    }


    private void singleCharCaseInsensitive(char ch, Set<Character> chs) {
        chs.add(ch);
        if (Modifier.openCaseInsensitive(modifier)) {
            if (Modifier.openUnicodeCharacterClass(modifier)) {
                chs.add(Character.toUpperCase(ch));
                chs.add(Character.toLowerCase(ch));
            } else{
                chs.add((char) ASCII.toUpper(ch));
                chs.add((char) ASCII.toLower(ch));
            }
        }
    }

    private CharPredicates.CharPredicate processRange(char start, char end) {
        CharPredicates.CharPredicate range = CharPredicates.range(start, end);
        if (Modifier.openUnicodeCharacterClass(modifier)) {
            char upperStart = Character.toUpperCase(start);
            char upperEnd = Character.toUpperCase(end);
            char lowerStart = Character.toLowerCase(start);
            char lowerEnd = Character.toLowerCase(end);
            range = range.union(CharPredicates.range(upperStart, upperEnd))
                    .union(CharPredicates.range(lowerStart, lowerEnd));
        } else{
            char upperStart = (char) ASCII.toUpper(start);
            char upperEnd = (char) ASCII.toUpper(end);
            char lowerStart = (char) ASCII.toLower(start);
            char lowerEnd = (char) ASCII.toLower(end);
            range = range.union(CharPredicates.range(upperStart, upperEnd))
                    .union(CharPredicates.range(lowerStart, lowerEnd));
        }
        return range;
    }
    private Ast processGroup(){
        int groupType = Group.CATCH_GROUP;
        //每个组都有私有的模式修正符
        int curModifiers = modifier;
        String groupName = null;
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
                    modifier |= negative ? modifier: Modifier.UNIX_LINES;
                    modifier &= negative ? Modifier.CLOSE_UNIX_LINES : modifier;
                }  else if(ch =='i'){
                    modifier |= negative ? modifier: Modifier.CASE_INSENSITIVE;
                    modifier &= negative ? Modifier.CLOSE_CASE_INSENSITIVE : modifier;
                } else if(ch == 'x'){
                    modifier |= negative ? modifier: Modifier.COMMENT;
                    modifier &= negative ? Modifier.CLOSE_COMMENT : modifier;
                } else if (ch == 'm') {
                    modifier |= negative ? modifier: Modifier.MULTILINE;
                    modifier &= negative ? Modifier.CLOSE_MULTILINE : modifier;
                } else if (ch == 's') {
                    modifier |= negative ? modifier: Modifier.DOTALL;
                    modifier &= negative ? Modifier.CLOSE_DOTALL : modifier;
                } else if(ch=='U'){
                    modifier |= negative ? modifier: Modifier.UNICODE_CHARACTER_CLASS;
                    modifier &= negative ? Modifier.CLOSE_UNICODE_CHARACTER_CLASS : modifier;
                }else if(ch ==')'){
                    // -) 错误的结尾
                    if (negative) {
                        throw new RuntimeException("不支持的分组类型");
                    }
                    next();
                    return new EmptyAst();
                } else if(ch == ':'){
                    break;
                } else{
                    break;
                }
                negative = false;
                next();
            }
            switch (getCh()){
                case ':': groupType = Group.NOT_CATCH_GROUP;break;
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
        //跳过)
        next();
        //设置组信息
        asTree.groupName = groupName;
        asTree.catchGroupNum = catchGroupNum;
        asTree.groupType = groupType;
        asTree.globalGroupNum = groupNum;
        if (groupType == Group.CATCH_GROUP) {
            catchGroups.add(asTree);
        }
        modifier = curModifiers;
        allGroups.add(asTree);
        return asTree;
    }
    private String readGroupName() {
        StringBuilder sb = new StringBuilder();
        if (!ASCII.isUpper(getCh()) && !ASCII.isLower(getCh())) {
            throw new RuntimeException("分组命名以大小写字母开头");
        }
        do {
            sb.append((getCh()));
            next();
        } while (ASCII.isUpper(getCh()) || ASCII.isLower(getCh()) || ASCII.isDigit(getCh()));
        if (getCh() != '>')
            throw new RuntimeException("分组命名以>结尾");
        return sb.toString();
    }
    private int getNum() {
        int num = 0;
        while (!isEnd() && ASCII.isDigit(getCh())) {
            num *= 10;
            num += getCh() - '0';
            next();
        }
        return num;
    }


    /**
     * terminator 类型信息
     */
    private static class TerminatorMsg {
       int type;
        /**
         * \p{} [:punct:] 复杂类型用得到
         */
       CharPredicates.CharPredicate charPredicate;

       Character ch;

       boolean hasTrans = false;

        public TerminatorMsg(int type) {
            this.type = type;
        }

        public TerminatorMsg(int type,Character ch) {
            this.type = type;
            this.ch = ch;
        }
        public TerminatorMsg(int type,Character ch,boolean hasTrans) {
            this.type = type;
            this.ch = ch;
            this.hasTrans = hasTrans;
        }

        public TerminatorMsg(int type, CharPredicates.CharPredicate charPredicate,Character character) {
            this.type = type;
            this.charPredicate = charPredicate;
            this.ch = character;
            hasTrans = true;
        }
    }

}
