package com.example.enterprisehraiagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /**
     * 是否启用轻量级企业网关鉴权。
     */
    private boolean enabled = false;

    /**
     * 表示当前员工姓名的请求头。后续对接真实 SSO 时，可由网关写入该头。
     */
    private String employeeHeader = "X-HR-EMPLOYEE-NAME";

    /**
     * 表示当前用户角色的请求头。当前只识别 ADMIN，普通员工可以不传。
     */
    private String roleHeader = "X-HR-ROLE";

    /**
     * 不需要鉴权的路径前缀。
     */
    private List<String> permitAllPrefixes = List.of(
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs"
    );

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmployeeHeader() {
        return employeeHeader;
    }

    public void setEmployeeHeader(String employeeHeader) {
        this.employeeHeader = employeeHeader;
    }

    public String getRoleHeader() {
        return roleHeader;
    }

    public void setRoleHeader(String roleHeader) {
        this.roleHeader = roleHeader;
    }

    public List<String> getPermitAllPrefixes() {
        return permitAllPrefixes;
    }

    public void setPermitAllPrefixes(List<String> permitAllPrefixes) {
        this.permitAllPrefixes = permitAllPrefixes;
    }
}
