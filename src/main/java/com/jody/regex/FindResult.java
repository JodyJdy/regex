package com.jody.regex;

import java.util.Objects;

/**
 * 一组查找结果
 */
public class FindResult implements Comparable<FindResult> {
    private final Integer left;
    private final Integer right;

    public FindResult(Integer left, Integer right) {
        this.left = left;
        this.right = right;
    }

    public Integer getLeft() {
        return left;
    }

    public Integer getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FindResult that = (FindResult) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public int compareTo(FindResult o) {
        if (!Objects.equals(this.left, o.left)) {
            return this.left - o.left;
        }
        return this.right - o.right;
    }

    @Override
    public String toString() {
        return "["+left+", "+right+"]";
    }
}
