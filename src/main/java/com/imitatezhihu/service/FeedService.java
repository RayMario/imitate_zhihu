package com.imitatezhihu.service;

import com.imitatezhihu.dao.FeedDao;
import com.imitatezhihu.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {
    @Autowired
    FeedDao feedDao;

    //选择发布人在List<Integer> userIds当中的Feed对象，以List<Feed>返回
    public List<Feed> selectUserFeeds(int maxId,List<Integer> userIds, int count){
        return feedDao.selectUserFeeds(maxId,userIds,count);
    }

    public int addFeed(Feed feed){
        return feedDao.addFeed(feed);
    }

    public Feed selectById(int id){
        return feedDao.selectById(id);
    }
}
