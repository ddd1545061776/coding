package com.chen.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.biz.pojo.Class;
import com.chen.biz.vo.ClassInformation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author danger
 * @date 2021/5/13
 */
@Mapper
public interface ClassMapper extends BaseMapper<Class> {
    List<ClassInformation> getAllClass();
    int updateClassByName(@Param("oldClass") String oldClass, @Param("newClass") String newClass);

    List<ClassInformation> getClassSuccessNum(@Param("order") Long order);

    List<ClassInformation> getClassSubmitNum(@Param("order") Long order);

    List<ClassInformation> getNumWithWeekByQuestionId(@Param("questionId")Long questionId);

    int getSuccessNumWithWeekByQuestionId(@Param("questionId")Long questionId);

    int getAllNumWithWeekByQuestionId(@Param("questionId")Long questionId);
}
