package com.atguigu.userprofile.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangchen
 * @since 2021-05-04
 */
@Data
public class UserGroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableField(exist = false)
    private String busiDate;
    private String conditionComment;
    private String conditionJsonStr;
    private Date createTime;
    @TableId(value = "id", type = IdType.AUTO)  //声明主键  主键默认的生成方式 Auto= 数据库的auto_increment
    private Long id;
    @TableField(exist = false)   //声明 数据表中实际不存在该字段
    private List<TagCondition> tagConditions;
    private Date updateTime;
    private String updateType;
    private String userGroupComment;
    private String userGroupName;
    private Long userGroupNum;

    public String conditionJsonToComment() {
        StringBuilder comment = new StringBuilder();
        for (TagCondition tagCondition : tagConditions) {
            comment.append(tagCondition.tagName + " " + tagCondition.operatorName + " " + StringUtils.join(tagCondition.getTagValues(), ",") + " ;\n");
        }
        return comment.toString();
    }
}
