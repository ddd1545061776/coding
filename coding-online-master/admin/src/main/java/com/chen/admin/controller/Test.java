package com.chen.admin.controller;

import com.chen.biz.pojo.ExecMessage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) throws NoSuchMethodException {
        Class c=Test.class;
        Method test = c.getMethod("test", int[].class);
        Class<?> returnType = test.getReturnType();
        String name = returnType.getName();
        System.out.println(name);
    }
    public List<List<Integer>> test(int[] a){
        return new ArrayList<>();
    }
}
