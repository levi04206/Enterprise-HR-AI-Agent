package com.example.enterprisehraiagent.tool;

import com.example.enterprisehraiagent.entity.ToolCallLog;
import com.example.enterprisehraiagent.mapper.ToolCallLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class HrToolsConfigTest {

    @Autowired
    @Qualifier("getCompanyEmployeeSummaryTool")
    private Function<HrToolsConfig.EmployeeDirectoryRequest, String> employeeSummaryTool;

    @Autowired
    private ToolCallLogMapper toolCallLogMapper;

    @Test
    void employeeSummaryToolShouldReturnEmployeeCount() {
        String result = employeeSummaryTool.apply(new HrToolsConfig.EmployeeDirectoryRequest(false));

        assertThat(result).contains("当前公司共有 2 名员工");
    }

    @Test
    void employeeSummaryToolShouldReturnDetailsWhenRequested() {
        String result = employeeSummaryTool.apply(new HrToolsConfig.EmployeeDirectoryRequest(true));

        assertThat(result)
                .contains("当前公司共有 2 名员工")
                .contains("#1 张三")
                .contains("研发中心")
                .contains("zhangsan@example.com")
                .contains("#2 李四")
                .contains("人力资源部")
                .contains("lisi@example.com");

        List<ToolCallLog> logs = toolCallLogMapper.selectList(null);
        assertThat(logs)
                .extracting(ToolCallLog::getToolName)
                .contains("getCompanyEmployeeSummaryTool");
    }
}
