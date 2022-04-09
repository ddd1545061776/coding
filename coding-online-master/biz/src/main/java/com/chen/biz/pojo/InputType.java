package com.chen.biz.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: sps-5113
 * @create: 2022-02-18 10:16
 **/
@TableName("input_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputType {
    @TableId(value = "input_type_id")
    private String inputTypeId;
    @TableField(value = "type_name")
    private String typeName;
}
