package de.honoka.qqrobot.farm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import de.honoka.qqrobot.farm.database.dao.LocationDao;
import de.honoka.qqrobot.farm.database.dao.RegisterRequestDao;
import de.honoka.qqrobot.farm.database.dao.UserDao;
import de.honoka.qqrobot.farm.entity.farm.Location;
import de.honoka.qqrobot.farm.entity.farm.RegisterRequest;
import de.honoka.qqrobot.farm.entity.farm.User;
import de.honoka.qqrobot.farm.util.LocationUtils;
import de.honoka.qqrobot.farm.web.WebConfigurations;
import de.honoka.qqrobot.spring.boot.starter.component.ExceptionReporter;
import de.honoka.qqrobot.spring.boot.starter.component.RobotBeanHolder;
import de.honoka.qqrobot.spring.boot.starter.component.session.RobotSession;
import de.honoka.qqrobot.spring.boot.starter.component.session.SessionManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

/**
 * 处理用户注册的逻辑
 */
@Service
public class RegisterService {

    /**
     * 手动注册
     */
    @Transactional
    public void manuallyRegister(long qq) {
        String province, city;
        try(RobotSession session = sessionManager.openSession(null, qq)) {
            //检查用户是否存在
            if(userDao.selectById(qq) != null) return;
            //获取用户提供的信息
            robotBeanHolder.getFramework().sendPrivateMsg(qq, "请回复你所在的" +
                    "省份（直辖市）名（目前仅支持国内省份，回复的省份名中不要带“省”（市）" +
                    "字，若一段时间内没有回复将自动关闭此次会话）");
            province = session.waitingForReply(30);
            robotBeanHolder.getFramework().sendPrivateMsg(qq, "请回复你所在的" +
                    "地级市（直辖市辖区或辖县）名（回复的市名中不要带“市”（区、县）字，" +
                    "除非这个名称只有一个字，如“开县”）");
            city = session.waitingForReply(30);
            //查询或新建地理位置信息
            Location location;
            //获取数据库中已有的关于此地理位置的信息
            location = locationDao.selectOne(new QueryWrapper<>(new Location()
                    .setProvince(province).setCity(city)));
            //若没有，则新建此地理位置的信息
            if(location == null) {
                location = LocationUtils.newLocation(province, city);
                if(location == null) {
                    robotBeanHolder.getFramework().sendPrivateMsg(qq,
                            "无法获取到此位置的天气或地形，注册未成功");
                    return;
                }
                locationDao.insert(location);
            }
            //使用此地理位置添加用户
            User newUser = new User();
            newUser.setQq(qq);
            newUser.setLocationId(location.getLocationId());
            userDao.insert(newUser);
            robotBeanHolder.getFramework().sendPrivateMsg(qq, "注册成功\n" +
                    "你的地理位置为：" + location.getFriendlyLocation() +
                    "\n地形为：" + location.getLandform());
        } catch(RobotSession.TimeoutException e) {
            //会话超时，不再执行注册
        } catch(Exception e) {
            robotBeanHolder.getFramework().sendPrivateMsg(qq,
                    "出现了错误，注册未成功");
            reporter.sendExceptionToDevelopingGroup(e);
            throw e;
        }
    }

    /**
     * 执行注册
     */
    @Transactional
    public String register(String id, String ip) {
        //检验注册请求ID是否合法
        RegisterRequest request = registerRequestDao.findAndLock(id);
        if(request == null) return "没有找到注册请求";
        //已过期
        if(System.currentTimeMillis() > request.getOverdueTime().getTime())
            return "此注册请求已过期，请重新发起注册";
        //ID合法
        //确认是否已有用户信息
        User u = userDao.selectById(request.getRequestQq());
        if(u != null) return "你已经注册了";
        //检验通过，执行注册
        //获取IP的地理位置
        String[] location0 = LocationUtils.ipLocationQuery(ip);
        if(location0 == null) return "没有找到你的IP地址的所在地，暂不能注册";
        //获取数据库中已有的关于此地理位置的信息
        Location location = locationDao.selectOne(new QueryWrapper<>(new Location()
                .setProvince(location0[0]).setCity(location0[1])));
        //若没有，则新建此地理位置的信息
        if(location == null) {
            location = LocationUtils.newLocation(location0[0], location0[1]);
            //无法获取到此位置的天气或地形，执行手动注册
            if(location == null) {
                new Thread(() -> {
                    registerService.manuallyRegister(request.getRequestQq());
                }).start();
                String reply = "IP：" + ip + "\n";
                reply += "Location：" + location0[0] + " " + location0[1] + "\n";
                reply += "你的IP地址所在区域无法获取到天气或地形，请在私聊会话中手动填写";
                return reply;
            }
            locationDao.insert(location);
            location = locationDao.selectOne(new QueryWrapper<>(new Location()
                    .setProvince(location0[0]).setCity(location0[1])));
        }
        //使用此地理位置添加用户
        User newUser = new User();
        newUser.setQq(request.getRequestQq());
        newUser.setLocationId(location.getLocationId());
        userDao.insert(newUser);
        return "注册成功\n你的地理位置为：" + location.getFriendlyLocation() +
                "\n地形为：" + location.getLandform();
    }

    /**
     * 某个QQ号请求注册
     */
    @Transactional
    public String requestRegister(Long group, long qq) {
        //确定是否已有用户信息
        User u = userDao.selectById(qq);
        if(u != null) return "你已经注册了";
        //生成注册请求ID
        //查找是否有现有可用的注册请求
        RegisterRequest request = registerRequestDao.getAvaliableRequest(qq);
        //若没有，则新建请求，并存储
        if(request == null) {
            request = new RegisterRequest()
                    .setRequestId(UUID.randomUUID().toString())
                    .setRequestQq(qq)
                    .setOverdueTime(new Date(System.currentTimeMillis() + 300 * 1000));
            registerRequestDao.insert(request);
        }
        //发送已生成的注册链接
        //honoka.de/qqrobot-farm/register?id=xxx
        String url = "http://www.honoka.de" + webConf.getContextPath() +
                "/register?id=" + request.getRequestId();
        robotBeanHolder.getFramework().sendPrivateMsg(qq, "请访问下面的链接" +
                "完成注册，将根据你的IP地址，来确定你的所在区域，以用于获取你所在区域" +
                "的地形与天气\n链接有效期为5分钟，此链接打开速度可能较慢，请耐心等待\n" +
                url);
        if(group != null) return "注册链接已通过私聊发送";
        return null;
    }

    @Resource
    private RegisterService registerService;

    @Resource
    private RegisterRequestDao registerRequestDao;

    @Resource
    private LocationDao locationDao;

    @Resource
    private RobotBeanHolder robotBeanHolder;

    @Resource
    private WebConfigurations webConf;

    @Resource
    private UserDao userDao;

    @Resource
    private ExceptionReporter reporter;

    @Resource
    private SessionManager sessionManager;
}
