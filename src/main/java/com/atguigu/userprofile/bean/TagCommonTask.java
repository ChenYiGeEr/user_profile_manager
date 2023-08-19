package com.atguigu.userprofile.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangchen
 * @since 2021-04-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "TagCommonTask对象", description = "")
public class TagCommonTask implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableField(exist = false)
    private FileInfo fileInfo;
    private Long id;
    private String mainClass;
    private Long taskFileId;
    private Date updateTime;

}
