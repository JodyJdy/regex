package com.jody.regex;

import java.util.ArrayList;
import java.util.List;

class Util {

    /**
     * 表示结尾节点，只需要实例化一个
     */
    static final EndAst END_AST = new EndAst();
    /**
     * 表示无意义的值
     */
    static final int NONE = -1;

    private static void doResetNext(Ast ast){
        if (ast instanceof OrAst) {
            OrAst orAst = (OrAst) ast;
            for(Ast node : orAst.asts){
                node.setNext(orAst.getNext());
                doResetNext(node);
            }
        } else if (ast instanceof CatAst) {
            CatAst catAst = (CatAst) ast;
            int len = catAst.asts.size();
            Ast last = catAst.asts.get(len - 1);
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

    /**
     *设置 ast节点的
     * numAstNoMin
     * numAstNoMax 值
     */
     static void setNodeMinMaxNumAstNo(Ast ast) {
        if (ast instanceof NumAst) {
            setNodeMinMaxNumAstNo(((NumAst) ast).ast);
            setNode(ast,((NumAst) ast).ast);
            return;
        }
        if (ast instanceof EndAst || ast instanceof TerminalAst || ast instanceof  ModifierAst) {
            return;
        }
        if (ast instanceof CatAst) {
            for (Ast a : ((CatAst) ast).asts) {
                setNodeMinMaxNumAstNo(a);
                setNode(ast,a);
            }
        }
        if (ast instanceof OrAst) {
            for (Ast a : ((OrAst) ast).asts) {
                setNodeMinMaxNumAstNo(a);
                setNode(ast,a);
            }
        }
    }

    private static void setNode(Ast notSet, Ast set) {
        if (notSet instanceof NumAst) {
            int numAstNo = ((NumAst) notSet).numAstNo;
            notSet.nodeMinNumAstNo = Math.min(notSet.nodeMinNumAstNo,Math.min(numAstNo, set.nodeMinNumAstNo));
            notSet.nodeMaxNumAstNo =   Math.max(notSet.nodeMaxNumAstNo,Math.max(numAstNo, set.nodeMaxNumAstNo));
        } else{
            notSet.nodeMinNumAstNo = Math.min(notSet.nodeMinNumAstNo,set.nodeMinNumAstNo);
            notSet.nodeMaxNumAstNo = Math.max(notSet.nodeMaxNumAstNo,set.nodeMaxNumAstNo);
        }
    }
}
