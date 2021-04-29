package com.atguigu.gmallmy0311.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmallmy0311.bean.SkuLsInfo;
import com.atguigu.gmallmy0311.bean.SkuLsParams;
import com.atguigu.gmallmy0311.bean.SkuLsResult;
import com.atguigu.gmallmy0311.config.RedisUtil;
import com.atguigu.gmallmy0311.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class listServiceImpl implements ListService {

    @Autowired
  private  JestClient jestClient;
    public static final String ES_INDEX="gmall";
    public static final String ES_TYPE="SkuInfo";

    @Autowired
       private  RedisUtil redisUtil;
    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        // 保存数据
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        try {
             jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        //1.构建查询语句
        String query=makeQueryStringForSearch(skuLsParams);
        //2.构建查询器
        Search search= new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult=null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeResultForSearch(searchResult,skuLsParams);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
//获取jedis用来操作redis
        Jedis jedis = redisUtil.getJedis();
        int timesToEs=10;
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId" + skuId);
        if (hotScore%timesToEs==0){
            updateHotScore(skuId,Math.round(hotScore));
          }



    }

    private void updateHotScore(String skuId, long hotScore) {
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";
        Update update  = new Update.Builder(updateJson).index("gmall").type("SkuInfo").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private SkuLsResult makeResultForSearch(SearchResult searchResult, SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult=new SkuLsResult();
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        //获取sku列表
        List<SearchResult.Hit<SkuLsInfo, Void>> hits=null;
        if (!StringUtils.isEmpty(searchResult)){
             hits = searchResult.getHits(SkuLsInfo.class);
            if (!StringUtils.isEmpty(hits)) {
                for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                    SkuLsInfo skuLsInfo  = hit.source;
                    if(hit.highlight!=null && hit.highlight.size()>0){
                        List<String> list = hit.highlight.get("skuName");
                        //把带有高亮标签的字符串替换skuName
                        String skuNameHl = list.get(0);
                        skuLsInfo.setSkuName(skuNameHl);

                    }
                    skuLsInfoArrayList.add(skuLsInfo);
                }


            }

            skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
            skuLsResult.setTotal(searchResult.getTotal());
            //取记录个数并计算出总页数
            long totalPage = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
            skuLsResult.setTotalPages(totalPage);
            //取出涉及的属性值id
            ArrayList<String> arrayList = new ArrayList<>();
            TermsAggregation groupby_attr =searchResult.getAggregations().getTermsAggregation("groupby_attr");

            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                arrayList.add(valueId);

            }
            skuLsResult.setAttrValueIdList(arrayList);
        }

  
        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
// 创建查询bulid
        SearchSourceBuilder searchSourceBuilder  = new SearchSourceBuilder();
        //2.获取boolquerybuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            MatchQueryBuilder  matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
             boolQueryBuilder.must(matchQueryBuilder);
            // 设置高亮
            HighlightBuilder highlighter =searchSourceBuilder.highlighter();
            // 设置高亮字段

          
            highlighter.postTags("</span>");
			  highlighter.preTags("<span style=color:red>");
            highlighter.field("skuName");
            // 将高亮结果放入查询器中
            searchSourceBuilder.highlight(highlighter);

        }
        // 设置属性值
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                boolQueryBuilder.filter(termQueryBuilder);


            }
        }
    //设置三级分类
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        // 设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
// 设置分页
        int from  = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        // 设置按照热度
      //  searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        // 设置聚合
        TermsBuilder  groupby_attr= AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        String query  = searchSourceBuilder.toString();
        System.out.println("query:"+query);
        return  query;
    }
}
