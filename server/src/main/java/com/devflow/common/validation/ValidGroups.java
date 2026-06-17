package com.devflow.common.validation;

/**
 * 参数校验分组
 */
public final class ValidGroups {

    /**
     * 私有构造防止实例化
     */
    private ValidGroups() {}

    /**
     * 新增分组
     */
    public interface Create {}

    /**
     * 更新分组
     */
    public interface Update {}
}
