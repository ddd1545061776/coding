package com.chen.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chen.biz.mapper.InputExampleMapper;
import com.chen.biz.mapper.InputTypeMapper;
import com.chen.biz.pojo.InputExample;
import com.chen.biz.pojo.InputType;
import com.chen.biz.pojo.QuestionType;
import com.chen.biz.service.InputExampleService;
import com.chen.biz.service.InputTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: sps-5113
 * @create: 2022-02-18 10:19
 **/
@Service
@Slf4j
public class InputTypeServiceImpl extends BaseServiceImpl<InputType, InputTypeMapper> implements InputTypeService {

    @Autowired
    private InputTypeMapper inputTypeMapper;

    @Override
    public String selectIdByTypeName(String type) {
        InputType filter = new InputType();
        filter.setTypeName(type);
        QueryWrapper<InputType> typeQueryWrapper = new QueryWrapper<>(filter);
        InputType inputType = inputTypeMapper.selectOne(typeQueryWrapper);
        if (inputType != null)
            return inputType.getInputTypeId();
        return null;
    }
}
