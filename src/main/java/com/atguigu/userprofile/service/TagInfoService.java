package com.atguigu.userprofile.service;

import com.atguigu.userprofile.bean.TagInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2021-04-13
 */
public interface TagInfoService extends IService<TagInfo> {

    List<TagInfo> getTagInfoAllWithStatus();

    TagInfo getTagInfo(Long taskId);

    List<TagInfo> getTagValueList(String parentTagCode);

    Map<String, TagInfo> getTagInfoMapWithCode();

}
