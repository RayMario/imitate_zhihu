package com.imitatezhihu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveService implements InitializingBean {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(SensitiveService.class);
    //定义本Bean初始化过程，加载敏感词文本进入Bean当中
    @Override
    public void afterPropertiesSet() throws Exception {
        try{
            //线程读取，读取为输入流，读入缓存
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "SensitiveWords.txt");
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            //缓存当中按行读取
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine())!= null){
                //trim将每一行的空格处理掉，
                addWord(lineTxt.trim());
            }
            //关闭reader而不是is
            reader.close();
        }catch (Exception e){
            logger.error("读取敏感词文件失败",e.getMessage());
        }
    }


    //字典树架构
    private class TrieNode{
        private boolean end = false;

        /*由于不是二叉树，因此采用Hashmap构建节点之间的连接
        表示当前节点下所有的子节点
        比如ab，ac，ad，当前节点为a，那么map当中的key存放b，c，d
        对应以b，c，d开头的TrieNode对象。
        根本不用存储当前节点所代表的值，因为判断的是“有没有”，
        一直有Map传递就是有，找不到Map传递就是没有。具体的文本要么复制要么替换/删除，不需要用到树当中的值。
         */
        private Map<Character,TrieNode> subNodes = new HashMap<Character,TrieNode>();


        public void addSubNode(Character key,TrieNode node){
            subNodes.put(key,node);
        }

        TrieNode getSubNode(Character key){
            return subNodes.get(key);
        }

        boolean isKeyWordEnd(){
            return end;
        }

        void setKeyWordEnd(boolean end){
            this.end = end;
        }
    }
    //在敏感词类里面初始化一个字典树的根节点。
    private TrieNode rootNode = new TrieNode();

    //去掉词间的非中文，英文，数字符号再进行判断：排除赌%博这种迷惑性写法
    private boolean isSymbol(char c){
        int ic = (int)c;
        //isLetterOrDigit判断是否为字母与数字，0x2E80-0x9FFF为东亚字符，用AscII码应当先转换为int
        return !Character.isLetterOrDigit(c) && (c<0x2E80 || ic>0x9FFF);
    }
    //添加敏感词进树
    private void addWord(String lineText){
        TrieNode tempNode = rootNode;
        for(int i = 0; i<lineText.length();i++){
            Character c = lineText.charAt(i);
            //添加字符串进树的时候就要删除掉Symbol类的字符：
            if(isSymbol(c)){
                continue;
            }
            //判断当前节点有没有子节点,如果有对应的子节点，直接赋给node
            TrieNode node = tempNode.getSubNode(c);
            //如果没有，新建一个节点
            if(node == null){
                node = new TrieNode();
                tempNode.addSubNode(c,node);
            }
            //当前节点指向下一个节点
            tempNode = node;

            //在词组的末尾加上结束标识
            if(i==lineText.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    //三指针过滤问题文本
    public String filter(String text){
        if(StringUtils.isEmpty(text)){
            return text;
        }
        StringBuilder result = new StringBuilder();
        String replacement = "***";
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        while (position<text.length()){
            char c = text.charAt(position);
            //如果是Symbol而不是字母/数字/字
            if(isSymbol(c)){
                /*如果树是在根节点，意味着还未开始一个敏感词
                要在前面已经出现了敏感词的一部分的时候再舍去
                单独出现一个字符应当保留并加到result当中去。
                */
                if(tempNode == rootNode){
                    result.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }
            tempNode = tempNode.getSubNode(c);

            //没有敏感词
            if(tempNode == null){
                result.append(text.charAt(begin));
                position = begin+1;
                begin = position;
                tempNode = rootNode;
            }else if(tempNode.isKeyWordEnd()){
                //发现敏感词，下一行注释掉就实现删除。
                result.append(replacement);
                //直接在探针的位置+1，而不是在begin+1.
                position = position+1;
                begin = position;
                tempNode = rootNode;
            }else{
                //有一部分，但是没到树中的结尾点，探针继续探测；
                ++position;
            }
        }
        //最后从begin开始将尾巴加进去，因为while判定的是position
        result.append(text.substring(begin));
        return result.toString();
    }

    //测试脚本
    public static void main(String[] args){
        SensitiveService sensitiveService = new SensitiveService();
        sensitiveService.addWord("色情");
        sensitiveService.addWord("赌博");
        System.out.println(sensitiveService.filter("你赌--博吗"));
    }
}
