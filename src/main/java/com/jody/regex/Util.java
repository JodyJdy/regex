package com.jody.regex;

import java.util.ArrayList;
import java.util.List;

class Util {

    static final EndAst END_AST = new EndAst();

    private static void doResetNext(Ast ast){
        if (ast instanceof OrAst) {
            OrAst orAst = (OrAst) ast;
            for(Ast node : orAst.asts){
                node.setNext(orAst.getNext());
                doResetNext(node);
            }
        } else if (ast instanceof CatAst) {
            CatAst catAst = (CatAst) ast;
            int len = catAst.ast.size();
            Ast last = catAst.ast.get(len - 1);
            last.setNext(ast.getNext());
            //cat子节点中只有最后一个的next发生了更改，其他子节点不用处理
            doResetNext(last);
        } else if (ast instanceof NumAst) {
            NumAst numAst = (NumAst) ast;
            if(numAst.type.equals(NumAst.MOST_1)) {
                numAst.ast.setNext(numAst.getNext());
            }
            doResetNext(numAst.ast);
        }
    }
    static void resetNext(Ast ast,Ast next){
        ast.setNext(next);
        doResetNext(ast);
    }
    static void checkEmpty(String regex){
        if(regex == null || regex.isEmpty()){
            throw new RuntimeException("regex is empty");
        }
    }

    /**
     * 是否是可以合并的终结符
     */
    private static boolean couldMergeTerminal(Ast ast){
        if(ast instanceof TerminalAst){
            TerminalAst l = (TerminalAst)ast;
            return Terminal.isSimple(l.type) && l.groupType == 0;
        }
        return false;
    }
    static List<Ast> mergeTerminal(List<Ast> astList){
        List<Ast> result = new ArrayList<>();
        int n = astList.size();
        int i = 0;
        while(i < n){
            //合并 ast(i)和 ast(i+1)
            if(couldMergeTerminal(astList.get(i))){
                int j = i + 1;
                while(j <n && couldMergeTerminal(astList.get(j))){
                    j++;
                }
                if(j == i + 1){
                    result.add(astList.get(i));
                    i++;
                } else{
                    StringBuilder sb = new StringBuilder();
                    for(int x = i;x<j;x++){
                        TerminalAst t = (TerminalAst)astList.get(x);
                        if(t.c != null){
                            sb.append(t.c);
                        } else{
                            sb.append(t.cs);
                        }
                    }
                    result.add(new TerminalAst(sb.toString(),Terminal.SIMPLE));
                    i = j;
                }
            } else{
                result.add(astList.get(i));
                i++;
            }
        }
        return result;
    }
}
