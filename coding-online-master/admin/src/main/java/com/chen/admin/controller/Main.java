package com.chen.admin.controller;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author 邓冬冬
 * @date 2021/10/23
 */
public class Main {
    public static void main(String[] args) throws IOException{
       Object[] a=new Object[args.length];
        Class c=Main.class;
        Method[] methods = c.getMethods();
        for (Method m:
             methods) {
            if (!m.getName().equals("main")&&
                    !m.getName().equals("wait")&&
                    !m.getName().equals("equals")&&
                    !m.getName().equals("hashCode")&&
                    !m.getName().equals("toString")&&
                    !m.getName().equals("getClass")&&
                   ! m.getName().equals("notify")&&
                    !m.getName().equals("notifyAll")
            ){
                Class<?> returnType = m.getReturnType();
                Class<?>[] parameterTypes = m.getParameterTypes();
                for (int i = 0; i <parameterTypes.length ; i++) {
                    String name = parameterTypes[i].getName();
                    if(name.equals("int")||name.equals("Integer")){
                        int i1 = Integer.parseInt(args[i]);
                        a[i]=i1;
                        continue;
                    }
                    if(name.equals("double")||name.equals("Double")){
                        double v = Double.parseDouble(args[i]);
                        a[i]= v;
                        continue;
                    }
                    if(name.equals("[I")){
                        String[] split= args[i].replaceAll("\\[", "").replaceAll("]", "").split(",");
                        a[i]= Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                        continue;
                    }
                    if(name.equals("[Ljava.lang.String;")){
                        a[i]=args[i].replaceAll("\\[", "").replaceAll("]", "").split(",");
                        continue;
                    }
                    a[i]=args[i];
                }
                if (args.length==1){
                    Object invoke = null;
                    try {
                        invoke = m.invoke(Main.class, a[0]);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (returnType.getName().equals("[I")){
                        int[] vv=(int[])invoke;
                        System.out.println(Arrays.toString(vv));
                    }
                    if (returnType.getName().equals("[Ljava.lang.String;")){
                        String[] vv=(String[])invoke;
                        System.out.println(Arrays.toString(vv));
                    }
                    System.out.println(invoke);
                }
                if (args.length==2){
                    Object invoke = null;
                    try {
                        invoke = m.invoke(Main.class, a[0],a[1]);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (returnType.getName().equals("[I")){
                        int[] vv=(int[])invoke;
                        System.out.println(Arrays.toString(vv));
                    }
                    if (returnType.getName().equals("[Ljava.lang.String;")){
                        String[] vv=(String[])invoke;
                        System.out.println(Arrays.toString(vv));
                    }
                    System.out.println(invoke);
                }
            }
        }

        String resource="public class Main {\n" +
                "    public static void main(String[] args)   {\n" +
                "        test(1,2);\n" +
                "    }\n" +
                "    public static int[] test(int i1,int i2){\n" +
                "        return i1+i2;\n" +
                "    }\n" +
                "}";
        int i = resource.indexOf("{");
        int  i2=resource.indexOf("{",i+1);
        String substring = resource.substring(0,i2+1);
        String substring1 = resource.substring(i2+1);
        StringBuilder s=new StringBuilder();
        s.append("import java.lang.reflect.Method;\n import java.util.Arrays;\n import java.lang.reflect.InvocationTargetException;\n");
        s.append(substring);
        s.append(" Object[] a=new Object[args.length];\n" +
                "        Class c=Main.class;\n" +
                "        Method[] methods = c.getMethods();\n" +
                "        for (Method m:\n" +
                "             methods) {\n" +
                "            if (!m.getName().equals(\"main\")&&\n" +
                "                    !m.getName().equals(\"wait\")&&\n" +
                "                    !m.getName().equals(\"equals\")&&\n" +
                "                    !m.getName().equals(\"hashCode\")&&\n" +
                "                    !m.getName().equals(\"toString\")&&\n" +
                "                    !m.getName().equals(\"getClass\")&&\n" +
                "                   ! m.getName().equals(\"notify\")&&\n" +
                "                    !m.getName().equals(\"notifyAll\")\n" +
                "            ){\n" +
                "                Class<?> returnType = m.getReturnType();\n" +
                "                Class<?>[] parameterTypes = m.getParameterTypes();\n" +
                "                for (int i = 0; i <parameterTypes.length ; i++) {\n" +
                "                    String name = parameterTypes[i].getName();\n" +
                "                    if(name.equals(\"int\")||name.equals(\"Integer\")){\n" +
                "                        int i1 = Integer.parseInt(args[i]);\n" +
                "                        a[i]=i1;\n" +
                "                        continue;\n" +
                "                    }\n" +
                "                    if(name.equals(\"double\")||name.equals(\"Double\")){\n" +
                "                        double v = Double.parseDouble(args[i]);\n" +
                "                        a[i]= v;\n" +
                "                        continue;\n" +
                "                    }\n" +
                "                    if(name.equals(\"[I\")){\n" +
                "                        String[] split= args[i].replaceAll(\"\\\\[\", \"\").replaceAll(\"]\", \"\").split(\",\");\n" +
                "                        a[i]= Arrays.stream(split).mapToInt(Integer::parseInt).toArray();\n" +
                "                        continue;\n" +
                "                    }\n" +
                "                    if(name.equals(\"[Ljava.lang.String;\")){\n" +
                "                        a[i]=args[i].replaceAll(\"\\\\[\", \"\").replaceAll(\"]\", \"\").split(\",\");\n" +
                "                        continue;\n" +
                "                    }\n" +
                "                    a[i]=args[i];\n" +
                "                }\n" +
                "                if (args.length==1){\n" +
                "                    Object invoke = null;\n" +
                "                    try {\n" +
                "                        invoke = m.invoke(Main.class, a[0]);\n" +
                "                    } catch (IllegalAccessException e) {\n" +
                "                        e.printStackTrace();\n" +
                "                    } catch (InvocationTargetException e) {\n" +
                "                        e.printStackTrace();\n" +
                "                    }\n" +
                "                    if (returnType.getName().equals(\"[I\")){\n" +
                "                        String res=\"\";\n" +
                "                        int[] vv=(int[])invoke;\n" +
                "                        System.out.println(Arrays.toString(vv));\n" +
                "                    }\n" +
                "                    if (returnType.getName().equals(\"[Ljava.lang.String;\")){\n" +
                "                        String[] vv=(String[])invoke;\n" +
                "                        System.out.println(Arrays.toString(vv));\n" +
                "                    }\n" +
                "                }\n" +
                "                if (args.length==2){\n" +
                "                    Object invoke = null;\n" +
                "                    try {\n" +
                "                        invoke = m.invoke(Main.class, a[0],a[1]);\n" +
                "                    } catch (IllegalAccessException e) {\n" +
                "                        e.printStackTrace();\n" +
                "                    } catch (InvocationTargetException e) {\n" +
                "                        e.printStackTrace();\n" +
                "                    }\n" +
                "                    if (returnType.getName().equals(\"[I\")){\n" +
                "                        int[] vv=(int[])invoke;\n" +
                "                        for (int i:\n" +
                "                                vv) {\n" +
                "                            System.out.println(i);\n" +
                "                        }\n" +
                "                    }\n" +
                "                    if (returnType.getName().equals(\"[Ljava.lang.String;\")){\n" +
                "                        String[] vv=(String[])invoke;\n" +
                "                        for (String i:\n" +
                "                                vv) {\n" +
                "                            System.out.println(i);\n" +
                "                        }\n" +
                "                    }\n" +
                "                    System.out.println(invoke);\n" +
                "                }\n" +
                "            }\n" +
                "        }");
        s.append(substring1);
        System.out.println(s.toString());




        File file=new File("D:\\final\\coding-online\\sourceFile");
        file.mkdirs();
        File file1=new File("D:\\final\\coding-online\\sourceFile\\Main.java");
        OutputStream outputStream=new FileOutputStream(file1);
        PrintWriter printWriter=new PrintWriter(outputStream);
        printWriter.write(s.toString());
        printWriter.close();
        outputStream.close();
//        test(11,"323");
    }
    public static int test(int a,String b){
        System.out.println(a+b);
        return 14;
//        [Ljava.lang.String;  [I
}
    public static String[] stringToStringArray(String s){
     return s.replaceAll("\\[", "").replaceAll("]", "").split(",");
    }
    public static int[] stringToIntArray(String s){
        String s1 = s.replaceAll("\\[", "").replaceAll("]", "");
        String[] split = s1.split(",");
        int[] array = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
        return  array;
    }
}
