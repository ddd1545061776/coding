package com.chen.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chen.biz.pojo.*;
import com.chen.biz.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author danger
 * @date 2021/3/16
 */
@Controller
@Slf4j
public class PagesController {

    @Autowired
    private QuestionTypeService questionTypeService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private InputExampleService inputExampleService;
    @Autowired
    private OutputExampleService outputExampleService;
    @Autowired
    private QuestionStatusService questionStatusService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserPassService userPassService;
    @Autowired
    private CommentReplyService commentReplyService;
    @Autowired
    private SysUserService sysUserService;

    @RequestMapping({"/index", "/index.html"})
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        SysUser sysUser = sysUserService.selectUserByName(currentUserName);
        UserPass filter = new UserPass();
        filter.setUserId(sysUser.getUserId());
        QueryWrapper<UserPass> wrapper = new QueryWrapper<>(filter);
        List<UserPass> passes = userPassService.selectList(wrapper);
        List<Question> res = new ArrayList<>();
        for (UserPass pass : passes) {
            Question question = questionService.getById(pass.getQuestionId());
            res.add(question);
        }
        model.addAttribute("questions", res);
        return "index";
    }

    @RequestMapping({"/learncode", "/learncode.html"})
    public String learncode(Model model) {
        return "learncode";
    }

    @RequestMapping({"/index1", "/index1.html"})
    public String index1(Model model) {
        return "pages/index1";
    }
    @GetMapping({"/pages-login.html", "/pages-login"})
    public String toLogin() {
        return "user/pages-login";
    }

    @GetMapping("/pages-register")
    public String toRegister() {
        return "user/pages-register";
    }

    @GetMapping("/pages-recoverpw")
    public String toForgotPassword() {
        return "user/pages-recoverpw";
    }

    @GetMapping("/pages-profile")
    public String toPagesProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        UserInfo filter = new UserInfo();
        filter.setUsername(name);
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>(filter);
        userInfoService.selectOne(wrapper);
        return "user/pages-profile";
    }

    @GetMapping("/question-list")
    public String toQuestionList(@RequestParam(value = "type", required = false) String type, Model model) {
        QuestionStatus wrapper = new QuestionStatus();
        Long questionTypeId = null;
        if (StringUtils.hasLength(type)) {
            questionTypeId = questionTypeService.selectIdByTypeName(type);
            wrapper.setQuestionTypeId(questionTypeId);
        }

        List<QuestionStatus> questionStatuses = questionStatusService.selectList(new QueryWrapper<>(wrapper));
        model.addAttribute("questionList", questionStatuses);
        return "question/question-list";
    }

    @GetMapping("/question/{order}")
    public String toQuestionPage(@PathVariable("order") String order, Model model) {
        Long questionOrder = Long.parseLong(order);
        Question question = questionService.getQuestionByOrder(questionOrder);
        List<InputExample> inputExamples = inputExampleService.getInputExampleById(question.getQuestionId());
        List<OutputExample> outputExamples = outputExampleService.getOutputExampleById(question.getQuestionId());
        model.addAttribute("inputExamples", inputExamples);
        model.addAttribute("outputExamples", outputExamples);
        model.addAttribute("question", question);
        return "question/question";
    }

    @GetMapping("/question-comments/{order}")
    public String toQuestionComments(@PathVariable("order") String order, Model model) {

        Question question = questionService.getQuestionByOrder(Long.parseLong(order));

        Comment filter = new Comment();
        filter.setQuestionId(question.getQuestionId());
        QueryWrapper<Comment> wrapper = new QueryWrapper<>(filter);
        List<Comment> comments = commentService.selectList(wrapper);
        model.addAttribute("comments", comments);
        return "question/comments";
    }

    @GetMapping("/question-comments/{order}/add")
    public String toQuestionCommentsAdd(@PathVariable("order") String order, Model model) {
        model.addAttribute("order", order);
        return "question/comments-add";
    }

    @GetMapping("/reply")
    public String toReply(@RequestParam("id") String commentId, Model model) {
        Comment comment = commentService.getById(Long.parseLong(commentId));
        UserInfo userInfo = userInfoService.getUserInfoByUserId(comment.getUserId());
        CommentReply filter = new CommentReply();
        filter.setCommentId(Long.parseLong(commentId));
        QueryWrapper<CommentReply> wrapper = new QueryWrapper<>(filter);
        List<CommentReply> commentReplies = commentReplyService.selectList(wrapper);
        model.addAttribute("comment", comment);
        model.addAttribute("email", userInfo.getEmail());
        model.addAttribute("replies", commentReplies);
        return "question/comments-reply";
    }

    @GetMapping("/help")
    public String toHelp() {
        return "pages/help";
    }

    @GetMapping("/ranking")
    public String toRanking(Model model) {

        List<UserRanking> ranks = userInfoService.getUserRankingByPassRate();
        model.addAttribute("ranks", ranks);
        return "pages/ranking";
    }

    @GetMapping("/pages-lock")
    public String toLock() {
        return "user/pages-lock-screen";
    }

    @RequestMapping({"/h5", "/index2.html"})
    public String index2(Model model) {
        return "pages/H5";
    }

    @RequestMapping({"/css", "/css.html"})
    public String index3(Model model) {
        return "pages/css";
    }


    @RequestMapping({"/css3", "/css3.html"})
    public String index4(Model model) {
        return "pages/css3";
    }

    @RequestMapping({"/bootstrap3", "/bootstrap3.html"})
    public String index5(Model model) {
        return "pages/bootstrap3";
    }

    @RequestMapping({"/bootstrap4", "/bootstrap4.html"})
    public String index6(Model model) {
        return "pages/bootstrap4";
    }

    @RequestMapping({"/bootstrap5", "/bootstrap5.html"})
    public String index7(Model model) {
        return "pages/bootstrap5";
    }

    @RequestMapping({"/fontansom", "/fontansom.html"})
    public String index8(Model model) {
        return "pages/fontansom";
    }

    @RequestMapping({"/foundation5", "/foundation5.html"})
    public String index9(Model model) {
        return "pages/foundation5";
    }

    @RequestMapping({"/javascript", "/javascript.html"})
    public String index10(Model model) {
        return "pages/javaScript";
    }

    @RequestMapping({"/htmldom", "/htmldom.html"})
    public String index11(Model model) {
        return "pages/htmlDom";
    }

    @RequestMapping({"/jquery", "/jquery.html"})
    public String index12(Model model) {
        return "pages/jQuery";
    }

    @RequestMapping({"/angularjs", "/angularjs.html"})
    public String index13(Model model) {
        return "pages/angularJs";
    }

    @RequestMapping({"/vue", "/vue.html"})
    public String index14(Model model) {
        return "pages/vue";
    }

    @RequestMapping({"/vue3", "/vue3.html"})
    public String index15(Model model) {
        return "pages/vue3";
    }

    @RequestMapping({"/react", "/react.html"})
    public String index16(Model model) {
        return "pages/react";
    }

    @RequestMapping({"/eCharts", "/eCharts.html"})
    public String index17(Model model) {
        return "pages/eCharts";
    }

    @RequestMapping({"/python", "/python.html"})
    public String index18(Model model) {
        return "pages/python";
    }

    @RequestMapping({"/java", "/java.html"})
    public String index19(Model model) {
        return "pages/java";
    }

    @RequestMapping({"/c", "/c.html"})
    public String index20(Model model) {
        return "pages/c";
    }

    @RequestMapping({"/c++", "/c++.html"})
    public String index21(Model model) {
        return "pages/c++";
    }

    @RequestMapping({"python2", "python2.html"})
    public String index22(Model model) {
        return "pages/python2";
    }

    @RequestMapping({"sql", "sql.html"})
    public String index23(Model model) {
        return "pages/sql";
    }
}
