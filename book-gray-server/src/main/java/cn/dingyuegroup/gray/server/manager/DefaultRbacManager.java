package cn.dingyuegroup.gray.server.manager;

import cn.dingyuegroup.gray.server.model.vo.GrayRbacUserVO;
import cn.dingyuegroup.gray.server.mysql.dao.GrayRbacDepartmentMapper;
import cn.dingyuegroup.gray.server.mysql.dao.GrayRbacRoleMapper;
import cn.dingyuegroup.gray.server.mysql.dao.GrayRbacUserMapper;
import cn.dingyuegroup.gray.server.mysql.dao.GrayRbacUserRoleMapper;
import cn.dingyuegroup.gray.server.mysql.entity.GrayRbacDepartment;
import cn.dingyuegroup.gray.server.mysql.entity.GrayRbacRole;
import cn.dingyuegroup.gray.server.mysql.entity.GrayRbacUser;
import cn.dingyuegroup.gray.server.mysql.entity.GrayRbacUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 170147 on 2019/1/22.
 */
@Service
public class DefaultRbacManager implements RbacManager {

    private Lock lock = new ReentrantLock();

    @Autowired
    private GrayRbacDepartmentMapper grayRbacDepartmentMapper;

    @Autowired
    private GrayRbacUserMapper grayRbacUserMapper;

    @Autowired
    private GrayRbacRoleMapper grayRbacRoleMapper;

    @Autowired
    private GrayRbacUserRoleMapper grayRbacUserRoleMapper;

    /**
     * 获取部门下的人员名单
     *
     * @param departmentId
     * @return
     */
    @Override
    public List<GrayRbacUserVO> list(String departmentId) {
        List<GrayRbacUserVO> list = new ArrayList<>();
        GrayRbacDepartment department = grayRbacDepartmentMapper.selectByDepartmentId(departmentId);
        if (department == null) {
            return list;
        }
        List<GrayRbacUser> grayRbacUsers = grayRbacUserMapper.selectByDepartmentId(departmentId);
        if (!CollectionUtils.isEmpty(grayRbacUsers)) {
            return list;
        }
        grayRbacUsers.forEach(e -> {
            GrayRbacUserVO vo = new GrayRbacUserVO();
            vo.setDepartmentName(department.getDepartmentName());
            vo.setNickname(e.getNickname());
            vo.setRemark(e.getRemark());
            vo.setUdid(e.getUdid());
            List<GrayRbacUserRole> grayRbacUserRoles = grayRbacUserRoleMapper.selectByUdid(e.getUdid());
            if (CollectionUtils.isEmpty(grayRbacUserRoles)) {
                return;
            }
            grayRbacUserRoles.forEach(f -> {
                GrayRbacRole grayRbacRole = grayRbacRoleMapper.selectByRoleId(f.getRoleId());
                if (StringUtils.isEmpty(vo.getRoleName())) {
                    vo.setRoleName(grayRbacRole.getRoleName());
                } else {
                    vo.setRoleName(vo.getRoleName() + "," + grayRbacRole.getRoleName());
                }
            });
            list.add(vo);
        });
        return list;
    }

    /**
     * 添加人员
     *
     * @param departmentId
     * @param roleId
     * @param nickName
     * @param remark
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean addUser(String departmentId, String roleId, String nickName, String remark) {
        GrayRbacDepartment department = grayRbacDepartmentMapper.selectByDepartmentId(departmentId);
        if (department == null) {
            return false;
        }
        GrayRbacRole role = grayRbacRoleMapper.selectByRoleId(roleId);
        if (role == null) {
            return false;
        }
        lock.lock();
        try {
            GrayRbacUser user = new GrayRbacUser();
            user.setUdid(GrayRbacUser.genId());
            user.setRemark(remark);
            user.setDepartmentId(departmentId);
            user.setNickname(nickName);
            grayRbacUserMapper.insert(user);
            GrayRbacUserRole grayRbacUserRole = new GrayRbacUserRole();
            grayRbacUserRole.setUdid(user.getUdid());
            grayRbacUserRole.setRoleId(roleId);
            grayRbacUserRoleMapper.insert(grayRbacUserRole);
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 编辑用户
     *
     * @param udid
     * @param nickName
     * @param remark
     * @return
     */
    @Override
    public boolean editUser(String udid, String nickName, String remark) {
        GrayRbacUser user = grayRbacUserMapper.selectByUdid(udid);
        if (user == null) {
            return false;
        }
        user.setRemark(remark);
        user.setNickname(nickName);
        grayRbacUserMapper.updateByUdid(user);
        return true;
    }

    /**
     * 删除用户
     *
     * @param udid
     * @return
     */
    @Override
    public boolean deleteUser(String udid) {
        grayRbacUserMapper.deleteByUdid(udid);
        return true;
    }
}