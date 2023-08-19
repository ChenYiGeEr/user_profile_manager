package com.atguigu.userprofile.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangchen
 * @since 2021-04-14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "FileInfo对象", description = "")
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "扩展名")
    private String fileExName;
    @ApiModelProperty(value = "文件名")
    private String fileName;
    @ApiModelProperty(value = "文件路径")
    private String filePath;
    @ApiModelProperty(value = "文件状态 1 正常 2 弃用")
    private Long fileStatus;
    @ApiModelProperty(value = "文件系统")
    private String fileSystem;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


}
