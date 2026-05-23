package com.example.enterprisehraiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.enterprisehraiagent.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
