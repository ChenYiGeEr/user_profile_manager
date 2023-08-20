package com.atguigu.userprofile.service;

import com.atguigu.userprofile.bean.UserGroup;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserGroupService extends IService<UserGroup> {

    void genUserGroup(UserGroup userGroup);

    /***
     * 方法：evaluateUserGroup
     * <p>计算分群人数 </p>
     *
     * @param userGroup 分群信息
     * @return java.lang.Long 分群人数
     * @since 2023/8/19 20:41
     * @author lim
     */
    Long evaluateUserGroup(UserGroup userGroup);

    void refreshUserGroup(String userGroupId, String busiDate);

    /**
     * 方法：removeUserGroup
     * <p>根据用户群id删除用户群</p>
     *
     * @param userGroupId 用户群id
     * @author lim
     * @since 2023/8/20 18:19
     */
    void removeUserGroup(String userGroupId);
}
