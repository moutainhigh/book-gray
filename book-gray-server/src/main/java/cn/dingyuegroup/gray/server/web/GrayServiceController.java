package cn.dingyuegroup.gray.server.web;

import cn.dingyuegroup.gray.core.GrayInstance;
import cn.dingyuegroup.gray.core.GrayService;
import cn.dingyuegroup.gray.server.manager.GrayServiceManager;
import cn.dingyuegroup.gray.server.model.vo.GrayInstanceVO;
import cn.dingyuegroup.gray.server.model.vo.GrayPolicyGroupVO;
import cn.dingyuegroup.gray.server.model.vo.GrayServiceVO;
import cn.dingyuegroup.gray.server.vertify.VertifyRequest;
import cn.dingyuegroup.gray.server.web.base.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gray/manager/services")
public class GrayServiceController extends BaseController {

    @Autowired
    private GrayServiceManager grayServiceManager;

    @RequestMapping("/index")
    public ModelAndView index(ModelAndView model) {
        List<GrayServiceVO> list = new ArrayList<>();
        List<GrayService> grayServices = grayServiceManager.getServices();
        grayServices.stream().forEach(e -> {
            GrayServiceVO vo = GrayServiceVO.builder()
                    .appName(e.getAppName())
                    .status(e.isStatus())
                    .serviceId(e.getServiceId())
                    .remark(e.getRemark())
                    .build();
            vo.setInstanceSize(e.getGrayInstances().size());
            vo.setHasGrayInstances(e.isOpenGray());
            vo.setHasGrayPolicies(e.hasGrayPolicy());
            list.add(vo);
        });
        model.addObject("list", list);
        model.setViewName("gray/service");
        return model;
    }

    @RequestMapping("/add")
    public String addService(@RequestParam String appName, @RequestParam String serviceId, @RequestParam String remark) {
        grayServiceManager.addService(appName, serviceId, remark);
        return "redirect:/gray/manager/services/index";
    }

    @RequestMapping("/edit")
    public String editService(@RequestParam String appName, @RequestParam String serviceId, @RequestParam String remark) {
        grayServiceManager.editService(appName, serviceId, remark);
        return "redirect:/gray/manager/services/index";
    }

    @RequestMapping("/delete")
    public String deleteService(@RequestParam String serviceId) {
        grayServiceManager.deleteService(serviceId);
        return "redirect:/gray/manager/services/index";
    }

    /**
     * 返回服务实例列表
     *
     * @param serviceId 服务id
     * @return 灰度服务实例VO列表
     */
    @RequestMapping(value = "/instances/index")
    public ModelAndView instances(ModelAndView model, @RequestParam("serviceId") String serviceId) {
        List<GrayInstanceVO> list = new ArrayList<>();
        List<GrayInstance> grayInstances = grayServiceManager.getInstances(serviceId);
        grayInstances.stream().forEach(e -> {
            GrayInstanceVO vo = GrayInstanceVO.builder()
                    .serviceId(serviceId)
                    .instanceId(e.getInstanceId())
                    .status(e.isStatus())
                    .appName(e.getAppName())
                    .url(e.getUrl())
                    .metadata(e.getMetadata())
                    .openGray(e.isOpenGray())
                    .hasGrayPolicies(e.hasGrayPolicy())
                    .remark(e.getRemark())
                    .policyGroupId(e.getGrayPolicyGroup() == null ? null : e.getGrayPolicyGroup().getPolicyGroupId())
                    .policyGroupAlias(e.getGrayPolicyGroup() == null ? null : e.getGrayPolicyGroup().getAlias())
                    .build();
            list.add(vo);
        });
        model.addObject("list", list);
        model.setViewName("gray/instance");
        return model;
    }

    @RequestMapping(value = "/instances/edit")
    public String editInstances(RedirectAttributes attr, @RequestParam("serviceId") String serviceId, @RequestParam("instanceId") String instanceId, @RequestParam("remark") String remark) {
        grayServiceManager.editInstance(serviceId, instanceId, remark);
        attr.addAttribute("serviceId", serviceId);
        return "redirect:/gray/manager/services/instances/index";
    }

    @RequestMapping(value = "/instances/delete")
    public String deleteInstances(RedirectAttributes attr, @RequestParam("serviceId") String serviceId, @RequestParam("instanceId") String instanceId) {
        grayServiceManager.deleteInstance(serviceId, instanceId);
        attr.addAttribute("serviceId", serviceId);
        return "redirect:/gray/manager/services/instances/index";
    }

    @VertifyRequest
    @ResponseBody
    @RequestMapping(value = "/instance/grayStatus", method = RequestMethod.GET)
    public ResponseEntity<Void> editInstanceGrayStatus(
            @RequestParam("serviceId") String serviceId,
            @RequestParam("instanceId") String instanceId,
            @RequestParam("status") int status) {
        boolean b = grayServiceManager.editInstanceGrayStatus(serviceId, instanceId, status);
        if (b) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @VertifyRequest
    @ResponseBody
    @RequestMapping(value = "/instance/onlineStatus", method = RequestMethod.GET)
    public ResponseEntity<Void> editInstanceOnlineStatus(
            @RequestParam("serviceId") String serviceId,
            @RequestParam("instanceId") String instanceId,
            @RequestParam("status") int status) {
        boolean b = grayServiceManager.editInstanceOnlineStatus(serviceId, instanceId, status);
        if (b) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 服务实例的所有灰度策略组
     *
     * @param serviceId  服务id
     * @param instanceId 实例id
     * @return 灰策略组VO列表
     */
    @VertifyRequest
    @ResponseBody
    @RequestMapping(value = "/instance/policyGroup", method = RequestMethod.GET)
    public ResponseEntity<GrayPolicyGroupVO> policyGroup(@RequestParam("serviceId") String serviceId,
                                                         @RequestParam("instanceId") String instanceId) {
        GrayInstance grayInstance = grayServiceManager.getGrayInstance(serviceId, instanceId);
        if (grayInstance == null) {
            return ResponseEntity.ok().build();
        }
        GrayPolicyGroupVO vo = new GrayPolicyGroupVO();
        vo.setServiceId(serviceId);
        vo.setInstanceId(instanceId);
        vo.setAppName(grayInstance.getAppName());
        if (grayInstance.getGrayPolicyGroup() != null) {
            vo.setPolicyGroupId(grayInstance.getGrayPolicyGroup().getPolicyGroupId());
            vo.setAlias(grayInstance.getGrayPolicyGroup().getAlias());
            vo.setPolicies(grayInstance.getGrayPolicyGroup().getList());
            vo.setEnable(grayInstance.getGrayPolicyGroup().isEnable());
        }
        return ResponseEntity.ok(vo);
    }

    @ResponseBody
    @RequestMapping(value = "/instance/policyGroup/status", method = RequestMethod.GET)
    public ResponseEntity<Void> editPolicyGroupStatus(@RequestParam("serviceId") String serviceId,
                                                      @RequestParam("instanceId") String instanceId,
                                                      @RequestParam("groupId") String groupId,
                                                      @RequestParam("status") int enable) {
        boolean b = grayServiceManager.editPolicyGroupStatus(serviceId, instanceId, groupId, enable);
        if (b) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }


    /**
     * 服务实例添加策略组
     *
     * @param serviceId 服务id
     * @return Void
     */
    @RequestMapping(value = "/instance/policyGroup/relate", method = RequestMethod.GET)
    public String addPolicyGroup(RedirectAttributes attr,
                                 @RequestParam("serviceId") String serviceId, @RequestParam("instanceId") String instanceId,
                                 @RequestParam(value = "policyGroupId", required = false) String groupId) {
        if (StringUtils.isEmpty(groupId) || !groupId.contains("POLICY_GROUP")) {
            grayServiceManager.delInstancePolicyGroup(serviceId, instanceId, null);
        } else {
            grayServiceManager.editInstancePolicyGroup(serviceId, instanceId, groupId);
        }
        attr.addAttribute("serviceId", serviceId);
        return "redirect:/gray/manager/services/instances/index";
    }


    /**
     * 服务实例删除策略组
     *
     * @param serviceId     服务id
     * @param instanceId    实例id
     * @param policyGroupId 灰度策略组id
     * @return Void
     */
    @ResponseBody
    @RequestMapping(value = "/instance/policyGroup/unRelate", method = RequestMethod.GET)
    public ResponseEntity<Void> delPolicyGroup(
            @RequestParam("serviceId") String serviceId,
            @RequestParam("instanceId") String instanceId,
            @RequestParam("groupId") String policyGroupId) {
        boolean b = grayServiceManager.delInstancePolicyGroup(serviceId, instanceId, policyGroupId);
        if (b) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
