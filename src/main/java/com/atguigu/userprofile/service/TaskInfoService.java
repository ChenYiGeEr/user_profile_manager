package com.atguigu.userprofile.service;

import com.atguigu.userprofile.bean.TaskInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2021-04-15
 */
public interface TaskInfoService extends IService<TaskInfo> {
    void saveTaskInfoWithTag(TaskInfo taskInfo);

    TaskInfo getTaskInfoWithTag(Long taskId);


}
