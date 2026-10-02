package com.xxxx.server.config;

import com.xxxx.server.config.security.component.CustomFilter;
import com.xxxx.server.config.security.component.CustomUrlDecisionManager;
import com.xxxx.server.pojo.Menu;
import com.xxxx.server.pojo.Role;
import com.xxxx.server.service.IMenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomFilterTest {
    private CustomFilter filter;
    private CustomUrlDecisionManager decisions = new CustomUrlDecisionManager();
    private Authentication recruiter;
    private Authentication admin;

    @BeforeEach
    void setup() {
        IMenuService menus = mock(IMenuService.class);
        Role role = new Role().setName("ROLE_admin");
        when(menus.getMenusWithRole()).thenReturn(Arrays.asList(
            new Menu().setUrl("/system/cfg/**").setRoles(Collections.singletonList(role)),
            new Menu().setUrl("/system/admin/**").setRoles(Collections.singletonList(role))));
        filter = new CustomFilter();
        ReflectionTestUtils.setField(filter, "menuService", menus);
        recruiter = new UsernamePasswordAuthenticationToken("naqiao", null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_recruiter")));
        admin = new UsernamePasswordAuthenticationToken("admin", null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_admin")));
    }

    private Collection<ConfigAttribute> attributes(String method, String path, String query) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.setQueryString(query);
        return filter.getAttributes(new FilterInvocation(request, new MockHttpServletResponse(), (req, res) -> {}));
    }

    @Test
    void loggedInOperatorCanReadOwnMenuWithOrWithoutQueryParameters() {
        for (String query : new String[]{null, "refresh=1"}) {
            Collection<ConfigAttribute> attrs = attributes("GET", "/system/cfg/menu", query);
            assertDoesNotThrow(() -> decisions.decide(recruiter, null, attrs));
        }
    }

    @Test
    void anonymousUserCannotReadOwnMenu() {
        Authentication anonymous = new AnonymousAuthenticationToken("anonymous", "anonymousUser",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        assertThrows(AccessDeniedException.class,
            () -> decisions.decide(anonymous, null, attributes("GET", "/system/cfg/menu", null)));
    }

    @Test
    void exceptionDoesNotGrantOtherConfigurationOrWriteAccess() {
        for (String path : new String[]{"/system/cfg/menu", "/system/cfg/settings", "/system/admin/"}) {
            Collection<ConfigAttribute> attrs = attributes("POST", path, null);
            assertThrows(AccessDeniedException.class, () -> decisions.decide(recruiter, null, attrs));
            assertDoesNotThrow(() -> decisions.decide(admin, null, attrs));
        }
        assertThrows(AccessDeniedException.class,
            () -> decisions.decide(recruiter, null, attributes("GET", "/system/cfg/settings", null)));
    }

    @Test
    void queryParametersCannotBypassManagementRoleRestrictions() {
        Collection<ConfigAttribute> attrs = attributes("GET", "/system/admin/", "page=1&size=20");
        assertThrows(AccessDeniedException.class, () -> decisions.decide(recruiter, null, attrs));
        assertDoesNotThrow(() -> decisions.decide(admin, null, attrs));
    }
}
