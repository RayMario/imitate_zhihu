package com.imitatezhihu.service;

import com.imitatezhihu.dao.QuestionDao;
import com.imitatezhihu.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class QuestionService {
    @Autowired
    QuestionDao questionDao;
    @Autowired
    SensitiveService sensitiveService;

    //获取首页显示的题目
    public List <Question> getLatestQuestion(int userId,int offset,int limit){
        return questionDao.selectLatestQuestion(userId,offset,limit);
    }

    //增加题目
    public int addQuestion(Question question){
        //标签过滤
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        //敏感词过滤——使用字典树方法：
        question.setContent(sensitiveService.filter(question.getContent()));
        question.setTitle(sensitiveService.filter(question.getTitle()));
        /*这里是利用dao层addQuestion的返回的int型来判断是否正确INSERT到数据库
         （实际上是数据库的返回值，返回大于0代表成功）*/
        return questionDao.addQuestion(question)>0? question.getId():0;
    }

    //获取指定id的题目
    public Question selectById(int id){
        return questionDao.selectById(id);
    }

    //为指定id的题目更新其评论数量
    public int updateCommentCount(int id, int count){
        return questionDao.updateCommentCount(id, count);
    }
}
