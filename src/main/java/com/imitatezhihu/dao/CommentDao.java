package com.imitatezhihu.dao;

import com.imitatezhihu.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentDao {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    //插入评论
    @Insert({"insert into", TABLE_NAME, "(", INSERT_FIELDS,
    ") values (#{userId}, #{content}, #{createdDate}, #{entityId}, #{entityType}, #{status})"})
    int addComment(Comment comment);

    //选取跟一个问题有关的所有评论
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            "where entity_id = #{entityId} and entity_type = #{entityType} order by created_date desc"})
    List<Comment> selectCommentByEntity(@Param("entityId") int entityId, @Param("entityType") int entityType);

    //得到跟一个问题有关的所有评论数量，相当于select+count，而不是像上条一样取出所有SELECT_FIELDS
    @Select({"select count(id) from ", TABLE_NAME, "where entity_id = #{entityId} and entity_type = #{entityType}"})
    int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);

    //改变一个评论的状态
    @Update({"update", TABLE_NAME, "set status = #{status} where id = #{id}"})
    int updateStatus(@Param("id") int id, @Param("status") int status);

    //根据Id选择Comment
    @Select({"select", SELECT_FIELDS, "from", TABLE_NAME, "where id = #{id}"})
    Comment selectById(@Param("id") int id);

    //特定user发出的评论总数
    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    int getUserCommentCount(int userId);

}
