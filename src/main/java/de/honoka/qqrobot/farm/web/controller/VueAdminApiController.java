package de.honoka.qqrobot.farm.web.controller;

import de.honoka.qqrobot.farm.system.ExtendRobotAttributes;
import de.honoka.qqrobot.farm.util.JsonMaker;
import de.honoka.qqrobot.farm.web.common.ApiResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/vue-admin")
public class VueAdminApiController {

    public static final String LOGIN_USERNAME = "robot_admin";

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if(ObjectUtils.anyNull(username, password)) {
            return ApiResponse.fail("用户名或密码不能为空");
        }
        //判断用户名密码
        boolean checkPassed = username.equals(LOGIN_USERNAME) &&
                password.equals(ExtendRobotAttributes.WEB_LOGIN_PASSWORD);
        //若未登录，且密码正确，添加登录状态
        //回应是否已登录，以及密码是否正确
        if(checkPassed) {
            return ApiResponse.success(null, JsonMaker.arbitraryMap(
                    "token", password));
        } else {
            return ApiResponse.fail("用户名或密码不正确");
        }
    }

    @GetMapping("/user_info")
    public ApiResponse<?> userInfo() {
        return ApiResponse.success(null, JsonMaker.arbitraryMap(
                "name", LOGIN_USERNAME,
                "avatar", "https://portrait.gitee.com" +
                        "/uploads/avatars/user/3398" +
                        "/10194555_kosaka-bun_1639635788.png!avatar200"
        ));
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout() {
        return ApiResponse.success(null, null);
    }
}
