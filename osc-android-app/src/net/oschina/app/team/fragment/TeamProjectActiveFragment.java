package net.oschina.app.team.fragment;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import com.tencent.weibo.sdk.android.component.sso.tools.MD5Tools;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaTeamApi;
import net.oschina.app.base.BaseListFragment;
import net.oschina.app.base.ListBaseAdapter;
import net.oschina.app.team.adapter.DynamicAdapter;
import net.oschina.app.team.adapter.TeamProjectMemberAdapter;
import net.oschina.app.team.bean.Team;
import net.oschina.app.team.bean.TeamActive;
import net.oschina.app.team.bean.TeamActives;
import net.oschina.app.team.bean.TeamProject;
import net.oschina.app.team.bean.TeamProjectMember;
import net.oschina.app.team.bean.TeamProjectMemberList;
import net.oschina.app.team.ui.TeamMainActivity;
import net.oschina.app.ui.empty.EmptyLayout;
import net.oschina.app.util.XmlUtils;
import android.os.Bundle;

/**
 * 团队动态列表
 * TeamProjectFragment.java
 * 
 * @author 火蚁(http://my.oschina.net/u/253900)
 * 
 * @data 2015-2-28 下午4:08:58
 */
public class TeamProjectActiveFragment extends
	BaseListFragment<TeamActive> {

    private Team mTeam;

    private int mTeamId;

    private TeamProject mTeamProject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Bundle bundle = getArguments();
	if (bundle != null) {
	    mTeam = (Team) bundle
		    .getSerializable(TeamMainActivity.BUNDLE_KEY_TEAM);
	    
	    mTeamProject = (TeamProject) bundle.getSerializable(TeamMainActivity.BUNDLE_KEY_PROJECT);

	    mTeamId = mTeam.getId();
	}
    }

    @Override
    protected DynamicAdapter getListAdapter() {
	// TODO Auto-generated method stub
	return new DynamicAdapter(getActivity());
    }

    @Override
    protected String getCacheKeyPrefix() {
	return "team_project_active_list_" + mTeamId + "_" + mTeamProject.getGit().getId();
    }

    @Override
    protected TeamActives parseList(InputStream is) throws Exception {
	TeamActives list = XmlUtils.toBean(
		TeamActives.class, is);
	return list;
    }

    @Override
    protected TeamActives readList(Serializable seri) {
	return ((TeamActives) seri);
    }

    @Override
    protected void sendRequestData() {
	// TODO Auto-generated method stub
	OSChinaTeamApi.getTeamProjectActiveList(mTeamId, mTeamProject, "all", mCurrentPage, mHandler);
    }

    @Override
    protected void executeOnLoadDataSuccess(List<TeamActive> data) {
	// TODO Auto-generated method stub
	super.executeOnLoadDataSuccess(data);
	if (mAdapter.getData().isEmpty()) {
	    setNoProjectMember();
	}
	mAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
    }

    private void setNoProjectMember() {
	mErrorLayout.setErrorType(EmptyLayout.NODATA);
	mErrorLayout.setErrorImag(R.drawable.page_icon_empty);
	String str = getResources().getString(
		R.string.team_empty_project_active);
	mErrorLayout.setErrorMessage(str);
    }
}
