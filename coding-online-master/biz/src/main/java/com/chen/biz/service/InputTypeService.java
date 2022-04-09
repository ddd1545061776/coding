package com.chen.biz.service;

import com.chen.biz.mapper.ClassMapper;
import com.chen.biz.mapper.InputTypeMapper;
import com.chen.biz.pojo.Class;
import com.chen.biz.pojo.InputType;

/**
 * @author: sps-5113
 * @create: 2022-02-18 10:18
 **/
public interface InputTypeService extends BaseService<InputType, InputTypeMapper>{
    String selectIdByTypeName(String type);
}
