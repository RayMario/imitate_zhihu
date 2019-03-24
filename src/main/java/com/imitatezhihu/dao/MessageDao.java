package com.imitatezhihu.dao;

import com.imitatezhihu.model.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageDao {
    String TABLE_NAME = " message ";
    String INSERT_FIELDS = " from_id, to_id, content, has_read, conversation_id, created_date";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    //插入消息
    @Insert({"insert into", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{fromId}, #{toId}, #{content}, #{hasRead}, #{conversationId}, #{createdDate})"})
    int addMessage(Message message);

    //选取跟一个人的所有通话记录：通过conversationId选择
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            "where conversation_id = #{conversationId} order by created_date desc limit #{offset},#{limit}"})
    List<Message> getConversationDetail(@Param("conversationId") String conversationId,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    /*获取与所有人的通信记录概要：
    *要选择与当前用户相关的，
    *需要降维，还要使得降维之后选中的那个是id最大的
    * 最后还需要对降维后的东西整体按时间排序。
    *select * from (select * from wenda.message where id = any(select max(id) from wenda.message group by conversation_id))tt order by created_date desc;
    */
    @Select({"select * from ( select * from ",TABLE_NAME,
    "where (from_id=#{userId} or to_id=#{userId}) and id = any(select max(id) from",TABLE_NAME,
            "group by conversation_id))tt order by created_date desc limit #{offset}, #{limit}"})
    List<Message> getConversationList(@Param("userId") int userId,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    //获取未读记录，前端要在我的私信页面展示我没读过的，因此其中toId应当为我的userId，而不是对面的
    @Select({"select count(id) from ",TABLE_NAME," " +
            "where has_read = 0 and to_id = #{userId} and conversation_id = #{conversationId}"})
    int getConversationUnreadCount(@Param("userId") int userId,
                                   @Param("conversationId") String conversationId);

    //更新unread记录
    @Update({"update", TABLE_NAME, "set has_read = #{hasRead} where from_id = #{fromId} and to_id = #{userId}"})
    void updateHasRead(@Param("fromId") int fromId, @Param("userId") int userId,@Param("hasRead") int hasRead);

    //给一条conversationId信息，以及本地用户id，返回其发件人。
    @Select({"select from_id from", TABLE_NAME, "where conversation_id = #{conversationId} and to_id = #{userId} group by to_id"})
    int getPosterId(@Param("userId") int userId,
                    @Param("conversationId") String conversationId);

}
