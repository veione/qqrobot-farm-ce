package de.honoka.qqrobot.farm.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.honoka.qqrobot.farm.database.dao.ExceptionRecordDao;
import de.honoka.qqrobot.farm.database.dao.UsageLogDao;
import de.honoka.qqrobot.farm.entity.system.ExceptionRecord;
import de.honoka.qqrobot.farm.entity.system.UsageLog;
import de.honoka.qqrobot.farm.service.SystemService;
import de.honoka.qqrobot.farm.system.ExtendRobotAttributes;
import de.honoka.qqrobot.farm.util.JsonMaker;
import de.honoka.qqrobot.farm.web.common.ApiResponse;
import de.honoka.qqrobot.spring.boot.starter.component.RobotAttributes;
import de.honoka.qqrobot.spring.boot.starter.component.RobotBeanHolder;
import de.honoka.standard.system.SystemInfoBean;
import de.honoka.util.code.ActionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ApiController {

    private static final int USAGE_LOG_PAGE_SIZE = 20,    //使用记录的每页记录条数
            EXCEPTION_RECORD_MAX_SIZE = 10;    //异常信息最大显示条数

    @RequestMapping("/action/sendTestMessage")
    public ApiResponse<?> sendTestMessage() {
        ActionUtils.doAction("发送测试消息", systemService::sendTestMessage);
        return ApiResponse.success(null, null);
    }

    @RequestMapping("/action/relogin")
    public ApiResponse<?> relogin() {
        ActionUtils.doAction("重新登录", robotBeanHolder.getFramework()
                ::reboot);
        return ApiResponse.success(null, null);
    }

    @GetMapping("/main")
    public ApiResponse<?> mainInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("system_info", new SystemInfoBean());
        data.put("will_send_test_message_on_relogin", ExtendRobotAttributes
                .willSendTestMessageOnRelogin);
        data.put("will_resend_on_send_failed", ExtendRobotAttributes
                .willResendOnSendFailed);
        return ApiResponse.success(null, data);
    }

    @RequestMapping("/switchWillResendOnSendFailed")
    public String switchWillResendOnSendFailed() {
        ExtendRobotAttributes.willResendOnSendFailed =
                !ExtendRobotAttributes.willResendOnSendFailed;
        return JsonMaker.statusAndValue(true, null).toString();
    }

    @RequestMapping("/switchWillSendTestMessageOnRelogin")
    public String switchWillSendTestMessageOnRelogin() {
        ExtendRobotAttributes.willSendTestMessageOnRelogin =
                !ExtendRobotAttributes.willSendTestMessageOnRelogin;
        return JsonMaker.statusAndValue(true, null).toString();
    }

    @RequestMapping("/getException")
    public String getException() {
        List<ExceptionRecord> list = exceptionRecordDao.readException(
                EXCEPTION_RECORD_MAX_SIZE);
        JsonObject result = JsonMaker.arbitrary(
                "status", "success",
                "list", gson.toJsonTree(list));
        return gson.toJson(result);
    }

    @RequestMapping("/getUsageLog")
    public String getUsageLog(
            @RequestParam(required = false, defaultValue = "1") int page) {
        int maxPage;
        //获取信息
        //计算最大页数
        int count = usageLogDao.getCount();
        maxPage = count / USAGE_LOG_PAGE_SIZE;
        if(count % USAGE_LOG_PAGE_SIZE > 0) maxPage++;
        if(maxPage > 10) maxPage = 10;
        //修正不正确的当前页号
        if(page > maxPage) page = maxPage;    //此处page可能会被赋值为0
        if(page <= 0) page = 1;
        //获取使用记录
        List<UsageLog> logs = usageLogDao.selectPage(
                new Page<>(page, USAGE_LOG_PAGE_SIZE),
                new LambdaQueryWrapper<UsageLog>().orderByDesc(
                        UsageLog::getDatetime)
        ).getRecords();
        //组装Json
        JsonObject result = JsonMaker.arbitrary(
                "status", "success",
                "page", page,
                "maxPage", maxPage,
                "PAGE_SIZE", USAGE_LOG_PAGE_SIZE,
                "list", gson.toJsonTree(logs)
        );
        return gson.toJson(result);
    }

    @RequestMapping("/getConsole")
    public String getConsole() {
        String content = attributes.consoleWindow.getText();
        JsonObject result = JsonMaker.arbitrary(
                "status", "success",
                "data", content);
        return gson.toJson(result);
    }

    @Resource
    private RobotBeanHolder robotBeanHolder;

    @Resource
    private SystemService systemService;

    @Resource
    private UsageLogDao usageLogDao;

    @Resource
    private ExceptionRecordDao exceptionRecordDao;

    @Resource
    private RobotAttributes attributes;

    @Resource
    private Gson gson;
}
