package com.imitatezhihu.service;

import com.imitatezhihu.dao.CommentDao;
import com.imitatezhihu.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentDao commentDao;

    @Autowired
    SensitiveService sensitiveService;


    //查找评论
    public List<Comment> getCommentsByEntity(int entityId,int entityType){
        return commentDao.selectCommentByEntity(entityId,entityType);
    }

    //添加评论，需要使用标签过滤与敏感词过滤再入库
    public int addComment(Comment comment){
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveService.filter(comment.getContent()));
        return commentDao.addComment(comment) >0 ? comment.getId() : 0;
    }


    public int getCommentCount(int entityId, int entityType){
        return commentDao.getCommentCount(entityId,entityType);
    }

    public int getUserCommentCount(int userId) {
        return commentDao.getUserCommentCount(userId);
    }

    public boolean deleteComment(int commentId){
        return commentDao.updateStatus(commentId,1) > 0 ;
    }

    public Comment getCommentById(int id){
        return commentDao.selectById(id);
    }
}
