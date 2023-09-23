package com.example.regex;

/**
 * 组相关
 */
class Group {
    static final int CATCH_GROUP = 0B1;

    static final int NOT_CATCH_GROUP = 0b10;

    /**
     * 正向肯定查找
     */
    static final int FORWARD_POSTIVE_SEARCH = 0B100;
    /**
     * 反向肯定查找
     */

    static final int BACKWARD_POSTIVE_SEARCH = 0B1000;

    /**
     * 正向否定查找
     */
    static final int FORWARD_NEGATIVE_SEARCH = 0B10000;
    /**
     * 反向否定查找
     */

    static final int BACKWARD_NEGATIVE_SEARCH = 0B100000;

    static boolean isForwardSearch(int type){
        return type == FORWARD_NEGATIVE_SEARCH || type == FORWARD_POSTIVE_SEARCH
                || type == BACKWARD_NEGATIVE_SEARCH || type == BACKWARD_POSTIVE_SEARCH;
    }
}
