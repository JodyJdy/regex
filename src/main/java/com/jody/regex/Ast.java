package com.jody.regex;


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
     * 分组命名
     */
    String groupName;
    /**
     * 分组类型
     */
    int groupType;
    /**
     * 跳转到下个节点时，是否离开了一个group的范围
     */
    boolean nextLeaveGroup;
    /**
     * 离开组的编号
     */
    int leaveGroupNum;

    /**
     * 当前节点以及所有子节点 最小/最大的编号
     */
    int nodeMinNumAstNo = Integer.MAX_VALUE;
    int nodeMaxNumAstNo = Util.NONE;


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
}
