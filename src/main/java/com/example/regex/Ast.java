package com.example.regex;

import java.util.Map;

/**
 * 抽象语法树
 */
abstract class Ast implements Cloneable{

    /**
     * 用于将 ast 扩展成一个链表
     */
    private Ast next;

    /**
     *  如果是一个分组，那么groupNum 不为0
     */
    int groupNum;
    /**
     * 分组类型
     */
    int groupType;
    /**
     * 捕获到的分组
     */
    int groupStart;
    int groupEnd;
    /**
     * 跳转到下个节点时，是否离开了一个group的范围
     */
    boolean nextLeaveGroup;
    /**
     * 离开组的编号
     */
    int leaveGroupNum;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    Ast getNext() {
        return next;
    }

    void setNext(Ast next) {
        this.next = next;
    }

    /**
     *  清除 NumAst的 circleNum,maxI信息
     */
    void clearNumAstStatus(){}
    /**
     * 存储 search时，当前节点的状态，并将当前节点设置为初始状态
     */
    void storeStatus(Map<Ast, MatcherStatus.AstStatus> map){
        map.put(this,new MatcherStatus.AstStatus(this));
    }
    /**
     * 还原 当前节点的状态
     */
    void loadStatus(Map<Ast,MatcherStatus.AstStatus> map){
        MatcherStatus.AstStatus status = map.get(this);
        this.groupStart = status.groupStart;
        this.groupEnd = status.groupEnd;
    }
}
