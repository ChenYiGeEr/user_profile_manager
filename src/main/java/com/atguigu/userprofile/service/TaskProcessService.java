package com.atguigu.userprofile.service;

import com.atguigu.userprofile.bean.TaskProcess;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2021-04-28
 */
public interface TaskProcessService extends IService<TaskProcess> {


    void updateStatus(Long taskProcessId, String status);

    void updateStatus(Long taskProcessId, String yarnAppId, String status);

    void genTaskProcess(String taskDate);

    List<TaskProcess> getTodoTaskProcessList(String taskTime);
}
