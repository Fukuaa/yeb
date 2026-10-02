package com.xxxx.server.config.security.component;

import com.xxxx.server.pojo.Menu;
import com.xxxx.server.pojo.Role;
import com.xxxx.server.service.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.List;

@Component
public class CustomFilter implements FilterInvocationSecurityMetadataSource {
    @Autowired
    private IMenuService menuService;
    AntPathMatcher AntPathMatcher = new AntPathMatcher();
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        FilterInvocation invocation = (FilterInvocation) o;
        String requestUrl = invocation.getRequest().getServletPath();
        // This endpoint returns only the current operator's permitted menus.
        // It must be available before applying the broader system-configuration roles.
        if ("GET".equals(invocation.getRequest().getMethod()) && "/system/cfg/menu".equals(requestUrl)) {
            return SecurityConfig.createList("ROLE_LOGIN");
        }
        List<Menu> menus = menuService.getMenusWithRole();
        for (Menu menu:menus){
            if(AntPathMatcher.match(menu.getUrl(),requestUrl)){
                String[] s = menu.getRoles().stream().map(Role::getName).toArray(String[]::new);
                return SecurityConfig.createList(s);

            }
        }
        return SecurityConfig.createList("ROLE_LOGIN");
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }
}
