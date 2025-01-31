package com.codingstudy.login.configuration.auth;

import com.codingstudy.login.service.SysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Map;

/**
 * 重写UsernamePasswordAuthenticationFilter过滤器
 */
public class MyUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    SysUserService userService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        if (request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
            ObjectMapper mapper = new ObjectMapper();
            UsernamePasswordAuthenticationToken authRequest;
            //取authenticationBean
            //用try with resource，方便自动释放资源
            try (InputStream is = request.getInputStream()) {
                Map<?, ?> authenticationBean = mapper.readValue(is, Map.class);
                if (!authenticationBean.isEmpty()) {
                    //获得账号、密码
                    String username = authenticationBean.get(SPRING_SECURITY_FORM_USERNAME_KEY).toString();
                    String password = authenticationBean.get(SPRING_SECURITY_FORM_PASSWORD_KEY).toString();
                    //可以验证账号、密码
                    //System.out.println("username = " + username);
                    //System.out.println("password = " + password);
                    //检测账号、密码是否存在
                    if (userService.checkLogin(username, password)) {
                        //将账号、密码装入UsernamePasswordAuthenticationToken中
                        authRequest = new UsernamePasswordAuthenticationToken(username, password);
                        setDetails(request, authRequest);
                        return this.getAuthenticationManager().authenticate(authRequest);
                    }
                }
            } catch (Exception e) {
                throw new MyAuthenticationException(e.getMessage());
            }
            return null;
        } else {
            return this.attemptAuthentication(request, response);
        }
    }
}
