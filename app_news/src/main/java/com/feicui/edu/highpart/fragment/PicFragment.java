package com.feicui.edu.highpart.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.feicui.edu.highpart.Adapter.BigNewsRecyclerAdapter;
import com.feicui.edu.highpart.Common.OkHttpUtil;
import com.feicui.edu.highpart.MainActivity;
import com.feicui.edu.highpart.R;
import com.feicui.edu.highpart.bean.BaseEntity;
import com.feicui.edu.highpart.bean.BigNews;
import com.feicui.edu.highpart.bean.NewsGroup;
import com.feicui.edu.highpart.util.CommonUtil;
import com.feicui.edu.highpart.util.Const;
import com.feicui.edu.highpart.util.HttpUtil;
import com.feicui.edu.highpart.util.UrlParameterUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/9 0009.
 */
public class PicFragment extends Fragment {

    private List<String> titles = new ArrayList<>();
    private List<Integer> subids = new ArrayList<>();
    private static final String TAG ="PicFragment" ;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //当activity创建的时候，回调
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.setSupportActionBar(toolbar);//设置支持toolbar
            activity.backToMainActivity(toolbar);//给toolbar设置点击事件
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_pic, null);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv);
        //加载数据
        asynLoadData();
        return view;
    }

    private void asynLoadData() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                //加载新闻编号
                parseNewsGroup();
                //url 拼接  ，加载某个新闻编号
                //news_image?ver=版本号&nid=新闻编号
                Map<String ,String> map = new HashMap<>();
                map.put("ver", CommonUtil.getVersionCode(getContext())+"");
                map.put("nid",2+"");
                String url = UrlParameterUtil.parameter(Const.URL_BIG_PIC,map);
                String s = OkHttpUtil.getString(url);
                if (s != null && s.contains("OK")) {
                    //成功，解析数据
                    List<BigNews> bigNewses = pareseBigNews(s);
                    //将数据添加到适配器，刷新适配器，必须在主线程进行
                    //把数据显示到rv中
                    setDataToRV(bigNewses);
                } else {
                    //失败,吐司
                }
            }
        }.start();
    }
    private void setDataToRV(final List<BigNews> bigNewses) {
        getActivity().runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    //initView ,必须在主线程中执行
                    BigNewsRecyclerAdapter adapter = new BigNewsRecyclerAdapter(getActivity(),bigNewses);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                    }
        });
    }

    private List<BigNews> pareseBigNews(String s) {
        Gson g = new Gson();
        Type type = new TypeToken<BaseEntity<List<BigNews>>>(){}.getType();
        BaseEntity entity  = g.fromJson(s,type);
        return (List<BigNews>) entity.getData();
    }

    public void parseNewsGroup() {
        String url = "http://118.244.212.82:9092/newsClient/news_sort?ver=1&imei=1";
        String data = HttpUtil.getJsonString(url);
        if (data == null) {
            return;
        }
        Log.d(TAG, "onCreate: " + data);

        Gson gson = new Gson();
        Type type = new TypeToken<NewsGroup<List<NewsGroup.DataBean<List<NewsGroup.DataBean.SubgrpBean>>>>>() {
        }.getType();

        NewsGroup newsGroup = gson.fromJson(data, type);
        Log.d(TAG, "parseNewsGroup: " + newsGroup.getMessage());
        List<NewsGroup.DataBean> data1 = (List<NewsGroup.DataBean>) newsGroup.getData();
        for (NewsGroup.DataBean dataBean : data1) {
            String group = dataBean.getGroup();
            Log.d(TAG, "group: " + group);
            List<NewsGroup.DataBean.SubgrpBean> subgrp = (List<NewsGroup.DataBean.SubgrpBean>) dataBean.getSubgrp();
            for (NewsGroup.DataBean.SubgrpBean subgrpBean : subgrp) {
                Log.d(TAG, "sub grp: " + subgrpBean.getSubid());
                titles.add(subgrpBean.getSubgroup());
                subids.add(subgrpBean.getSubid());
            }
        }
    }
}
