package com.atguigu.userprofile.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangchen
 * @since 2021-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private Date createTime;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(exist = false)
    private String parentTagCode;
    private Long parentTagId;
    @TableField(exist = false)
    private Long parentTagLevel;
    @TableField(exist = false)
    private String parentTagName;
    private String tagCode;
    private String tagComment;
    private Long tagLevel;
    private String tagName;
    private Long tagTaskId;
    private String tagType;
    private String tagValueType;
    @TableField(exist = false)
    private String taskStatus;


}
