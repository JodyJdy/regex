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
     *  捕获分组编号
     */
    int catchGroupNum = -1;

    /**
     * 全局分组编号
     */
    int globalGroupNum = -1;
    /**
     * 分组命名
     */
    String groupName;
    /**
     * 分组类型
     */
    int groupType;
    /**
     *  next 节点如果离开了组，离开组的编号，使用全局编号
     */
    int nextLeaveGroupNum = Util.NONE;
    /**
     * next节点离开组的类型
     */
    int nextLeaveGroupType = Util.NONE;

    /**
     * 当前节点以及所有子节点 最小/最大的编号
     */
    int nodeMinNumAstNo = Integer.MAX_VALUE;
    int nodeMaxNumAstNo = Util.NONE;

    /**
     *开启的flag
     */
    int openFlag = 0;
    /**
     * 关闭的flag
     */
    int closeFlag = Util.NONE;


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

    void setModifierFlag(int openFlag, int closeFlag) {
        this.openFlag = openFlag;
        this.closeFlag = closeFlag;
    }
}
