package com.imitatezhihu.dao;

import com.imitatezhihu.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {
    String TABLE_NAME = "user";
    String INSERT_FIELDS = " name, password, salt, head_url ";
    String SELECT_FIELDS = " id, name, password, salt, head_url";

    //插入
    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{name},#{password},#{salt},#{headUrl})"})
    int addUser(User user);
    //通过Id选择
    @Select({"select",SELECT_FIELDS,"from",TABLE_NAME,"where id = #{id}"})
    User selectById(int id);
    //通过名字选择：主要供登录验证：验证是否存在此用户时使用
    @Select({"select", SELECT_FIELDS, "from", TABLE_NAME, "where name = #{name}"})
    User selectByName(String name);
}
