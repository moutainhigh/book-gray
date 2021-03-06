package cn.dingyuegroup.gray.server.mysql.dao;

import cn.dingyuegroup.gray.server.mysql.entity.GrayPolicyGroupEntity;

import java.util.List;

public interface GrayPolicyGroupMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(GrayPolicyGroupEntity record);

    GrayPolicyGroupEntity selectByPrimaryKey(Integer id);

    List<GrayPolicyGroupEntity> selectAll();

    int updateByPrimaryKey(GrayPolicyGroupEntity record);

    List<GrayPolicyGroupEntity> selectListByPolicyGroupId(List<String> groupIds);

    GrayPolicyGroupEntity selectByPolicyGroupId(String groupPolicyId);

    int updateByPolicyGroupId(GrayPolicyGroupEntity record);

    int deleteByGroupId(String groupPolicyId);

    int editByPolicyGroupId(GrayPolicyGroupEntity record);

    List<GrayPolicyGroupEntity> selectByDepartmentId(String departmentId);
}