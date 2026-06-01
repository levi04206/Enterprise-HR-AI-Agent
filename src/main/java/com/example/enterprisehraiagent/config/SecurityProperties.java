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

    /**
     * 判断是否启用轻量级鉴权。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用轻量级鉴权。
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取员工姓名请求头名称。
     */
    public String getEmployeeHeader() {
        return employeeHeader;
    }

    /**
     * 设置员工姓名请求头名称。
     */
    public void setEmployeeHeader(String employeeHeader) {
        this.employeeHeader = employeeHeader;
    }

    /**
     * 获取角色请求头名称。
     */
    public String getRoleHeader() {
        return roleHeader;
    }

    /**
     * 设置角色请求头名称。
     */
    public void setRoleHeader(String roleHeader) {
        this.roleHeader = roleHeader;
    }

    /**
     * 获取免鉴权路径前缀列表。
     */
    public List<String> getPermitAllPrefixes() {
        return permitAllPrefixes;
    }

    /**
     * 设置免鉴权路径前缀列表。
     */
    public void setPermitAllPrefixes(List<String> permitAllPrefixes) {
        this.permitAllPrefixes = permitAllPrefixes;
    }
}
