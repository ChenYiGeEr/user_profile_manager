package com.atguigu.userprofile.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.userprofile.bean.TagCondition;
import com.atguigu.userprofile.bean.TagInfo;
import com.atguigu.userprofile.bean.UserGroup;
import com.atguigu.userprofile.constants.ConstCodes;
import com.atguigu.userprofile.mapper.UserGroupMapper;
import com.atguigu.userprofile.service.TagInfoService;
import com.atguigu.userprofile.service.UserGroupService;
import com.atguigu.userprofile.utils.RedisUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2021-05-04
 */
@Service
@Slf4j
@DS("mysql")
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup> implements UserGroupService {


    @Autowired
    TagInfoService tagInfoService;

    //1负责保存分群基本信息 mysql    mybatis-plus
    // 2  根据分群中的条件筛选出人群包(基于bitmap的组合查询)
    // 3 保存分群的人群包 -> clickhouse
    //  4  保存分群的人群包 -> redis
    @Override
    public void genUserGroup(UserGroup userGroup) {
        //1负责保存分群基本信息 mysql  （）
        //  condition_json_str 分群条件(json)
        String conditionJsonStr = JSON.toJSONString(userGroup.getTagConditions());
        userGroup.setConditionJsonStr(conditionJsonStr);
        //   condition_commentvarchar  分群条件(中文描述)
        String conditionComment = userGroup.conditionJsonToComment();
        userGroup.setConditionComment(conditionComment);
        // create_time
        userGroup.setCreateTime(new Date());
        this.saveOrUpdate(userGroup);  //会把新生成的主键id 写入到对象中 //mysql

        // 2.1  根据分群中的条件筛选出人群包(基于bitmap的组合查询)
        String bitAndSql = getBitAndSql(userGroup.getTagConditions(), userGroup.getBusiDate());
        System.out.println(bitAndSql);
        // 2.2  保存分群的人群包 -> clickhouse
        String insertUidsSql = getInsertUidsSql(userGroup.getId().toString(), bitAndSql);
        baseMapper.insertUidsSQL(insertUidsSql); //执行sql  //clickhouse

        //  3  保存人群包到redis 中
        //  数据哪里来 ： 基于已经写入到clickhouse中的人群包数据  查询出来 然后写入redis
        // 3.1 从clickhouse查询
        //通过bitmapToArray 把clickhouse中的bitmap取出 成为id的集合
        List<String> userIdsList = baseMapper.getUserIdListByUserGruopId(userGroup.getId().toString());
        // 3.2 往redis中写入
        //  type?  set   key ?  user_group:[id]   value?  uids ..    写api?  sadd     过期？不自动过期
        Jedis jedis = RedisUtil.getJedis();
        String[] uidArr = userIdsList.toArray(new String[]{});   //把list转为数组
        String key = "user_group:" + userGroup.getId();
        jedis.del(key);    // 每次新建人群包时要清理旧的
        if (uidArr.length > 0) {
            //批量提交
            jedis.sadd(key, uidArr);
        }
        jedis.close();

        // 4获得个数 更新到mysql中
        Long userCount = baseMapper.getUserCountByUserGruopId(userGroup.getId().toString());
        userGroup.setUserGroupNum(userCount);
        userGroup.setUpdateTime(new Date());
        baseMapper.updateById(userGroup);


    }

    /***
     * 方法：evaluateUserGroup
     * <p>计算分群人数 </p>
     *
     * @param userGroup 分群信息
     * @return java.lang.Long 分群人数
     * @since 2023/8/19 20:41
     * @author lim
     */
    @Override
    public Long evaluateUserGroup(UserGroup userGroup) {
        String bitAndSql = getBitAndSql(userGroup.getTagConditions(), userGroup.getBusiDate());
        String countUserSql = "select bitmapCardinality(" + bitAndSql + ")";
        return baseMapper.getUserCountBySQL(countUserSql);
    }

    @Override
    public void refreshUserGroup(String userGroupId, String busiDate) {
        //清除原来的人群包信息
        baseMapper.deleteUserCountById(userGroupId);
        //重新查询人群包  依据条件
        UserGroup userGroup = baseMapper.selectById(userGroupId);
        String conditionJsonStr = userGroup.getConditionJsonStr();
        List<TagCondition> tagConditions = JSON.parseArray(conditionJsonStr, TagCondition.class);
        userGroup.setTagConditions(tagConditions);
        userGroup.setBusiDate(busiDate);


        //更新clickhouse  redis  人数
        genUserGroup(userGroup);

    }

    /**
     * 方法：removeUserGroup
     * <p>根据用户群id删除用户群</p>
     *
     * @param userGroupId 用户群id
     * @author lim
     * @since 2023/8/20 18:19
     */
    @Override
    public void removeUserGroup(String userGroupId) {
        // 1. 请求参数校验
        if (StringUtils.isBlank(userGroupId)) {
            throw new RuntimeException("userGroupId cannot be empty!");
        }
        // 2. 删除mysql中该组信息
        baseMapper.deleteById(userGroupId);
        // 3. 删除redis中该组信息
        Jedis jedis = RedisUtil.getJedis();
        jedis.del("user_group:" + userGroupId);
        jedis.close();
        // 4. 删除clickhouse中该组信息
        baseMapper.deleteUserCountById(userGroupId);
    }

    //insert into  user_group
    //select ....

    /**
     * 方法：getInsertUidsSql
     * <p>获取新增位图表sql </p>
     *
     * @param userGroupId 用户分组编号
     * @param bitAndSql   位图sql
     * @return java.lang.String
     * @throws
     * @author lim
     * @version 1.0
     * @since 2023/8/19 22:48
     */
    private String getInsertUidsSql(String userGroupId, String bitAndSql) {
        String insertUidsSql = "insert into user_group select '" + userGroupId + "'," + bitAndSql;
        return insertUidsSql;
    }

    //select
    // bitmapAnd(
    //	 bitmapAnd(
    //	   (sql1)
    //	    ,
    //	   (sql2)
    //	 ),
    //	 (sql3)
    // )
    //

    /**
     * 方法：getBitAndSql
     * <p>生成查询clickhouse的用户画像位图表的sql </p>
     *
     * @param tagConditionList 查询条件
     * @param taskDate         查询时间
     * @return java.lang.String
     * @author lim
     * @since 2023/8/19 20:46
     */
    private String getBitAndSql(List<TagCondition> tagConditionList, String taskDate) {
        // 查询所有的标签信息
        Map<String, TagInfo> tagInfoMapWithCode = tagInfoService.getTagInfoMapWithCode();
        // 结果
        String bitsql = null;

        for (TagCondition tagCondition : tagConditionList) {
            if (bitsql == null) {
                bitsql = "(" + getConditionSQL(tagCondition, taskDate, tagInfoMapWithCode) + ")";
            } else {
                bitsql = " bitmapAnd ( " + bitsql + ",(" + getConditionSQL(tagCondition, taskDate, tagInfoMapWithCode) + "))";
            }

        }
        return bitsql;

    }


    //(select  groupBitmapMergeState(us)
// from user_tag_value_string
// where  tag_code ='tg_person_base_gender' and tag_value='女性’
// and dt=‘2021-05-16’)
    //  1    表名           要 通过tag_code 查询mysql 中tag_info 中的tag_value_type
//   2    tag_code  ->  参数
//   3   tag_value     ->   参数
//        加不加单引     ->要查询mysql 中tag_info 中的tag_value_type
//        一个还是多个  -> 根据tagvalues 数组长度
//  4   符号       ->  要根据operator  进行转义 =   >=    in  not in
//  5   业务日期  -> 参数
    private String getConditionSQL(TagCondition tagCondition, String taskDate, Map<String, TagInfo> tagInfoWithTagCodeMap) {
        String tagCode = tagCondition.getTagCode();
        TagInfo tagInfo = tagInfoWithTagCodeMap.get(tagCode);
        String tableName = getTableName(tagInfo.getTagValueType()); //根据tagValueType 得到表名
        List<String> tagValues = tagCondition.getTagValues();
        String tagValueSQL = getTagValueSQL(tagValues, tagInfo.getTagValueType(), tagCondition.getOperator());

        String conditionSQL = "select  groupBitmapMergeState(us)  from  " + tableName + " where  tag_code ='" + tagCode.toLowerCase() + "' and " + tagValueSQL + " and  dt='" + taskDate.replace("-", "") + "'";

        return conditionSQL;
    }


    //根据 相关条件组合tag_value的判断表达式 如：tag_value in ('70后','80后’)
    //   3   tag_value     ->   参数
    //        加不加单引     ->要查询mysql 中tag_info 中的tag_value_type
    //        一个还是多个  -> 根据tagvalues 数组长度

    private String getTagValueSQL(List<String> tagValues, String tagValueType, String operatorStr) {

        String values = null;
        if (tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_DATE) || tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_STRING)) {
            values = "'" + StringUtils.join(tagValues, "','") + "'";
        } else {
            values = StringUtils.join(tagValues, ",");
        }
        if (tagValues.size() > 1) {
            values = "(" + values + ")";
        }
        String conditionOperator = getConditionOperator(operatorStr);

        return "tag_value " + conditionOperator + " " + values;

    }

    //根据英文判断符号转为 sql判断符号
    private String getConditionOperator(String operator) {
        switch (operator) {
            case "eq":
                return "=";
            case "lte":
                return "<=";
            case "gte":
                return ">=";
            case "lt":
                return "<";
            case "gt":
                return ">";
            case "neq":
                return "<>";
            case "in":
                return "in";
            case "nin":
                return "not in";
        }
        throw new RuntimeException("操作符不正确");
    }


    //根据tagValueType 得到表名
    private String getTableName(String tagValueType) {
        String tableNamePrefix = "user_tag_value_";
        if (tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_LONG)) {
            return tableNamePrefix + "long";
        } else if (tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_DECIMAL)) {
            return tableNamePrefix + "decimal";
        } else if (tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_STRING)) {
            return tableNamePrefix + "string";
        } else if (tagValueType.equals(ConstCodes.TAG_VALUE_TYPE_DATE)) {
            return tableNamePrefix + "date";
        } else {
            throw new RuntimeException("类型不匹配！！");
        }

    }
}
