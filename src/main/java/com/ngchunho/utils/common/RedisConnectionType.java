package com.ngchunho.utils.common;

/**
 * RedisConnectionType
 *
 * @author ngchunho
 * @version 1.0.0
 * @description
 * @date 2022/2/17 15:09
 */
public enum RedisConnectionType {

    /**单节点*/
    NODE,

    /**集群*/
    CLUSTER,

    /**哨兵*/
    SENTINEL;
}
