<#include "header.ftl">
<#--<script type="text/javascript" src="jquery.js"></script>-->
<script src="http://code.jquery.com/jquery-latest.js"></script>
<#setting number_format="#">；
<link rel="stylesheet" href="../styles/index.css">
<link rel="stylesheet" href="../styles/detail.css">
    <div class="zg-wrap zu-main clearfix " role="main">
        <div class="zu-main-content">
            <div class="zu-main-content-inner">
                <div class="zg-section" id="zh-home-list-title">
                    <i class="zg-icon zg-icon-feedlist"></i>最新动态
                    <input type="hidden" id="is-topstory">
                    <span class="zg-right zm-noti-cleaner-setting" style="list-style:none">
                        <a href="https://nowcoder.com/settings/filter" class="zg-link-gray-normal">
                            <i class="zg-icon zg-icon-settings"></i>设置</a></span>
                </div>
                <div class="zu-main-feed-con navigable js-home-list" data-feedtype="topstory" id="zh-question-list" data-widget="navigable" data-navigable-options="{&quot;items&quot;:&quot;&gt; .zh-general-list .feed-content&quot;,&quot;offsetTop&quot;:-82}">
                    <a href="javascript:;" class="zu-main-feed-fresh-button" id="zh-main-feed-fresh-button" style="display:none"></a>
                    <div id="js-home-feed-list" class="zh-general-list topstory clearfix " data-init="{&quot;params&quot;: {}, &quot;nodename&quot;: &quot;TopStory2FeedList&quot;}" data-delayed="true" data-za-module="TopStoryFeedList">

                        <#list vos as vo>
                        <div class="feed-item folding feed-item-hook feed-item-2
                        " feed-item-a="" data-type="a" id="feed-2" data-za-module="FeedItem" data-za-index="">
                            <meta itemprop="ZReactor" data-id="389034" data-meta="{&quot;source_type&quot;: &quot;promotion_answer&quot;, &quot;voteups&quot;: 4168, &quot;comments&quot;: 69, &quot;source&quot;: []}">
                            <div class="feed-item-inner">
                                <div class="avatar">
                                    <a title="${vo.user.name!}" data-tip="p$t$amuro1230" class="zm-item-link-avatar" target="_blank" href="https://nowcoder.com/people/amuro1230">
                                        <img src="${vo.user.headUrl!}" class="zm-item-img-avatar"></a>
                                </div>
                                <div class="feed-main">
                                    <div class="feed-content" data-za-module="AnswerItem">
                                        <meta itemprop="answer-id" content="389034">
                                        <meta itemprop="answer-url-token" content="13174385">
                                        <h2 class="feed-title">
                                            <a class="question_link" target="_blank" href="/question/${vo.question.id!}">${vo.question.title!}</a></h2>
                                        <div class="feed-question-detail-item">
                                            <div class="question-description-plain zm-editable-content"></div>
                                        </div>
                                        <div class="expandable entry-body">
                                            <div class="zm-item-vote">
                                                <a class="zm-item-vote-count js-expand js-vote-count" href="javascript:;" data-bind-votecount="">${vo.followCount}</a></div>
                                            <div class="zm-item-answer-author-info">
                                                <a class="author-link" data-tip="p$b$amuro1230" target="_blank" href="/user/${vo.user.id!}">${vo.user.name!}</a>
                                                , ${vo.question.createdDate?datetime!}</div>
                                            <div class="zm-item-vote-info" data-votecount="4168" data-za-module="VoteInfo">
                                                <span class="voters text">
                                                    <a href="#" class="more text">
                                                        <span class="js-voteCount">4168</span>&nbsp;人赞同</a></span>
                                            </div>
                                            <div class="zm-item-rich-text expandable js-collapse-body" data-resourceid="123114" data-action="/answer/content" data-author-name="李淼" data-entry-url="/question/19857995/answer/13174385">
                                                <div class="zh-summary summary clearfix">${vo.question.content!}</div>
                                            </div>
                                        </div>
                                        <div class="feed-meta">
                                            <div class="zm-item-meta answer-actions clearfix js-contentActions">
                                                <div class="zm-meta-panel">
                                                    <a data-follow="q:link" class="follow-link zg-follow meta-item"  id="sfb-123114">
                                                        <i class="z-icon-follow"></i>关注问题</a>
                                                    <a href="/addComment" name="addcomment" class="meta-item toggle-comment js-toggleCommentBox">
                                                        <i class="z-icon-comment"></i>${vo.question.commentCount!} 条评论</a>


                                                    <button class="meta-item item-collapse js-collapse">
                                                        <i class="z-icon-fold"></i>收起</button>
                                                </div>
                                            </div>

                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        </#list>
                    </div>
                </div>
                <div class="js-load-more">
                    <a href="javascript:;" id="zh-load-more" data-method="next" class="zg-btn-white zg-r3px zu-button-more " style="">更多</a>
                </div>
            </div>
        </div>
    </div>
 <script>
     $(function (){
         /*初始化*/
         var counter = 0; /*计数器*/
         var pageStart = 0; /*offset*/
         var pageSize = 10; /*size*/
         var isEnd = false;/*结束标志*/
         var isAjax = false;/*防止滚动过快，服务端没来得及响应造成多次请求*/

         function timestampToTime (cjsj){
             var date = new Date(cjsj);//时间戳为10位需*1000，时间戳为13位的话不需乘1000
             var Y = date.getFullYear() + '-';
             var M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
             var D = date.getDate() + ' ';
             var h = date.getHours() + ':';
             var m = date.getMinutes() + ':';
             var s = date.getSeconds();
             return Y+M+D+h+m+s
         }


         //监听点击加载更多
         $(document).on('click', '.js-load-more', function(){
             counter ++;
             pageStart = counter * pageSize;
             //console.log(pageStart);
             getData(pageStart, pageSize);
             console.log("点击了");
         });
         //监听滚轮加载更多
         $(window).scroll(function(){
             console.log(isEnd, isAjax);

             /*滚动加载时如果已经没有更多的数据了、正在发生请求时，不能继续进行*/
             if(isEnd == true || isAjax == true){
                 return;
             }

             // 当滚动到最底部以上100像素时， 加载新内容
             if ($(document).height() - $(this).scrollTop() - $(this).height()<100){
                 counter ++;
                 pageStart = counter * pageSize;

                 getData(pageStart, pageSize);
             }
         });
         function getData(offset,pageSize) {
             $.ajax({
                 url: "/ajax",
                 type: "post",
                 async: false,
                 dataType: "json",
                 data :{"offset":offset},
                 success: function (result) {

                     var newData = [];
                     console.log("成功了");
                     var data = result;
                     console.log(data);
                     <#--result += <#list vos as vo>$("#feed-2")</#list> ;-->

                 for(i = 0;i< data.length; ++i){
                     newData.push('<div class="feed-item folding feed-item-hook feed-item-2\" feed-item-a="" data-type="a" id="feed-2" data-za-module="FeedItem" data-za-index="">');
                     newData.push('<meta itemprop="ZReactor" data-id="389034" data-meta="{&quot;source_type&quot;: &quot;promotion_answer&quot;, &quot;voteups&quot;: 4168, &quot;comments&quot;: 69, &quot;source&quot;: []}">');
                     newData.push('<div class="feed-item-inner">');
                     newData.push('<div class="avatar">');
                     newData.push('<a title="'+data[i].user.name+'" data-tip="p$t$amuro1230" class="zm-item-link-avatar" target="_blank" href="https://nowcoder.com/people/amuro1230">');
                     newData.push('<img src="'+data[i].user.headUrl+'" class="zm-item-img-avatar">');
                     newData.push('</a>');
                     newData.push('</div>');
                     newData.push('<div class="feed-main">');
                     newData.push('<div class="feed-content" data-za-module="AnswerItem">');
                     newData.push('<meta itemprop="answer-id" content="389034">');
                     newData.push('<meta itemprop="answer-url-token" content="13174385">');
                     newData.push('<h2 class="feed-title">');
                     newData.push('<a class="question_link" target="_blank" href="/question/'+data[i].question.id+'">'+data[i].question.title+'</a>');
                     newData.push('</h2>');
                     newData.push('<div class="feed-question-detail-item">');
                     newData.push('<div class="question-description-plain zm-editable-content">');
                     newData.push('</div>');
                     newData.push('</div>');
                     newData.push('<div class="expandable entry-body">');
                     newData.push('<div class="zm-item-vote">');
                     newData.push('<a class="zm-item-vote-count js-expand js-vote-count" href="javascript:;" data-bind-votecount="">'+data[i].followCount+'</a>');
                     newData.push('</div>');
                     newData.push('<div class="zm-item-answer-author-info">');
                     newData.push('<a class="author-link" data-tip="p$b$amuro1230" target="_blank" href="/user/'+data[i].user.id+'">'+data[i].user.name+'</a>,' +timestampToTime(data[i].question.createdDate));
                     newData.push('</div>');
                     newData.push('<div class="zm-item-vote-info" data-votecount="4168" data-za-module="VoteInfo">');
                     newData.push('<span class="voters text">');
                     newData.push('<a href="#" class="more text">');
                     newData.push('<span class="js-voteCount">'+data[i].followCount+'</span>&nbsp;人赞同</a></span>');
                     newData.push('</div>');
                     newData.push('<div class="zm-item-rich-text expandable js-collapse-body" data-resourceid="123114" data-action="/answer/content" data-author-name="李淼" data-entry-url="/question/19857995/answer/13174385">');
                     newData.push('<div class="zh-summary summary clearfix">'+data[i].question.content+'</div>');
                     newData.push('</div>');
                     newData.push('</div>');
                     newData.push('<div class="feed-meta">');
                     newData.push('<div class="zm-item-meta answer-actions clearfix js-contentActions">');
                     newData.push('<div class="zm-meta-panel">');
                     newData.push('<a data-follow="q:link" class="follow-link zg-follow meta-item"  id="sfb-123114">');
                     newData.push('<i class="z-icon-follow"></i>关注问题</a>');
                     newData.push('<a href="/addComment" name="addcomment" class="meta-item toggle-comment js-toggleCommentBox">');
                     newData.push('<i class="z-icon-comment"></i>'+data[i].question.commentCount+' 条评论</a>');
                     newData.push('<button class="meta-item item-collapse js-collapse">');
                     newData.push('<i class="z-icon-fold"></i>收起</button>');
                     newData.push('</div>');
                     newData.push('</div>');
                     newData.push('</div>');
                     newData.push('</div>');
                     newData.push('</div>');
                     newData.push('</div>');
                     newData.push('</div>');
                 }
                     $("#zh-question-list").append(newData);

                 },
                 error: function(xhr, type){
                     console.log("lose");
                 }
             })
         }
     });
 </script>
<#include "js.ftl">

<script type="text/javascript" src="/scripts/main/site/detail.js"></script>
<#include "footer.html">
