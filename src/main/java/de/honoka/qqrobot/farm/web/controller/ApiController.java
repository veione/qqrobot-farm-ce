package de.honoka.qqrobot.farm.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.honoka.qqrobot.farm.database.dao.ExceptionRecordDao;
import de.honoka.qqrobot.farm.database.dao.UsageLogDao;
import de.honoka.qqrobot.farm.entity.system.ExceptionRecord;
import de.honoka.qqrobot.farm.entity.system.UsageLog;
import de.honoka.qqrobot.farm.system.ExtendRobotAttributes;
import de.honoka.qqrobot.farm.util.JsonMaker;
import de.honoka.qqrobot.farm.web.interceptor.LoginInterceptor;
import de.honoka.qqrobot.spring.boot.starter.component.RobotAttributes;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final int USAGE_LOG_PAGE_SIZE = 20,    //使用记录的每页记录条数
            EXCEPTION_RECORD_MAX_SIZE = 10;    //异常信息最大显示条数

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

    @RequestMapping("/checkLogin")
    public String checkLogin(
            @RequestParam("robot_username") String username,
            @RequestParam("robot_password") String password,
            HttpSession session) {
        //判断登录状态
        boolean alreayLogin = loginInterceptor.checkLoginStatus(session);
        //判断用户名密码
        boolean checkPassed = username.equals("robot_admin") &&
                password.equals(ExtendRobotAttributes.WEB_LOGIN_PASSWORD);
        //若未登录，且密码正确，添加登录状态
        if(!alreayLogin && checkPassed)
            session.setAttribute("loginUUID", password);
        //回应是否已登录，以及密码是否正确
        return JsonMaker.arbitrary("check_passed", checkPassed).toString();
    }

    @RequestMapping("/getException")
    @SneakyThrows
    public String getException() {
        List<ExceptionRecord> list = exceptionRecordDao.readException(
                EXCEPTION_RECORD_MAX_SIZE);
        JsonObject result = JsonMaker.arbitrary(
                "status", "success",
                "list", gson.toJsonTree(list));
        return gson.toJson(result);
    }

    @RequestMapping("/getUsageLog")
    @SneakyThrows
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
    private UsageLogDao usageLogDao;

    @Resource
    private ExceptionRecordDao exceptionRecordDao;

    @Resource
    private LoginInterceptor loginInterceptor;

    @Resource
    private RobotAttributes attributes;

    @Resource
    private Gson gson;
}
