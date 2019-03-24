package com.imitatezhihu.dao;

import com.imitatezhihu.model.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketDao {
    String TABLE_NAME = " login_ticket";
    String INSERT_FIELDS = " user_id, expired, status, ticket";
    String SELECT_FIELDS = " id," + INSERT_FIELDS;

    //插入
    @Insert({"insert into", TABLE_NAME, "(", INSERT_FIELDS,
            ") values( #{userId},#{expired},#{status},#{ticket})"})
    int addTicket(LoginTicket loginTicket);

    //直接通过ticket字段本身验证是否存在这个ticket
    @Select({"select", SELECT_FIELDS, "from", TABLE_NAME, "where ticket = #{ticket}"})
    LoginTicket selectByTicket(String ticket);

    //更新ticket状态：供登出时ticket无效化使用
    @Update({"update", TABLE_NAME, "set status = #{status} where ticket = #{ticket}"})
    void updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
