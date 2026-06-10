package com.institution.coursemanager.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页响应包装类
 */
@Data
public class PageResult<T> {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> list;

    public PageResult(Long total, Integer pageNum, Integer pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
    }

    public static <T> PageResult<T> of(Long total, Integer pageNum, Integer pageSize, List<T> list) {
        return new PageResult<>(total, pageNum, pageSize, list);
    }
}
