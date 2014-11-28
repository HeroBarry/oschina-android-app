package net.oschina.app.fragment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpResponseHandler;
import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.adapter.CommentAdapter;
import net.oschina.app.adapter.CommentAdapter.OnOperationListener;
import net.oschina.app.api.OperationResponseHandler;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.base.BaseActivity;
import net.oschina.app.base.BaseListFragment;
import net.oschina.app.base.ListBaseAdapter;
import net.oschina.app.bean.BlogCommentList;
import net.oschina.app.bean.Comment;
import net.oschina.app.bean.CommentList;
import net.oschina.app.bean.ListEntity;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.ResultBean;
import net.oschina.app.emoji.EmojiFragment;
import net.oschina.app.emoji.EmojiFragment.EmojiTextListener;
import net.oschina.app.ui.dialog.CommonDialog;
import net.oschina.app.ui.dialog.DialogHelper;
import net.oschina.app.util.HTMLSpirit;
import net.oschina.app.util.TDevice;
import net.oschina.app.util.UIHelper;
import net.oschina.app.util.XmlUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CommentFrament extends BaseListFragment implements
		OnOperationListener, EmojiTextListener, OnItemLongClickListener {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";
	public static final String BUNDLE_KEY_BLOG = "BUNDLE_KEY_BLOG";
	public static final String BUNDLE_KEY_ID = "BUNDLE_KEY_ID";
	public static final String BUNDLE_KEY_OWNER_ID = "BUNDLE_KEY_OWNER_ID";
	protected static final String TAG = CommentFrament.class.getSimpleName();
	private static final String BLOG_CACHE_KEY_PREFIX = "blogcomment_list";
	private static final String CACHE_KEY_PREFIX = "comment_list";
	private static final int REQUEST_CODE = 0x10;

	private int mId, mOwnerId;
	private boolean mIsBlogComment;

	private EmojiFragment mEmojiFragment;
	
	private AsyncHttpResponseHandler mCommentHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
			try {
				ResultBean rsb = XmlUtils.toBean(ResultBean.class, new ByteArrayInputStream(arg2));
				Result res = rsb.getResult();
				if (res.OK()) {
					hideWaitDialog();
					AppContext.showToastShort(R.string.comment_publish_success);
					
					mAdapter.addItem(0, rsb.getComment());
					mAdapter.notifyDataSetChanged();
					mEmojiFragment.reset();
					UIHelper.sendBroadCastCommentChanged(getActivity(),
							mIsBlogComment, mId, mCatalog, Comment.OPT_ADD,
							rsb.getComment());
				} else {
					hideWaitDialog();
					AppContext.showToastShort(res.getErrorMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(arg0, arg1, arg2, e);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			hideWaitDialog();
			AppContext.showToastShort(R.string.comment_publish_faile);
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		BaseActivity act = ((BaseActivity) activity);
		FragmentTransaction trans = act.getSupportFragmentManager()
				.beginTransaction();
		mEmojiFragment = new EmojiFragment();
		mEmojiFragment.setEmojiTextListener(this);
		trans.replace(R.id.emoji_container, mEmojiFragment);
		trans.commit();
		activity.findViewById(R.id.emoji_container).setVisibility(View.VISIBLE);
	}

	protected int getLayoutRes() {
		return R.layout.fragment_pull_refresh_listview;
	}
	
	@Override
	public void initView(View view) {
		super.initView(view);
		mListView.setOnItemLongClickListener(this);
	}

	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mCatalog = args.getInt(BUNDLE_KEY_CATALOG, 0);
			mId = args.getInt(BUNDLE_KEY_ID, 0);
			mOwnerId = args.getInt(BUNDLE_KEY_OWNER_ID, 0);
			mIsBlogComment = args.getBoolean(BUNDLE_KEY_BLOG, false);
		}

		if (!mIsBlogComment && mCatalog == CommentList.CATALOG_POST) {
			((BaseActivity) getActivity())
					.setActionBarTitle(R.string.post_answer);
		}

		int mode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
		getActivity().getWindow().setSoftInputMode(mode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE 
				&& resultCode == Activity.RESULT_OK) {
			Comment comment = data.getParcelableExtra(Comment.BUNDLE_KEY_COMMENT);
			if(comment != null) {
				mAdapter.addItem(0,comment);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected ListBaseAdapter getListAdapter() {
		return new CommentAdapter(this);
	}

	@Override
	protected String getCacheKeyPrefix() {
		String str = mIsBlogComment ? BLOG_CACHE_KEY_PREFIX : CACHE_KEY_PREFIX;
		return new StringBuilder(str).append("_").append(mId).append("_Owner")
				.append(mOwnerId).toString();
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		if (mIsBlogComment) {
			return XmlUtils.toBean(BlogCommentList.class, is);
		} else {
			return XmlUtils.toBean(CommentList.class, is);
		}
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		if (mIsBlogComment)
			return ((BlogCommentList) seri);
		return ((CommentList) seri);
	}

	@Override
	public boolean onBackPressed() {
		if (mEmojiFragment != null) {
			return mEmojiFragment.onBackPressed();
		}
		return super.onBackPressed();
	}

	@Override
	protected void sendRequestData() {
		if (mIsBlogComment) {
			OSChinaApi.getBlogCommentList(mId, mCurrentPage, mHandler);
		} else {
			OSChinaApi.getCommentList(mId, mCatalog, mCurrentPage, mHandler);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Comment comment = (Comment) mAdapter.getItem(position);
		if (comment == null)
			return;
		mEmojiFragment.setTag(comment);
		mEmojiFragment.setInputHint("回复" + comment.getAuthor() + ":");
		mEmojiFragment.requestFocusInput();
	}

	private void handleReplyComment(Comment comment, String text) {
		showWaitDialog(R.string.progress_submit);
		if (!AppContext.getInstance().isLogin()) {
			UIHelper.showLoginActivity(getActivity());
			return;
		}
		
		if (mIsBlogComment) {
			OSChinaApi.replyBlogComment(mId, AppContext.getInstance().getLoginUid(),
					text, comment.getId(), comment.getAuthorId(),
					mCommentHandler);
		} else {
			OSChinaApi.replyComment(mId, mCatalog, comment.getId(),
					comment.getAuthorId(),
					AppContext.getInstance().getLoginUid(), text,
					mCommentHandler);
		}
	}
	
	private void handleComment(String text) {
		showWaitDialog(R.string.progress_submit);
		if (mIsBlogComment) {
			OSChinaApi.publicBlogComment(mId, AppContext.getInstance().getLoginUid(), text, mCommentHandler);
		} else {
			OSChinaApi.publicComment(mCatalog, mId, AppContext.getInstance().getLoginUid(), text, 1, mCommentHandler);
		}
	}

	private void handleDeleteComment(Comment comment) { 
		if (!AppContext.getInstance().isLogin()) {
			UIHelper.showLoginActivity(getActivity());
			return;
		}
		AppContext.showToastShort(R.string.deleting);
		if (mIsBlogComment) {
			OSChinaApi.deleteBlogComment(AppContext.getInstance().getLoginUid(), mId,
					comment.getId(), comment.getAuthorId(), mOwnerId,
					new DeleteOperationResponseHandler(comment));
		} else {
			OSChinaApi.deleteComment(mId, mCatalog, comment.getId(), comment
					.getAuthorId(), new DeleteOperationResponseHandler(comment));
		}
	}

	@Override
	public void onSendClick(String text) {
		if (!TDevice.hasInternet()) {
			AppContext.showToastShort(R.string.tip_network_error);
			return;
		}
		if (!AppContext.getInstance().isLogin()) {
			UIHelper.showLoginActivity(getActivity());
			return;
		}
		if (TextUtils.isEmpty(text)) {
			AppContext.showToastShort(R.string.tip_comment_content_empty);
			mEmojiFragment.requestFocusInput();
			return;
		}
		
		if (mEmojiFragment.getInputTag() != null) {
			handleReplyComment((Comment)mEmojiFragment.getInputTag(), text);
		} else {
			handleComment(text);
		}
	}

	class DeleteOperationResponseHandler extends OperationResponseHandler {

		DeleteOperationResponseHandler(Object... args) {
			super(args);
		}

		@Override
		public void onSuccess(int code, ByteArrayInputStream is, Object[] args) {
			try {
				Result res = XmlUtils.toBean(ResultBean.class, is).getResult();
				if (res.OK()) {
					AppContext.showToastShort(R.string.delete_success);
					mAdapter.removeItem(args[0]);
				} else {
					AppContext.showToastShort(res.getErrorMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(code, e.getMessage(), args);
			}
		}

		@Override
		public void onFailure(int code, String errorMessage, Object[] args) {
			AppContext.showToastShort(R.string.delete_faile);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		final Comment item = (Comment) mAdapter.getItem(position);
		if (item == null)
			return false;
		int itemsLen = item.getAuthorId() == AppContext.getInstance().getLoginUid() ? 2 : 1;
		String[] items = new String[itemsLen];
		items[0] = getResources().getString(R.string.copy);
		if (itemsLen == 2) {
			items[1] = getResources().getString(R.string.delete);
		}
		final CommonDialog dialog = DialogHelper
				.getPinterestDialogCancelable(getActivity());
		dialog.setNegativeButton(R.string.cancle, null);
		dialog.setItemsWithoutChk(items, new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dialog.dismiss();
				if (position == 0) {
					TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(item
							.getContent()));
				} else if(position == 1) {
					handleDeleteComment(item);
				}
			}
		});
		dialog.show();
		return true;
	}

	@Override
	public void onMoreClick(Comment comment) {
		
	}
}