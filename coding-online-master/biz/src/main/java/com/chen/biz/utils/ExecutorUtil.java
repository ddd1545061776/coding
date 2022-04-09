package com.chen.biz.utils;

import com.chen.biz.pojo.ExecMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;

/**
 * 执行命令行工具类
 * @author danger
 * @date 2021/4/15
 */
@Slf4j
public class ExecutorUtil {

    /**
     * 执行命令行并返回信息
     * @param cmd 待执行的命令
     * @return 执行信息
     */
    public static ExecMessage exec(String cmd) {
        log.info(cmd);
        Runtime runtime = Runtime.getRuntime();
        Process exec = null;
        try {
            exec = runtime.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return new ExecMessage(e.getMessage(), null);
        }
        ExecMessage res = new ExecMessage();
        String error = message(exec.getErrorStream());
        res.setError(error);
        if (!StringUtils.hasLength(error))
            res.setStdout(message(exec.getInputStream()));
        System.out.println("res = " + res);
        return res;
    }

    public static ExecMessage execJavaWithInput(String cmd, String input) {
        log.info(cmd);
        String[] split = input.split("#");
        StringBuilder cmds=new StringBuilder();
        cmds.append(cmd);
        for (int i = 0; i <split.length ; i++) {
            cmds.append(" ");
            cmds.append(split[i]);
        }
        cmd=cmds.toString();
        Runtime runtime = Runtime.getRuntime();
        Process exec = null;
        try {
            exec = runtime.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return new ExecMessage(e.getMessage(), null);
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));
        try {
            bw.write(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ExecMessage res = new ExecMessage();
        String error = message(exec.getErrorStream());
        res.setError(error);
        if (!StringUtils.hasLength(error)) {
            res.setStdout(message(exec.getInputStream()));
            System.out.println("res = " + res);
        }
        return res;
    }

    public static ExecMessage execPythonWithInput(String cmd, String input,String[] strings) throws IOException {
        log.info(cmd);
        String[] split = input.split("#");
        StringBuilder cmds=new StringBuilder();
        cmds.append(cmd);
        for (int i = 0; i <split.length ; i++) {
            cmds.append(" ");
            cmds.append(split[i]);
            cmds.append(" ");
            cmds.append(strings[i]);
        }
        cmd=cmds.toString();
        Runtime runtime = Runtime.getRuntime();
        Process exec = null;
        try {
            exec = runtime.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return new ExecMessage(e.getMessage(), null);
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));
        try {
            bw.write(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ExecMessage res = new ExecMessage();
//        InputStream in = exec.getErrorStream();
//        byte b[] = new byte[1024];
//        int c=-1;
//        OutputStream os =new FileOutputStream("D:\\final\\coding-online\\sourceFile\\cs.txt");
//        while ((c=(in.read(b,0,b.length)))!=-1) {
//            os.write(b);
//        }
        String error = message(exec.getErrorStream());
        res.setError(error);
        if (!StringUtils.hasLength(error)) {
            res.setStdout(message(exec.getInputStream()));
            System.out.println("res = " + res);
        }
        return res;
    }

    public static ExecMessage execWithNoInput(String cmd) {
        log.info(cmd);
        Runtime runtime = Runtime.getRuntime();
        Process exec = null;
        try {
            exec = runtime.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return new ExecMessage(e.getMessage(), null);
        }
        ExecMessage res = new ExecMessage();
        String error = messageWithRunTest(exec.getErrorStream());
        res.setError(error);
        if (!StringUtils.hasLength(error)){
            res.setStdout(messageWithRunTest(exec.getInputStream()));
        }
        System.out.println("res = " + res);
        return res;
    }
    /**
     * 用于将命令行提示转为字符串
     * @param inputStream 流信息
     * @return 信息字符串
     */
    private static String message(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
            StringBuilder message = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                message.append(str+"\n");
            }
            reader.close();
            String result = message.toString();
            if (result.equals("")) {
                return null;
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            try {
                inputStream.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static String messageWithRunTest(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder message = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
//                str=new String(str.getBytes(),"UTF-8");
                message.append("\n"+str+"\n");
            }
            reader.close();
            String result = message.toString();
            System.out.println(result);
            if (result.equals("")) {
                return null;
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        } finally {
            try {
                inputStream.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
