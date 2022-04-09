package com.chen.biz.service;

import com.chen.biz.mapper.ClassMapper;
import com.chen.biz.pojo.Class;
import com.chen.biz.vo.ClassInformation;

import java.util.List;

/**
 * @author danger
 * @date 2021/5/13
 */
public interface ClassService extends BaseService<Class, ClassMapper> {
    List<ClassInformation> getAllClass();

    Class getClassByName(String className);

    int updateClassByName(String oldClass, String newClass);

    List<ClassInformation> getClassSubmitNum(Long order);

    List<ClassInformation> getClassSuccessNum(Long order);

    List<ClassInformation> getNumWithWeekByQuestionId(Long questionId);

    int getSuccessNumWithWeekByQuestionId(Long questionId);

    int getAllNumWithWeekByQuestionId(Long questionId);
}
