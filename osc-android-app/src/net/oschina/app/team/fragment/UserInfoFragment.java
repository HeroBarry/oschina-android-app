package net.oschina.app.team.fragment;

import java.io.InputStream;
import java.io.Serializable;

import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.base.BaseListFragment;
import net.oschina.app.base.ListBaseAdapter;
import net.oschina.app.bean.Entity;
import net.oschina.app.bean.ListEntity;
import net.oschina.app.team.adapter.DynamicAdapter;
import net.oschina.app.team.adapter.TeamMemberAdapter;
import net.oschina.app.team.bean.TeamActives;
import net.oschina.app.team.bean.TeamMember;
import net.oschina.app.ui.SimpleBackActivity;
import net.oschina.app.util.TLog;
import net.oschina.app.util.XmlUtils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * 用户个人信息界面
 * 
 * @author kymjs
 * 
 */
public class UserInfoFragment extends BaseListFragment {

    TextView mTvName;
    TextView mTvUserName;
    TextView mTvEmail;
    TextView mTvJoinDate;
    TextView mTvAddress;

    private Activity aty;
    private TeamMember teamMember;
    private String teamId = "0";

    protected static final String TAG = UserInfoFragment.class.getSimpleName();
    private static final String CACHE_KEY_PREFIX = "DynamicFragment_list";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getActivity().getIntent().getBundleExtra(
                SimpleBackActivity.BUNDLE_KEY_ARGS);
        if (bundle != null) {
            teamMember = (TeamMember) bundle
                    .getSerializable(TeamMemberAdapter.TEAM_MEMBER_KEY);
            teamId = bundle.getString(TeamMemberAdapter.TEAM_ID_KEY);
        } else {
            teamMember = new TeamMember();
            TLog.log(TAG, "数据初始化异常");
        }
        aty = getActivity();
    }

    @Override
    public void initView(View view) {
        View headview = View.inflate(aty, R.layout.fragment_team_userinfo_head,
                null);
        mTvName = (TextView) headview.findViewById(R.id.fragment_team_name);
        mTvUserName = (TextView) headview
                .findViewById(R.id.fragment_team_username);
        mTvEmail = (TextView) headview.findViewById(R.id.fragment_team_email);
        mTvJoinDate = (TextView) headview
                .findViewById(R.id.fragment_team_joindate);
        mTvAddress = (TextView) headview
                .findViewById(R.id.fragment_team_address);
        mListView.addHeaderView(headview);

        mTvName.setText(teamMember.getName());
        mTvUserName.setText(teamMember.getOscName());
        mTvEmail.setText(teamMember.getTeamEmail());
        mTvJoinDate.setText(teamMember.getJoinTime());
        mTvAddress.setText(teamMember.getLocation());
        super.initView(view);
    }

    @Override
    protected ListBaseAdapter getListAdapter() {
        return new DynamicAdapter(aty);
    }

    @Override
    protected String getCacheKeyPrefix() {
        return CACHE_KEY_PREFIX + "_" + teamMember.getId() + mCurrentPage;
    }

    //
    // @Override
    // protected ListEntity parseList(InputStream is) throws Exception {
    // TeamMyActives list = XmlUtils.toBean(TeamMyActives.class, is);
    // if (list == null) {
    // list = new TeamMyActives();
    // list.setList(new ArrayList<TeamMyActive>(1));
    // }
    // return list;
    // }
    //
    // @Override
    // protected ListEntity<? extends Entity> readList(Serializable seri) {
    // return (TeamMyActives) seri;
    // }

    @Override
    protected ListEntity parseList(InputStream is) throws Exception {
        TeamActives list = XmlUtils.toBean(TeamActives.class, is);
        return list;
    }

    @Override
    protected ListEntity<? extends Entity> readList(Serializable seri) {
        return (TeamActives) seri;
    }

    @Override
    protected void sendRequestData() {
        OSChinaApi.getUserDynamic(teamId, teamMember.getId() + "",
                mCurrentPage, mHandler);
    }
}
