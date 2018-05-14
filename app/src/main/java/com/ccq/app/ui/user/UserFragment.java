package com.ccq.app.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ccq.app.R;
import com.ccq.app.base.BaseFragment;
import com.ccq.app.base.BasePresenter;
import com.ccq.app.entity.UserBean;
import com.ccq.app.http.ApiService;
import com.ccq.app.http.RetrofitClient;
import com.ccq.app.ui.user.adapter.MyFragmentAdapter;
import com.ccq.app.utils.AppCache;
import com.ccq.app.utils.Constants;
import com.ccq.app.utils.ToastUtils;
import com.google.gson.jpush.JsonObject;
import com.google.gson.jpush.JsonParser;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/****************************************
 * 功能说明:  我的
 *
 * Author: Created by bayin on 2018/3/26.
 ****************************************/

public class UserFragment extends BaseFragment implements IWXAPIEventHandler {

    @BindView(R.id.llyout_my_info)
    LinearLayout llyoutMyInfo;
    @BindView(R.id.tv_my_attention_count)
    TextView tvMyAttentionCount;
    @BindView(R.id.tv_my_fans_count)
    TextView tvMyFansCount;
    @BindView(R.id.tv_home_line)
    View tvHomeLine;
    @BindView(R.id.tv_intro_line)
    View tvIntroLine;
    @BindView(R.id.llyout_my_attention)
    LinearLayout llyoutMyAttention;
    @BindView(R.id.layout_my_subscribe)
    LinearLayout layoutMySubscribe;
    @BindView(R.id.layout_my_subscribe_fans)
    LinearLayout layoutMySubscribeFans;
    @BindView(R.id.btn_invite_attation)
    Button btnInviteAttation;

    private MyFragmentAdapter adapter;

    @BindView(R.id.user_iv_header)
    ImageView ivHeader;

    @BindView(R.id.tv_userName)
    TextView tvName;

    @BindView(R.id.tv_home)
    TextView tvHome;
    @BindView(R.id.tv_intro)
    TextView tvIntro;

    Unbinder unbinder;
    @BindView(R.id.vp_my_info)
    ViewPager vpMyInfo;

    @OnClick(R.id.user_iv_header)
    public void login() {
        startActivity(new Intent(get(), LoginActivity.class));
    }

    @Override
    protected int inflateContentView() {
        return R.layout.fragment_user;
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void initView(View rootView) {
        EventBus.getDefault().register(this);
    }

    @Override
    public void initData() {
        UserBean userBean = AppCache.getUserBean();
        if (userBean != null) {
            Glide.with(get()).load(userBean.getHeadimgurl()).into(ivHeader);
            setView();
            getData();
        }
    }

    @Subscribe
    public void onReceiveLoginSuccess(Integer eventId) {
        Log.e("222---", eventId.toString());

        if (eventId.equals(Constants.WX_LOGIN_SUCCESS)) {
            ToastUtils.show(get(), "登录成功！设置页面数据！");
            Glide.with(get()).load(AppCache.getUserBean().getHeadimgurl()).into(ivHeader);
            setView();
            getData();
        }
    }

    @Subscribe
    public void onRefreshData(Integer eventId) {
        Log.e("333---", eventId.toString());
    }


    public void setView() {
        tvName.setText(AppCache.getUserBean().getNickname());
        llyoutMyInfo.setVisibility(View.VISIBLE);
        llyoutMyAttention.setVisibility(View.VISIBLE);
        adapter = new MyFragmentAdapter(getActivity().getSupportFragmentManager());
        vpMyInfo.setAdapter(adapter);
        vpMyInfo.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        selectHome();
                        break;
                    case 1:
                        selectIntro();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void getData() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        apiService.getSubscribeCount(AppCache.getUserBean().getUserid()).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response != null && response.body() != null) {
                    Object obj = response.body();
                    if (obj != null) {
                        JsonObject returnData = new JsonParser().parse(obj.toString()).getAsJsonObject();
                        //   v1:10  我订阅的， v2:10   订阅我的
                        tvMyAttentionCount.setText(String.valueOf(returnData.get("v1").getAsInt()));
                        tvMyFansCount.setText(String.valueOf(returnData.get("v2").getAsInt()));
                    }
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

            }
        });

    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        int result = 0;

        Toast.makeText(getHostActivity(), "baseresp.getType = " + baseResp.getType(), Toast.LENGTH_SHORT).show();

        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = R.string.errcode_success;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.errcode_unsupported;
                break;
            default:
                result = R.string.errcode_unknown;
                break;
        }

        Toast.makeText(getHostActivity(), result, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.tv_home, R.id.tv_intro, R.id.layout_my_subscribe, R.id.layout_my_subscribe_fans})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_home:
                vpMyInfo.setCurrentItem(0);
                selectHome();
                break;
            case R.id.tv_intro:
                vpMyInfo.setCurrentItem(1);
                selectIntro();
                break;
            case R.id.layout_my_subscribe:
                Intent i = new Intent(getActivity(), UserSubscribeActivity.class);
                i.putExtra("type", 0);
                getActivity().startActivity(i);
                break;
            case R.id.layout_my_subscribe_fans:
                Intent ii = new Intent(getActivity(), UserSubscribeActivity.class);
                ii.putExtra("type", 1);
                getActivity().startActivity(ii);
                break;
        }
    }

    private void selectHome() {
        tvHome.setTextColor(getResources().getColor(R.color.steelblue));
        tvIntro.setTextColor(getResources().getColor(R.color.tab_text));
        tvHomeLine.setVisibility(View.VISIBLE);
        tvIntroLine.setVisibility(View.GONE);
    }

    private void selectIntro() {
        tvHome.setTextColor(getResources().getColor(R.color.tab_text));
        tvIntro.setTextColor(getResources().getColor(R.color.steelblue));
        tvHomeLine.setVisibility(View.GONE);
        tvIntroLine.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_invite_attation)
    public void onViewClicked() {

    }


}
