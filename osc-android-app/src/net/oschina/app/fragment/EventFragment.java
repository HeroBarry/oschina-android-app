package net.oschina.app.fragment;

import java.io.InputStream;
import java.io.Serializable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.adapter.EventAdapter;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.base.BaseListFragment;
import net.oschina.app.base.ListBaseAdapter;
import net.oschina.app.bean.Constants;
import net.oschina.app.bean.EventList;
import net.oschina.app.bean.Event;
import net.oschina.app.bean.ListEntity;
import net.oschina.app.ui.empty.EmptyLayout;
import net.oschina.app.util.UIHelper;
import net.oschina.app.util.XmlUtils;

/** 
 * 活动列表fragment
 * @author FireAnt（http://my.oschina.net/LittleDY）
 * @version 创建时间：2014年12月8日 下午5:17:32 
 * 
 */
public class EventFragment extends BaseListFragment {
	
	public static final String BUNDLE_KEY_EVENT_TYPE = "eventlist_type";

	protected static final String TAG = EventFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "eventlist_";
	
	private int event_type;
	
	@Override
	protected ListBaseAdapter getListAdapter() {
		EventAdapter adapter = new EventAdapter();
		adapter.setEventType(event_type);
		return adapter;
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			requestData(true);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			event_type = args.getInt(BUNDLE_KEY_EVENT_TYPE);
		}
		
		if (event_type == EventList.EVENT_LIST_TYPE_MY_EVENT) {
			IntentFilter filter = new IntentFilter(
					Constants.INTENT_ACTION_USER_CHANGE);
			filter.addAction(Constants.INTENT_ACTION_LOGOUT);
			getActivity().registerReceiver(mReceiver, filter);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (event_type == EventList.EVENT_LIST_TYPE_MY_EVENT) {
			getActivity().unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void initView(View view) {
		super.initView(view);
		mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clickErrorLayout();
			}
		});
	}
	
	private void clickErrorLayout() {
		if (event_type == EventList.EVENT_LIST_TYPE_NEW_EVENT) {
			requestData(true);
		} else {
			if (AppContext.getInstance().isLogin()) {
				requestData(true);
			} else {
				UIHelper.showLoginActivity(getActivity());
			}
		}
	}

	@Override
	protected void requestData(boolean refresh) {
		
		if (event_type == EventList.EVENT_LIST_TYPE_NEW_EVENT) {
			mCatalog = -1;
			super.requestData(refresh);
			return;
		}
		if (AppContext.getInstance().isLogin()) {
			mCatalog = AppContext.getInstance().getLoginUid();
			super.requestData(refresh);
		} else {
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
		}
	}

	@Override
	protected String getCacheKeyPrefix() {
		return CACHE_KEY_PREFIX + mCatalog;
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		EventList list = XmlUtils.toBean(EventList.class, is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((EventList) seri);
	}

	@Override
	protected void sendRequestData() {
		OSChinaApi.getEventList(mCurrentPage, mCatalog, mHandler);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Event event = (Event) mAdapter.getItem(position);
		if (event != null)
			UIHelper.showEventDetail(view.getContext(), event.getId());
	}

}
