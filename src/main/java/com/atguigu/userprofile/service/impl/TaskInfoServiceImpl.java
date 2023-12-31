package com.atguigu.userprofile.service.impl;

import com.atguigu.userprofile.bean.FileInfo;
import com.atguigu.userprofile.bean.TagInfo;
import com.atguigu.userprofile.bean.TaskInfo;
import com.atguigu.userprofile.bean.TaskTagRule;
import com.atguigu.userprofile.constants.ConstCodes;
import com.atguigu.userprofile.mapper.TaskInfoMapper;
import com.atguigu.userprofile.service.FileInfoService;
import com.atguigu.userprofile.service.TagInfoService;
import com.atguigu.userprofile.service.TaskInfoService;
import com.atguigu.userprofile.service.TaskTagRuleService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2021-04-15
 */
@Service
@DS("mysql")
public class TaskInfoServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements TaskInfoService {

    @Autowired
    TaskTagRuleService taskTagRuleService;

    @Autowired
    TagInfoService tagInfoService;

    @Autowired
    FileInfoService fileInfoService;

    @Transactional
    public void saveTaskInfoWithTag(TaskInfo taskInfo) {
        if (taskInfo.getId() != null) {
            // id不为null,说明是更新
            TaskTagRule taskTagRuleForRm = new TaskTagRule();
            taskTagRuleForRm.setTaskId(taskInfo.getTagId());
            taskTagRuleService.remove(new QueryWrapper<TaskTagRule>().eq("task_id", taskInfo.getId()));
        } else {
            // 否则为更新
            taskInfo.setCreateTime(new Date());
        }
        saveOrUpdate(taskInfo);
        List<TaskTagRule> taskTagRuleList = taskInfo.getTaskTagRuleList();
        if (taskTagRuleList != null && taskTagRuleList.size() > 0) {
            for (TaskTagRule taskTagRule : taskTagRuleList) {
                taskTagRule.setTaskId(taskInfo.getId());
            }
            taskTagRuleService.saveBatch(taskTagRuleList);
        }
        if (taskInfo.getTaskType().equals(ConstCodes.TASK_TYPE_TAG)) {
            TagInfo tagInfoForUpdate = new TagInfo();
            tagInfoForUpdate.setTagTaskId(taskInfo.getId());
            tagInfoForUpdate.setId(taskInfo.getTagId());
            tagInfoService.updateById(tagInfoForUpdate);
        }


    }

    public TaskInfo getTaskInfoWithTag(Long taskId) {
        TaskInfo taskInfo = getById(taskId);
        if (taskInfo.getFileId() != null) {
            FileInfo fileInfo = fileInfoService.getById(taskInfo.getFileId());
            taskInfo.setFileName(fileInfo.getFileName());
            taskInfo.setFilePath(fileInfo.getFilePath());
        }
        List<TaskTagRule> taskTagRuleList = taskTagRuleService.list(new QueryWrapper<TaskTagRule>().eq("task_id", taskId));
        taskInfo.setTaskTagRuleList(taskTagRuleList);
        return taskInfo;
    }
}
