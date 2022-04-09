package com.chen.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.chen.biz.compiler.CmdStrings;
import com.chen.biz.pojo.*;
import com.chen.biz.service.*;
import com.chen.biz.utils.ExecutorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;
import java.util.UUID;

/**
 * @author danger
 * @date 2021/4/15
 */
@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Value("${compile.filepath}")//D:\final\coding-online\sourceFile\
    private String CompileFilePath;
    @Value("${compile.relativePath}")
    private String relativePath;

    @Value("${compile.dangerousKeyWords}")
    private String dangerousKeyWords;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionStatusService questionStatusService;

    @Autowired
    private JudgeResultService judgeResultService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserPassService userPassService;

    @Autowired
    private OutputExampleService outputExampleService;

    @Autowired
    private InputExampleService inputExampleService;

    @Override
    @Transactional
    public JudgeResult judge(JudgeTask task,String[] input) {
        // 用于存储运行结果
        JudgeResult result = new JudgeResult();
        // 设置运行结果的判题 id 和用户 id
        result.setJudgeTaskId(task.getJudgeTaskId());
        result.setUserId(task.getUserId());
        // 根据判题 id 创建源代码文件
        String path = CompileFilePath + task.getJudgeTaskId();
        File file = new File(path);
        file.mkdirs();
        try {
            createFile(task.getCompilerId(), path, task.getSource(),input);
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(8);
            result.setErrorMessage("创建文件失败！");
            // 出现错误时，删除文件
//windows            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
            // 将判题结果插入
            judgeResultService.insert(result);
            // 更新题目状态及个人状态
            questionStatusService.updateStatus(task.getQuestionId(), result.getStatus());
            userInfoService.updateStatus(task.getUserId(), result.getStatus());
            return result;
        }
        // 检查源码字符流中是否包含危险字符
        if (!verify(task.getSource())) {
            result.setStatus(7);
            result.setErrorMessage("使用了不安全的函数");
//windows            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_LINUX + path);
            // 将判题结果插入
            judgeResultService.insert(result);
            // 更新题目状态及个人状态
            questionStatusService.updateStatus(task.getQuestionId(), result.getStatus());
            userInfoService.updateStatus(task.getUserId(), result.getStatus());
            return result;
        }

        // 调用编译器对源文件进行编译
        String message = compile(task.getCompilerId(), path, task.getJudgeTaskId());
        log.info(message);
        if (message != null && task.getCompilerId() != 4) {
            result.setStatus(6);
            result.setErrorMessage("编译错误：\n" + message);
//windows            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_LINUX + path);
            // 将判题结果插入
            judgeResultService.insert(result);
            // 更新题目状态及个人状态
            questionStatusService.updateStatus(task.getQuestionId(), result.getStatus());
            userInfoService.updateStatus(task.getUserId(), result.getStatus());
            return result;
        }

        log.info("编译完成");
        // 生成可执行文件的执行命令
        String process = process(task.getCompilerId(), path);

        // 执行可执行文件
        try {
            parseToResult(process, result, task, input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 删除源文件
//windows        ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
        ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
        // 将判题结果插入
        judgeResultService.insert(result);
        // 更新题目状态及个人状态
        questionStatusService.updateStatus(task.getQuestionId(), result.getStatus());
        userInfoService.updateStatus(task.getUserId(), result.getStatus());

        // 更新通过题目表
        UserPass filter = new UserPass();
        filter.setUserId(task.getUserId());
        filter.setQuestionId(task.getQuestionId());
        QueryWrapper<UserPass> wrapper = new QueryWrapper<>(filter);
        List<UserPass> userPasses = userPassService.selectList(wrapper);
        if (userPasses.isEmpty()) {
            filter.setIsPassed(true);
            userPassService.insert(filter);
        } else {
            UpdateWrapper<UserPass> updateWrapper = new UpdateWrapper<>(filter);
            filter.setIsPassed(true);
            userPassService.updateByEntity(filter, updateWrapper);
        }
        return result;
    }


    private boolean verify(String source) {
        String[] keys = dangerousKeyWords.split(",");
        for (String key: keys) {
            if (source.contains(key)) {
                return false;
            }
        }
        return true;
    }
    //    @Override
//    public JudgeResult judge(JudgeTask task) {
//        JudgeResult result = new JudgeResult();
//        result.setJudgeTaskId(task.getJudgeTaskId());
//        String path = workspace + "/" + task.getJudgeTaskId();
//        File file = new File(path);
//        file.mkdirs();
//        try {
//            createFile(task.getCompilerId(), path, task.getSource());
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.setStatus(8);
//            ExecutorUtil.exec("rm -rf " + path);
//            return result;
//        }
//        //verify the key
//        if (!verify(task.getSource())) {
//            result.setStatus(9);
//            ExecutorUtil.exec("rm -rf " + path);
//            return result;
//        }
//        //compile the source
//        String message = compile(task.getCompilerId(), path);
//        if (message != null && task.getCompilerId() != 4) {
//            result.setStatus(7);
//            result.setErrorMessage(message);
//            ExecutorUtil.exec("rm -rf " + path);
//            return result;
//        }
//        //chmod -R 755 path
//        ExecutorUtil.exec("chmod -R 755 " + path);
//        //judge
//        String process = process(task.getCompilerId(), path);
//        String judge_data = judgeData + "/" + task.getQuestionId();
//        String cmd = "python2 " + judgeScript + " " + process + " " + judge_data + " " + path + " " + task.getTimeLimit() + " " + task.getMemoryLimit();
//        parseToResult(cmd, result);
//        ExecutorUtil.exec("rm -rf " + path);
//        return result;
//    }
//
    @Override
    public String compile(int compilerId, String path, Long judgeTaskId) {
        String cmd = "";
//        switch (compilerId) {
//            case 1:
//                cmd = "cmd /c gcc " + path + "\\main.c -o " + path + "\\main -Wall -lm -O2 -std=c99 --static -DONLINE_JUDGE";
//                break;
//            case 2:
//                cmd = "cmd /c g++ " + path + "\\main.cpp -O2 -Wall -lm --static -DONLINE_JUDGE -o " +path +"\\main";
//                break;
//            case 3:
//                cmd = "cmd /c set CLASSPATH="+ path +" && javac "+ path + "\\Main.java";
//                break;
//            case 4:
//                cmd = "cmd /c python -m py_compile "+ path +"\\main.py";
//                break;
//        }

        switch (compilerId) {
            case 1:
                cmd = "gcc " + path + "/main.c -o " + path + "/main -Wall -lm -O2 -std=c99 --static -DONLINE_JUDGE";
                break;
            case 2:
                cmd = "g++ " + path + "/main.cpp -O2 -Wall -lm --static -DONLINE_JUDGE -o " +path +"/main";
                break;
            case 3:
                cmd = "javac "+ path + "/Main.java";
                break;
            case 4:
                cmd = "python -m py_compile "+ path +"/main.py";
                break;
        }

//        String cmdCompile = "cmd.exe && E: && cd " + relativePath + " && " + cmd;
//        String cmdCompile = "cmd /c set CLASSPATH="+ path +" && javac "+ path + "\\Main.java";

        return ExecutorUtil.exec(cmd).getError();
    }

    @Override
    public JudgeResult  runCode(String source,String compilerId) {
        JudgeResult result=new JudgeResult();
        // 根据判题 id 创建源代码文件
        UUID uuid =UUID.randomUUID();
        String str = uuid.toString().replaceAll("-","");
        String path = CompileFilePath+str;
        File file = new File(path);
        file.mkdirs();
        try {
            createFileWithRunTest(Integer.parseInt(compilerId),path,source);
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(8);
            result.setErrorMessage("创建文件失败！");
            // 出现错误时，删除文件
//windows            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
            return result;
        }
        // 调用编译器对源文件进行编译
        String message = compile(Integer.parseInt(compilerId), path,null);
        log.info(message);
        if (message != null && Integer.parseInt(compilerId) != 4) {
            result.setStatus(6);
            result.setErrorMessage("编译错误：\n" + message);
//windows            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_WINDOWS + path);
            ExecutorUtil.exec(CmdStrings.REMOVE_FILE_LINUX + path);
            return result;
        }

        log.info("编译完成");
        // 生成可执行文件的执行命令
        String process = process(Integer.parseInt(compilerId), path);

        // 执行可执行文件
        parseToResult1(process, result);
        return  result;
    }
//

    //
    private String process(int compileId, String path) {
/*        switch (compileId) {
            case 1:
                return "cmd /c " + path + "\\main";
            case 2:
                return "cmd /c " + path + "\\main";
            case 3:
                return "cmd /c set CLASSPATH="+ path + " && java Main";
            case 4:
                return "python " + path + "\\main.py";
        }*/

        switch (compileId) {
            case 1:
                return path + "/main";
            case 2:
                return path + "/main";
            case 3:
                return "java -cp "+ path +" Main";
            case 4:
                return "python " + path + "/main.py";
        }
        return null;
    }
    //
    private void parseToResult1(String cmd, JudgeResult result) {

        Runtime runtime = Runtime.getRuntime();
        // 记录开始结束时间
        long startMemory = runtime.freeMemory();
        long startTime = System.currentTimeMillis();
        // 执行可执行文件
        //获取问题id
        ExecMessage exec = ExecutorUtil.execWithNoInput(cmd);
        long endMemory = runtime.freeMemory();
        long endTime = System.currentTimeMillis();

        if (exec.getError() != null) {
            // 运行时错误
            result.setStatus(5);
            result.setErrorMessage("运行时错误：\n"+exec.getError());

            log.error("=====error====" + result.getJudgeTaskId() + ":" + exec.getError());
        } else {
            int timeUsed = (int) (endTime - startTime);
            int memoryUsed = (int) (endMemory - startMemory);
            if(memoryUsed < 0) {
                memoryUsed = 0;
            }
            result.setTimeUsed(timeUsed);
            result.setMemoryUsed(memoryUsed);
            result.setStatus(0);
            result.setErrorMessage(exec.getStdout());
        }
    }
    private void parseToResult(String cmd, JudgeResult result, JudgeTask judgeTask,String[] input) throws IOException {

        Runtime runtime = Runtime.getRuntime();
        // 记录开始结束时间
        long startMemory = runtime.freeMemory();
        long startTime = System.currentTimeMillis();
        // 执行可执行文件
        //获取问题id
        Long questionId = judgeTask.getQuestionId();
        //根据问题id获取输出用例
        List<InputExample> inputExamples = inputExampleService.getInputExampleById(questionId);
        List<OutputExample> outputExamples = outputExampleService.getOutputExampleById(questionId);

        a:for (int i = 0; i < inputExamples.size(); i++) {
            for (int j = 0; j < outputExamples.size(); j++) {
                String inputExampleString = inputExamples.get(i).getInputExample();
                ExecMessage exec=null;
                if (judgeTask.getCompilerId()==3){
                    exec  = ExecutorUtil.execJavaWithInput(cmd, inputExampleString);
                } else if (judgeTask.getCompilerId()==4) {
                    exec  = ExecutorUtil.execPythonWithInput(cmd, inputExampleString,input);
                }

                if (exec.getError() != null) {
                    // 运行时错误
                    result.setStatus(5);
                    result.setErrorMessage("运行时错误：\n"+exec.getError());
                    log.error("=====error====" + result.getJudgeTaskId() + ":" + exec.getError());
                    return;
                }
                String outexm= outputExamples.get(i).getOutputExample().replaceAll("\\s*","");

                String res=  exec.getStdout().replaceAll("\\s*","");
                if (!outexm.equals(res)) {
                    result.setStatus(1);
                    result.setErrorMessage("用例输入:"+inputExampleString.replaceAll("#",",")+"\t你的答案是"+res+"\t,预期答案是"+outexm);
                    return;
                }
                continue a;
            }
        }
        long endMemory = runtime.freeMemory();
        long endTime = System.currentTimeMillis();

        int timeUsed = (int) (endTime - startTime);
        int memoryUsed = (int) (endMemory - startMemory);
        if(memoryUsed < 0) {
            memoryUsed = 0;
        }
        result.setTimeUsed(timeUsed);
        result.setMemoryUsed(memoryUsed);
        if (timeUsed > judgeTask.getTimeLimit()*1000) {
            result.setStatus(4);
            result.setErrorMessage("超出了题目的时间限制!");
            return;
        }
        if (memoryUsed > judgeTask.getMemoryLimit()*1024) {
            result.setStatus(3);
            result.setErrorMessage("超出了题目的内存限制!");
            return;
        }

        result.setStatus(0);
        result.setErrorMessage("正确!");
    }
//    if (exec.getError() != null) {
//        // 运行时错误
//        result.setStatus(5);
//        result.setErrorMessage(exec.getError());
//        log.error("=====error====" + result.getJudgeTaskId() + ":" + exec.getError());
//    } else {
////            Stdout out = JSON.parseObject(exec.getStdout(), Stdout.class);
////            log.info("=====stdout====" + out);
////            result.setStatus(out.getStatus());
////            result.setTimeUsed(out.getMaxTime().intValue());
////            result.setMemoryUsed(out.getMaxMemory().intValue());
//
//    }

    private static void createFile(int compilerId, String path, String resource,String[] input) throws Exception {
        String filename = "";
        switch (compilerId) {
            case 1:
                filename = CmdStrings.FILE_NAME_GCC;
                break;
            case 2:
                filename = CmdStrings.FILE_NAME_GPP;
                break;
            case 3:
                filename = CmdStrings.FILE_NAME_JAVA;
                break;
            case 4:
                filename = CmdStrings.FILE_NAME_PYTHON;
                break;
        }
        File file = new File(path + "/" + filename);
        file.createNewFile();
        OutputStream output = new FileOutputStream(file);
        PrintWriter writer = new PrintWriter(output);
        String fileResource="";
        switch (compilerId) {
            case 1:
                filename = CmdStrings.FILE_NAME_GCC;
                break;
            case 2:
                filename = CmdStrings.FILE_NAME_GPP;
                break;
            case 3:
                fileResource = createFileByJava(resource);
                break;
            case 4:
                fileResource = createFileByPython(resource,input);
                break;
        }
        writer.print(fileResource);
        writer.close();
        output.close();
    }

    private static  String createFileByPython(String resource,String[] input) {
        StringBuffer dd = new StringBuffer();
        dd.append("#!/usr/bin/python\n");
        dd.append("import sys\n");
        dd.append("from typing import List\n");
        dd.append("class ListNode(object):\n" +
                "    def __init__(self, val=0, next=None):\n" +
                "          self.val = val\n" +
                "          self.next = next\n");
        int main = resource.indexOf("main");
        int i = resource.indexOf(")", main);
        String substring =resource.substring(main, i);
        int k=0;
        for (int j = 1; j <substring.length() ; j++) {
            char c =   substring.charAt(j);
            if (c==',') {
                k++;
            }
        }
        if (k==0) {
            return resource;
        } else {
            k=k*2-1;
//            int print = resource.indexOf("print(");
//            int i1 = resource.indexOf(")", print);
//            String substring1 = resource.substring(0,print);
            dd.append(resource);
            if (input.length==2){
                dd.append("\n    def typeof1(self,variate,v,variate1,v1):\n" +
                        "        type=None           \n" +
                        "        if (v=='1'):\n" +
                        "           variate = int(variate)\n" +
                        "        if (v1=='1'):\n" +
                        "           variate1 = int(variate1)           \n" +
                        "        if (v=='2'):\n" +
                        "            if(len(variate)==2):\n" +
                        "                return(ss.main(variate))\n" +
                        "            else:           \n" +
                        "                variate = variate.replace(\"[\",\"\").replace(\"]\",\"\")\n" +
                        "                variate = variate.split(\",\")\n" +
                        "                variate = list(map(int,variate))      \n" +
                        "        if (v1=='2'):\n" +
                        "            if(len(variate1)==2):\n" +
                        "                return(ss.main(variate))\n" +
                        "            else:          \n" +
                        "                variate1 = variate1.replace(\"[\",\"\").replace(\"]\",\"\")\n" +
                        "                variate1 = variate1.split(\",\")\n" +
                        "                variate1 = list(map(int,variate1))    \n" +
                        "        if (v=='3'):\n" +
                        "            type='3'\n" +
                        "        if (v=='4'):\n" +
                        "            if(len(variate)==2):\n" +
                        "                return(ss.main(variate))\n" +
                        "            else:          \n" +
                        "                variate = variate.split(\",\")\n" +
                        "                variate = list(map(int,variate))    \n" +
                        "                variate = set(variate) \n" +
                        "        if (v1=='4'):\n" +
                        "            if(len(variate1)==2):\n" +
                        "                return(ss.main(variate))\n" +
                        "            else:           \n" +
                        "                variate1 = variate1.split(\",\")\n" +
                        "                variate1 = list(map(int,variate1))    \n" +
                        "                variate1 = set(variate1) \n" +
                        "        if (v=='5'):\n" +
                        "            type = \"dict\"   \n" +
                        "        if (v=='6'):\n" +
                        "            variate = float(variate)\n" +
                        "        if (v1=='6'):\n" +
                        "            variate1 = float(variate1)\n" +
                        "            \n" +
                        "        if (v=='7'):\n" +
                        "            if(len(variate)==2):\n" +
                        "                variate=None\n" +
                        "            else:          \n" +
                        "                variate = variate.replace(\"[\",\"\").replace(\"]\",\"\")\n" +
                        "                variate = variate.split(\",\")\n" +
                        "                variate = list(map(int,variate))  \n" +
                        "                variate = list2link(variate)            \n" +
                        "        if (v1=='7'):\n" +
                        "            if(len(variate1)==2):\n" +
                        "                variate1=None\n" +
                        "            else:          \n" +
                        "                variate1 = variate1.replace(\"[\",\"\").replace(\"]\",\"\")\n" +
                        "                variate1 = variate1.split(\",\")\n" +
                        "                variate1 = list(map(int,variate1))  \n" +
                        "                variate1 = list2link(variate1)\n" +
                        "        if (isinstance(variate,ListNode) or isinstance(variate1,ListNode) or variate1 is None or variate is None):\n" +
                        "            variate = print_linked_list(ss.main(variate,variate1))\n" +
                        "            variate = list(reversed(variate));\n" +
                        "        else:\n" +
                        "            variate = ss.main(variate,variate1)   \n" +
                        "\n" +
                        "        return variate ");
            }else{
                dd.append("\n    def typeof(self,variate,v):\n" +
                        "        type=None\n" +
                        "        if (v=='0'):\n" +
                        "            return ss.main(variate)\n" +
                        "        elif (v=='1'):\n" +
                        "           variate = int(variate)\n" +
                        "           return ss.main(variate)\n" +
                        "        elif (v=='2'):\n" +
                        "            if(len(variate)==2):\n" +
                        "                return(ss.main(variate))\n" +
                        "            else:\n" +
                        "                variate = variate.replace(\"[\",\"\").replace(\"]\",\"\");\n" +
                        "                variate = variate.split(\",\")\n" +
                        "                variate = list(map(int,variate))      \n" +
                        "                if (ss.main(variate)==None):\n" +
                        "                    return(variate)\n" +
                        "                else:\n" +
                        "                    return(ss.main(variate))\n" +
                        "        elif (v=='3'):\n" +
                        "            return None\n" +
                        "        elif (v=='4'):\n" +
                        "            if(len(variate)==2):\n" +
                        "                return(ss.main(variate))\n" +
                        "            else:        \n" +
                        "                variate = variate.replace(\"[\",\"\").replace(\"]\",\"\");\n" +
                        "                variate = variate.split(\",\")\n" +
                        "                variate = list(map(int,variate))    \n" +
                        "                variate = set(variate) \n" +
                        "                return arr\n" +
                        "        elif (v=='5'):\n" +
                        "            type = \"dict\"   \n" +
                        "        elif (v=='6'):\n" +
                        "            variate = float(variate)\n" +
                        "            return ss.main(variate)\n" +
                        "        elif (v=='7'):\n" +
                        "            if(len(variate)==2):\n" +
                        "                return(ss.main(variate))\n" +
                        "            else:   \n" +
                        "                variate = variate.replace(\"[\",\"\").replace(\"]\",\"\");\n" +
                        "                variate = variate.split(\",\")\n" +
                        "                variate = list(map(int,variate))  \n" +
                        "                variate = list2link(variate)            \n" +
                        "                if __name__ == \"__main__\":\n" +
                        "                    variate = print_linked_list(ss.main(variate))\n" +
                        "                return variate           \n" +
                        "        return type");
            }
            dd.append("\ndef list2link(list_):\n" +
                    "    head = ListNode(list_[0])\n" +
                    "    p = head\n" +
                    "    for i in range(1, len(list_)):\n" +
                    "        p.next = ListNode(list_[i])\n" +
                    "        p = p.next\n" +
                    "    return head\n" +
                    "  \n" +
                    "def print_linked_list(listNode):\n" +
                    "    stack=[]\n" +
                    "    if listNode == None:\n" +
                    "        return []\n" +
                    "    while listNode.next != None:\n" +
                    "        stack.insert(0,listNode.val)\n" +
                    "        listNode=listNode.next\n" +
                    "    stack.insert(0,listNode.val)\n" +
                    "    return stack\n" +
                    "ss =  Main() ");
            if (input.length==2){
                dd.append("\nprint(ss.typeof1(");
            } else {
                dd.append("\nprint(ss.typeof(");
            }
            for (int j = 1; j <=k; j++) {
                if (j%2==0){
                    dd.append("sys.argv["+j+"],");
                    continue;
                }else if(j<k){
                    dd.append("sys.argv["+j+"],");
                    continue;
                }else{
                    dd.append("sys.argv["+j+"]");
                    continue;
                }
            }
            k=k+1;
            if (k==4){
                dd.append(",sys.argv["+k+"]");
            }else{
                dd.append(",sys.argv["+k+"]");
            }
            dd.append("))");
        }
        return  dd.toString();
    }

    private  static  String createFileByJava(String resource){
        int i = resource.indexOf("{");
        int  i2=resource.indexOf("{",i+1);
        String substring = resource.substring(i+1,i2+1);
        System.out.println(substring);
        int i1 = resource.lastIndexOf("}");
        String substring1 = resource.substring(i2+1,i1);
        System.out.println(substring1);
        StringBuilder s=new StringBuilder();
        String substring2 = resource.substring(0,i+1);
        System.out.println(substring2);
        s.append("import java.lang.reflect.Method;\n import java.util.*;\n import java.lang.reflect.InvocationTargetException;\n");
        s.append(substring2);
        s.append(" static class ListNode{\n" +
                "        int val;\n" +
                "        ListNode next;\n" +
                "        ListNode() {}\n" +
                "        ListNode(int val) { this.val = val; }\n" +
                "        ListNode(int val, ListNode next) { this.val = val; this.next = next; }\n" +
                "    }");
        s.append(substring);
        s.append("    Object[] a=new Object[args.length];\n" +
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
                "                   ! m.getName().equals(\"listNodeToArray\")&&\n" +
                "                   ! m.getName().equals(\"listNodeSize\")&&\n" +
                "                   ! m.getName().equals(\"arrayToListNode\")&&\n" +
                "                    !m.getName().equals(\"notifyAll\")\n" +
                "            ){\n" +
                "                Class<?> returnType = m.getReturnType();\n" +
                "                Class<?>[] parameterTypes = m.getParameterTypes();\n" +
                "                for (int i = 0; i <parameterTypes.length ; i++) {\n" +
                "                    String name = parameterTypes[i].getSimpleName();\n" +
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
                "                    if(name.equals(\"int[]\")){\n" +
                "                        String[] split= args[i].replaceAll(\"\\\\[\", \"\").replaceAll(\"]\", \"\").split(\",\");\n" +
                "                        a[i]= Arrays.stream(split).mapToInt(Integer::parseInt).toArray();\n" +
                "                        continue;\n" +
                "                    }\n" +
                "                    if(name.equals(\"String[]\")){\n" +
                "                        a[i]=args[i].replaceAll(\"\\\\[\", \"\").replaceAll(\"]\", \"\").split(\",\");\n" +
                "                        continue;\n" +
                "                    }\n" +
                "         if(name.equals(\"ListNode\")){\n" +
                "                        int[] ints;\n" +
                "                        if (args[i].equals(\"[]\")){\n" +
                "                            ints=new int[0];\n" +
                "                        }else{\n" +
                "                        String[] split = args[i].replaceAll(\"\\\\[\", \"\").replaceAll(\"]\", \"\").split(\",\");\n" +
                "                             ints = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();\n" +
                "                        }\n" +
                "                        ListNode listNode = arrayToListNode(ints);\n" +
                "                        a[i]=listNode;\n" +
                "                        continue;\n" +
                "                    }                 \n" +
                "                    a[i]=args[i];\n" +
                "}\n"+
                "                if (args.length==1){\n" +
                "                    Object invoke = null;\n" +
                "                    try {\n" +
                "                        invoke = m.invoke(Main.class, a[0]);\n" +
                "                    } catch (IllegalAccessException e) {\n" +
                "                        e.printStackTrace();\n" +
                "                    } catch (InvocationTargetException e) {\n" +
                "                        e.printStackTrace();\n" +
                "                    }\n" +
                "                    if (returnType.getSimpleName().equals(\"int[]\")){\n" +
                "                        int[] vv=(int[])invoke;\n" +
                "                        System.out.println(Arrays.toString(vv));\n" +
                "                            return;\n"+
                "                    }\n" +
                "                    if (returnType.getSimpleName().equals(\"String[]\")){\n" +
                "                        String[] vv=(String[])invoke;\n" +
                "                        System.out.println(Arrays.toString(vv));\n" +
                "                            return;\n"+
                "                    }\n" + " if (returnType.getSimpleName().equals(\"ListNode\")){\n" +
                "                       int listNode[]  =listNodeToArray((ListNode)invoke);\n" +
                "                        System.out.println(Arrays.toString(listNode));\n" +
                "                            return;\n"+
                "                    }\n"+
                "                       System.out.println(invoke);\n" +
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
                "                    if (returnType.getSimpleName().equals(\"int[]\")){\n" +
                "                        int[] vv=(int[])invoke;\n" +
                "                        System.out.println(Arrays.toString(vv));\n" +
                "                            return;\n"+
                "                    }\n" +
                "                    if (returnType.getSimpleName().equals(\"String[]\")){\n" +
                "                        String[] vv=(String[])invoke;\n" +
                "                        System.out.println(Arrays.toString(vv));\n" +
                "                            return;\n"+
                "                    }\n" + " if (returnType.getSimpleName().equals(\"ListNode\")){\n" +
                "                        int listNode[]  =listNodeToArray((ListNode)invoke);\n" +
                "                        System.out.println(Arrays.toString(listNode));\n" +
                "                            return;\n"+
                "                    }\n"+
                "                       System.out.println(invoke);\n" +
                "            }\n" +
                "            }\n" +
                "        }");
        s.append(substring1);
        s.append("public static int[] listNodeToArray(ListNode l) {\n" +
                "        int size = listNodeSize(l);\n" +
                "        int[] ints = new int[size];\n" +
                "        int index = 0;\n" +
                "        while (l != null) {\n" +
                "            ints[index] = l.val;\n" +
                "            l = l.next;\n" +
                "            index++;\n" +
                "        }\n" +
                "        return ints;\n" +
                "    }\n" +
                "    //获取列表长度\n" +
                "    public static int listNodeSize(ListNode l) {\n" +
                "        int size = 0;\n" +
                "        while (l != null) {\n" +
                "            size++;\n" +
                "            l = l.next;\n" +
                "        }\n" +
                "        return size;\n" +
                "    }\n" +
                "    public static ListNode arrayToListNode(int[] s) {\n" +
                "        if (s.length==0){\n" +
                "        return null;\n" +
                "    }\n" +
                "  ListNode root = new ListNode(s[0]);\n" +
                "        ListNode other = root;\n" +
                "        for (int i = 1; i < s.length; i++) {\n" +
                "            ListNode temp = new ListNode(s[i]);\n" +
                "            other.next = temp;\n" +
                "            other = temp;\n" +
                "        }\n" +
                "        return root;\n" +
                "    }\n");
        s.append("}\n");
        return  s.toString();
    }

    private static void createFileWithRunTest(int compilerId, String path, String resource) throws Exception {
        String filename = "";
        switch (compilerId) {
            case 1:
                filename = CmdStrings.FILE_NAME_GCC;
                break;
            case 2:
                filename = CmdStrings.FILE_NAME_GPP;
                break;
            case 3:
                filename = CmdStrings.FILE_NAME_JAVA;
                break;
            case 4:
                filename = CmdStrings.FILE_NAME_PYTHON;
                break;
        }
        File file = new File(path + "/" + filename);
        file.createNewFile();
        OutputStream output = new FileOutputStream(file);
        PrintWriter writer = new PrintWriter(output);
        writer.write(resource);
        writer.close();
        output.close();
    }
}
