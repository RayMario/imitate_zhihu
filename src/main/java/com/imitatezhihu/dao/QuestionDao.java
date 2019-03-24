package com.imitatezhihu.dao;

import com.imitatezhihu.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionDao {
    String TABLE_NAME = "question";
    String INSERT_FIELDS = " title, content, created_date, user_id, comment_count";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    //添加问题
    @Insert({"insert into", TABLE_NAME, "(", INSERT_FIELDS,
            ") values(#{title}, #{content}, #{createdDate}, #{userId}, #{commentCount})"})
    int addQuestion(Question question);

    //获取首页显示用的10条最新评论
    List<Question> selectLatestQuestion(@Param("userId") int userId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    //通过id选择一条question
    @Select({"select", SELECT_FIELDS, "from", TABLE_NAME, "where id = #{id}"})
    Question selectById(int id);

    //为一个question更新其评论数量
    @Update({"update", TABLE_NAME, "set comment_count = #{commentCount} where id = #{id}"})
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);
}
